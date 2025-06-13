import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.toedter.calendar.JDateChooser;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Font;
import com.lowagie.text.Element;

public class ExportPDFFrame extends JFrame {

    private JComboBox<String> classeComboBox;
    private JDateChooser debutChooser, finChooser;
    private final String URL = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
    private final String USER = "postgres";
    private final String PASSWORD = "24268641";

    public ExportPDFFrame() {
        setTitle("Exporter Emploi du Temps en PDF");
        setSize(500, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(0xF5F5F5));
        setLayout(new GridLayout(5, 2, 10, 10));

        add(new JLabel("Classe :"));
        classeComboBox = new JComboBox<>();
        chargerClasses();
        add(classeComboBox);

        add(new JLabel("Date Début :"));
        debutChooser = new JDateChooser();
        debutChooser.setDateFormatString("yyyy-MM-dd");
        debutChooser.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        debutChooser.setDate(new Date());
        add(debutChooser);

        add(new JLabel("Date Fin :"));
        finChooser = new JDateChooser();
        finChooser.setDateFormatString("yyyy-MM-dd");
        finChooser.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        finChooser.setDate(new Date());
        add(finChooser);

        JButton btnExporter = new JButton("Générer PDF");
        styliserBouton(btnExporter, new Color(0x2196F3)); // Bleu
        add(btnExporter);

        JButton btnRetour = new JButton("Retour");
        styliserBouton(btnRetour, new Color(0x9E9E9E)); // Gris
        add(btnRetour);

        setVisible(true);

        // --- Actions
        btnExporter.addActionListener(e -> genererPDF());
        btnRetour.addActionListener(e -> dispose());
    }

    private void chargerClasses() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT idclasse, niveau FROM classe ORDER BY idclasse")) {

            while (rs.next()) {
                String item = rs.getString("idclasse") + " - " + rs.getString("niveau");
                classeComboBox.addItem(item);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des classes !");
        }
    }

    private void styliserBouton(JButton bouton, Color couleur) {
        bouton.setBackground(couleur);
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);
        bouton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setPreferredSize(new Dimension(140, 38));
        bouton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        bouton.setOpaque(true);
        bouton.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));
    }

    private void genererPDF() {
        String selected = (String) classeComboBox.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une classe !");
            return;
        }

        // Extraire idclasse et niveau
        String[] parts = selected.split(" - ");
        String idClasse = parts[0];
        String niveauClasse = parts.length > 1 ? parts[1] : "Inconnu";

        Date debut = debutChooser.getDate();
        Date fin = finChooser.getDate();

        if (debut == null || fin == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner des dates valides !");
            return;
        }

        if (fin.before(debut)) {
            JOptionPane.showMessageDialog(this, "La date de fin doit être postérieure à la date de début !");
            return;
        }

        String sql = """
            SELECT edt.cours, s.design AS salle, s.idsalle AS salle_id,
                   p.nom || ' ' || p.prénoms AS professeur,
                   edt.datedebut, edt.datefin
            FROM emploi_du_temps edt
            JOIN salle s ON edt.idsalle = s.idsalle
            JOIN professeur p ON edt.idprof = p.idprof
            WHERE edt.idclasse = ?
              AND edt.datedebut::date BETWEEN ? AND ?
            ORDER BY edt.datedebut
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, idClasse);
            pst.setDate(2, new java.sql.Date(debut.getTime()));
            pst.setDate(3, new java.sql.Date(fin.getTime()));

            ResultSet rs = pst.executeQuery();

            // Boîte de dialogue pour choisir le chemin
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choisir l'emplacement de sauvegarde");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = dateFormat.format(new Date());
            fileChooser.setSelectedFile(new java.io.File("emploi_du_temps_" + idClasse + "_" + timestamp + ".pdf"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(this, "Exportation annulée.");
                return;
            }

            String nomFichier = fileChooser.getSelectedFile().getAbsolutePath();
            if (!nomFichier.toLowerCase().endsWith(".pdf")) {
                nomFichier += ".pdf";
            }

            // Check if file is in use
            File file = new File(nomFichier);
            if (file.exists() && !file.canWrite()) {
                JOptionPane.showMessageDialog(this, "Le fichier est déjà ouvert. Veuillez le fermer avant de générer un nouveau PDF.");
                return;
            }

            // Création du PDF
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            com.lowagie.text.Font titreFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 18);
            com.lowagie.text.Font texteFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            com.lowagie.text.Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            com.lowagie.text.Font tableCellFont = FontFactory.getFont(FontFactory.HELVETICA, 14);

            SimpleDateFormat dateFormatFull = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            // Titre avec le niveau de la classe
            Paragraph titre = new Paragraph("Emploi du Temps - " + niveauClasse, titreFont);
            titre.setSpacingAfter(20f);
            document.add(titre);

            document.add(new Paragraph(
                    "Du " + dateOnlyFormat.format(debut) + " au " + dateOnlyFormat.format(fin),
                    texteFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Table header styling
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(new java.awt.Color(64, 64, 64));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            String[] headers = {"Cours", "Salle", "Professeur", "Début", "Fin"};
            for (String header : headers) {
                headerCell.setPhrase(new Paragraph(header, tableHeaderFont));
                table.addCell(headerCell);
            }

            boolean hasData = false;
            int rowCount = 0;
            while (rs.next()) {
                PdfPCell cell = new PdfPCell();
                cell.setBorderWidth(0.5f);
                cell.setBackgroundColor(rowCount % 2 == 0 ? java.awt.Color.WHITE : new java.awt.Color(240, 240, 240));

                cell.setPhrase(new Paragraph(rs.getString("cours") != null ? rs.getString("cours") : "N/A", texteFont));
                table.addCell(cell);

                String salleDesign = rs.getString("salle");
                cell.setPhrase(new Paragraph(salleDesign != null ? salleDesign : "Non défini", texteFont));
                table.addCell(cell);

                cell.setPhrase(new Paragraph(rs.getString("professeur") != null ? rs.getString("professeur") : "N/A", texteFont));
                table.addCell(cell);

                cell.setPhrase(new Paragraph(timeFormat.format(rs.getTimestamp("datedebut")), tableCellFont));
                table.addCell(cell);

                cell.setPhrase(new Paragraph(timeFormat.format(rs.getTimestamp("datefin")), tableCellFont));
                table.addCell(cell);

                hasData = true;
                rowCount++;
            }

            if (hasData) {
                document.add(table);
                JOptionPane.showMessageDialog(this, "PDF généré avec succès !");
            } else {
                document.add(new Paragraph("Aucune donnée pour cette période."));
                JOptionPane.showMessageDialog(this, "Aucune session trouvée pour cette classe.");
            }

            document.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion à la base de données : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la génération du PDF : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Class.forName("org.postgresql.Driver");
                new ExportPDFFrame();
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Pilote PostgreSQL introuvable !");
            }
        });
    }
}