import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Vector;
import com.github.lgooddatepicker.components.DateTimePicker;

public class EmploiDuTempsFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnAjouter, btnModifier, btnSupprimer, btnRetour;

    private final String URL = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
    private final String USER = "postgres";
    private final String PASSWORD = "24268641";

    public EmploiDuTempsFrame() {
        setTitle("Emploi du Temps");
        setSize(900, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.decode("#F5F5F5"));

        model = new DefaultTableModel(new String[]{"Salle", "Professeur", "Classe", "Cours", "Date Début", "Date Fin"}, 0);
        table = new JTable(model);

        // Personnalisation JTable
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0, 123, 255));
        header.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);

        // Création des boutons avec icônes
        btnAjouter   = creerBoutonAvecIcone("Ajouter", new Color(40, 167, 69), "ajouter.png");
        btnModifier  = creerBoutonAvecIcone("Modifier", new Color(0, 123, 255), "modifier.png");
        btnSupprimer = creerBoutonAvecIcone("Supprimer", new Color(220, 53, 69), "supprimer.png");
        btnRetour    = creerBoutonAvecIcone("Retour", Color.GRAY, "retour.png");

        JPanel panelBoutons = new JPanel();
        panelBoutons.setBackground(Color.decode("#F5F5F5"));
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnRetour);

        add(scrollPane, BorderLayout.CENTER);
        add(panelBoutons, BorderLayout.SOUTH);

        chargerEmplois();

        // Actions
        btnAjouter.addActionListener(e -> afficherFormulaire(null));
        btnModifier.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                afficherFormulaire(model.getDataVector().elementAt(selectedRow));
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne à modifier.");
            }
        });
        btnSupprimer.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                Vector<?> row = model.getDataVector().elementAt(selectedRow);
                String salle      = row.get(0).toString();
                String prof       = row.get(1).toString();
                String classe     = row.get(2).toString();
                String dateDebut  = row.get(4).toString();
                String dateFin    = row.get(5).toString();

                int idSalle  = Integer.parseInt(salle.split(" - ")[0]);
                int idProf   = Integer.parseInt(prof.split(" - ")[0]);
                String idClasse = classe.split(" - ")[0];

                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                    PreparedStatement delete = conn.prepareStatement(
                        "DELETE FROM emploi_du_temps " +
                        "WHERE idsalle = ? AND idprof = ? AND idclasse = ? AND datedebut = ? AND datefin = ?");
                    delete.setInt(1, idSalle);
                    delete.setInt(2, idProf);
                    delete.setString(3, idClasse);
                    delete.setTimestamp(4, Timestamp.valueOf(dateDebut));
                    delete.setTimestamp(5, Timestamp.valueOf(dateFin));

                    int confirm = JOptionPane.showConfirmDialog(this,
                        "Confirmer la suppression de cette session ?", "Confirmation",
                        JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        delete.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Session supprimée avec succès !");
                        chargerEmplois();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur lors de la suppression !");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne à supprimer.");
            }
        });
        btnRetour.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void chargerEmplois() {
        model.setRowCount(0);
        String sql =
            "SELECT edt.idsalle, s.design, edt.idprof, p.nom, p.prénoms, " +
            "edt.idclasse, c.niveau, edt.cours, edt.datedebut, edt.datefin " +
            "FROM emploi_du_temps edt " +
            "JOIN salle s ON edt.idsalle = s.idsalle " +
            "JOIN professeur p ON edt.idprof = p.idprof " +
            "JOIN classe c ON edt.idclasse = c.idclasse " +
            "ORDER BY edt.datedebut";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt  = conn.createStatement();
             ResultSet rs     = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String salle = rs.getInt("idsalle") + " - " + rs.getString("design");
                String prof = rs.getInt("idprof") + " - " + rs.getString("nom") + " " + rs.getString("prénoms");
                String classe = rs.getString("idclasse") + " - " + rs.getString("niveau");
                String cours = rs.getString("cours");
                Timestamp dDebut = rs.getTimestamp("datedebut");
                Timestamp dFin = rs.getTimestamp("datefin");

                model.addRow(new Object[]{
                    salle, prof, classe, cours,
                    dDebut.toString(), dFin.toString()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de chargement des données !");
        }
    }

    private void afficherFormulaire(Object rowData) {
        boolean isModification = rowData != null;
        JDialog dialog = new JDialog(this,
            isModification ? "Modifier une session" : "Ajouter une session", true);
        dialog.setLayout(new GridLayout(7, 2, 10, 10));
        dialog.getContentPane().setBackground(Color.decode("#F5F5F5"));

        JComboBox<String> salleComboBox      = new JComboBox<>();
        JComboBox<String> professeurComboBox = new JComboBox<>();
        JComboBox<String> classeComboBox     = new JComboBox<>();
        JTextField coursField                = new JTextField();
        DateTimePicker dateDebutChooser = new DateTimePicker();
        DateTimePicker dateFinChooser = new DateTimePicker();

        // Configurer l'apparence des DateTimePicker
        dateDebutChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateFinChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        chargerComboBox(salleComboBox,
            "SELECT idsalle, design FROM SALLE", "idsalle", "design");
        chargerComboBox(professeurComboBox,
            "SELECT idprof, nom, prénoms FROM PROFESSEUR", "idprof", "nom", "prénoms");
        chargerComboBox(classeComboBox,
            "SELECT idclasse, niveau FROM CLASSE", "idclasse", "niveau");

        if (isModification) {
            Vector<?> row = (Vector<?>) rowData;
            salleComboBox.setSelectedItem(row.get(0).toString());
            professeurComboBox.setSelectedItem(row.get(1).toString());
            classeComboBox.setSelectedItem(row.get(2).toString());
            coursField.setText(row.get(3).toString());
            try {
                Timestamp timestampDebut = Timestamp.valueOf(row.get(4).toString());
                Timestamp timestampFin = Timestamp.valueOf(row.get(5).toString());
                dateDebutChooser.setDateTimeStrict(timestampDebut.toLocalDateTime());
                dateFinChooser.setDateTimeStrict(timestampFin.toLocalDateTime());
            } catch (Exception e) {
                dateDebutChooser.setDateTimeStrict(LocalDateTime.now());
                dateFinChooser.setDateTimeStrict(LocalDateTime.now());
            }
        } else {
            // Par défaut, définir la date et l'heure actuelles pour les nouveaux ajouts
            dateDebutChooser.setDateTimeStrict(LocalDateTime.now());
            dateFinChooser.setDateTimeStrict(LocalDateTime.now());
        }

        dialog.add(new JLabel("Salle :"));      dialog.add(salleComboBox);
        dialog.add(new JLabel("Professeur :")); dialog.add(professeurComboBox);
        dialog.add(new JLabel("Classe :"));     dialog.add(classeComboBox);
        dialog.add(new JLabel("Cours :"));      dialog.add(coursField);
        dialog.add(new JLabel("Date Début :")); dialog.add(dateDebutChooser);
        dialog.add(new JLabel("Date Fin :"));   dialog.add(dateFinChooser);

        JButton btnValider  = creerBoutonStylé(isModification ? "Modifier" : "Ajouter", new Color(40, 167, 69));
        JButton btnAnnuler  = creerBoutonStylé("Annuler", Color.GRAY);

        btnAnnuler.addActionListener(e -> dialog.dispose());
        btnValider.addActionListener(e -> {
            String salleSel  = (String) salleComboBox.getSelectedItem();
            String profSel   = (String) professeurComboBox.getSelectedItem();
            String classeSel = (String) classeComboBox.getSelectedItem();
            String cours     = coursField.getText().trim();
            LocalDateTime dateTimeDebut = dateDebutChooser.getDateTimeStrict();
            LocalDateTime dateTimeFin = dateFinChooser.getDateTimeStrict();

            // Validation des dates
            if (dateTimeDebut == null || dateTimeFin == null) {
                JOptionPane.showMessageDialog(dialog, "Veuillez sélectionner des dates et heures valides !");
                return;
            }
            if (dateTimeFin.isBefore(dateTimeDebut)) {
                JOptionPane.showMessageDialog(dialog, "La date de fin doit être postérieure à la date de début !");
                return;
            }
            if (cours.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Le champ Cours ne peut pas être vide !");
                return;
            }

            int idSalle      = Integer.parseInt(salleSel.split(" - ")[0]);
            int idProf       = Integer.parseInt(profSel.split(" - ")[0]);
            String idClasse  = classeSel.split(" - ")[0];
            Timestamp dDebut = Timestamp.valueOf(dateTimeDebut);
            Timestamp dFin   = Timestamp.valueOf(dateTimeFin);

            // Vérifier si la salle est occupée
            if (estSalleOccupee(idSalle, dDebut, dFin, isModification ? (Vector<?>) rowData : null)) {
                JOptionPane.showMessageDialog(dialog, "Cette salle est déjà occupée pour cet intervalle de temps !");
                return;
            }

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                if (isModification) {
                    Vector<?> row = (Vector<?>) rowData;
                    Timestamp oldDebut = Timestamp.valueOf(row.get(4).toString());
                    Timestamp oldFin   = Timestamp.valueOf(row.get(5).toString());
                    PreparedStatement update = conn.prepareStatement(
                        "UPDATE emploi_du_temps SET " +
                        "idsalle=?, idprof=?, idclasse=?, cours=?, datedebut=?, datefin=? " +
                        "WHERE idsalle=? AND idprof=? AND idclasse=? AND datedebut=? AND datefin=?");
                    update.setInt(1, idSalle);
                    update.setInt(2, idProf);
                    update.setString(3, idClasse);
                    update.setString(4, cours);
                    update.setTimestamp(5, dDebut);
                    update.setTimestamp(6, dFin);
                    update.setInt(7,
                        Integer.parseInt(row.get(0).toString().split(" - ")[0]));
                    update.setInt(8,
                        Integer.parseInt(row.get(1).toString().split(" - ")[0]));
                    update.setString(9, row.get(2).toString().split(" - ")[0]);
                    update.setTimestamp(10, oldDebut);
                    update.setTimestamp(11, oldFin);
                    update.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Session modifiée avec succès !");
                } else {
                    PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO emploi_du_temps " +
                        "(idsalle, idprof, idclasse, cours, datedebut, datefin) " +
                        "VALUES (?,?,?,?,?,?)");
                    insert.setInt(1, idSalle);
                    insert.setInt(2, idProf);
                    insert.setString(3, idClasse);
                    insert.setString(4, cours);
                    insert.setTimestamp(5, dDebut);
                    insert.setTimestamp(6, dFin);
                    insert.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Session ajoutée avec succès !");
                }
                chargerEmplois();
                dialog.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Erreur lors de l'enregistrement : " + ex.getMessage());
            }
        });

        dialog.add(btnValider);
        dialog.add(btnAnnuler);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void chargerComboBox(JComboBox<String> comboBox, String query, String... fields) {
        comboBox.removeAllItems();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(query)) {
            while (rs.next()) {
                StringBuilder item = new StringBuilder(rs.getString(fields[0]));
                for (int i = 1; i < fields.length; i++) {
                    item.append(" - ").append(rs.getString(fields[i]));
                }
                comboBox.addItem(item.toString());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur de chargement des données pour liste déroulante !");
        }
    }

    private JButton creerBoutonAvecIcone(String texte, Color couleur, String nomFichierIcone) {
        String chemin = "images/" + nomFichierIcone;
        ImageIcon orig = new ImageIcon(chemin);
        Image img = orig.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(img);

        JButton btn = new JButton(texte, icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(couleur);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(couleur.darker(), 1, true));
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(10);
        return btn;
    }

    private JButton creerBoutonStylé(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(couleur);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(couleur.darker(), 1, true));
        return btn;
    }

    private boolean estSalleOccupee(int idSalle, Timestamp dateDebut, Timestamp dateFin, Vector<?> oldRow) {
        String sql = "SELECT COUNT(*) FROM emploi_du_temps WHERE idsalle = ? AND " +
                     "((datedebut <= ? AND datefin >= ?) OR " +
                     "(datedebut >= ? AND datedebut < ?))";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, idSalle);
            pst.setTimestamp(2, dateFin);  // Fin de la nouvelle session
            pst.setTimestamp(3, dateDebut); // Début de la nouvelle session
            pst.setTimestamp(4, dateDebut); // Début de la nouvelle session
            pst.setTimestamp(5, dateFin);   // Fin de la nouvelle session

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                // Si c'est une modification, exclure la session actuelle du compte
                if (oldRow != null) {
                    Timestamp oldDebut = Timestamp.valueOf(oldRow.get(4).toString());
                    Timestamp oldFin = Timestamp.valueOf(oldRow.get(5).toString());
                    if (idSalle == Integer.parseInt(oldRow.get(0).toString().split(" - ")[0]) &&
                        oldDebut.equals(dateDebut) && oldFin.equals(dateFin)) {
                        return false; // Pas de conflit si c'est la même session
                    }
                }
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Pilote PostgreSQL introuvable !");
                return;
            }
            new EmploiDuTempsFrame();
        });
    }
}