import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SalleListeFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JButton btnAjouter, btnModifier, btnSupprimer, btnRetour;

    private final String URL = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
    private final String USER = "postgres";
    private final String PASSWORD = "24268641";

    public SalleListeFrame() {
        setTitle("Liste des Salles");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        model = new DefaultTableModel(new String[]{"ID", "Désignation", "Occupation"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(51, 102, 255));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);

        btnAjouter = creerBoutonAvecIcone("Ajouter", new Color(0, 153, 76), "ajouter.png");
        btnModifier = creerBoutonAvecIcone("Modifier", new Color(51, 102, 255), "modifier.png");
        btnSupprimer = creerBoutonAvecIcone("Supprimer", new Color(204, 0, 0), "supprimer.png");
        btnRetour = creerBoutonAvecIcone("Retour", new Color(80, 80, 80), "retour.png");

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBoutons.setBackground(new Color(245, 245, 245));
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnRetour);

        add(scrollPane, BorderLayout.CENTER);
        add(panelBoutons, BorderLayout.SOUTH);

        chargerSalles();

        btnAjouter.addActionListener(e -> afficherFormulaireAjout());
        btnModifier.addActionListener(e -> afficherFormulaireModification());
        btnSupprimer.addActionListener(e -> supprimerSalle());
        btnRetour.addActionListener(e -> {
            dispose();
            new MainDashboardFrame();
        });

        setVisible(true);
    }

    private JButton creerBoutonAvecIcone(String texte, Color couleur, String nomIcone) {
        JButton bouton = new JButton(texte);
        styliserBouton(bouton, couleur);
        ImageIcon icone = chargerIcone(nomIcone, 20, 20);
        if (icone != null) bouton.setIcon(icone);
        return bouton;
    }

    private ImageIcon chargerIcone(String nomFichier, int largeur, int hauteur) {
        String chemin = "C:/Users/N I C K I/Pictures/Icones/" + nomFichier;
        ImageIcon icone = new ImageIcon(chemin);
        if (icone.getIconWidth() <= 0 || icone.getIconHeight() <= 0) return null;

        Image image = icone.getImage().getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private void styliserBouton(JButton btn, Color couleur) {
        btn.setBackground(couleur);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void chargerSalles() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String query = """
                SELECT 
                    s.idsalle, 
                    s.design,
                    CASE 
                        WHEN EXISTS (
                            SELECT 1
                            FROM emploi_du_temps edt
                            WHERE edt.idsalle = s.idsalle
                            AND edt.datedebut <= NOW()
                            AND edt.datefin >= NOW()
                        ) THEN 
                            'Occupé'
                        ELSE 
                            'Libre'
                    END AS occupation
                FROM salle s
                ORDER BY s.idsalle;
                """;

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("idsalle");
                String design = rs.getString("design");
                String occupation = rs.getString("occupation");
                model.addRow(new Object[]{id, design, occupation});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de chargement des salles !");
        }
    }

    private void afficherFormulaireAjout() {
        JDialog dialog = new JDialog(this, "Ajouter une salle", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));

        JLabel lblDesign = new JLabel("Désignation:");
        JTextField txtDesign = new JTextField();

        JButton btnValider = creerBoutonAvecIcone("Valider", new Color(0, 153, 76), "valider.png");
        JButton btnAnnuler = creerBoutonAvecIcone("Annuler", new Color(80, 80, 80), "annuler.png");

        dialog.add(lblDesign);
        dialog.add(txtDesign);
        dialog.add(btnValider);
        dialog.add(btnAnnuler);

        btnAnnuler.addActionListener(e -> dialog.dispose());

        btnValider.addActionListener(e -> {
            String design = txtDesign.getText().trim();
            if (design.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Veuillez remplir tous les champs !");
                return;
            }

            // Vérifier si la désignation existe déjà
            if (estDesignationExiste(design)) {
                JOptionPane.showMessageDialog(dialog, "Cette désignation existe déjà ! Veuillez en choisir une autre.");
                return;
            }

            int newId = genererProchainId();

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pst = conn.prepareStatement("INSERT INTO salle (idsalle, design, occupation) VALUES (?, ?, ?)")) {

                pst.setInt(1, newId);
                pst.setString(2, design);
                pst.setBoolean(3, false);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Salle ajoutée avec succès !");
                dialog.dispose();
                chargerSalles();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Erreur lors de l'ajout : " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void afficherFormulaireModification() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une salle à modifier !");
            return;
        }

        int id = (int) model.getValueAt(selectedRow, 0);
        String ancienDesign = (String) model.getValueAt(selectedRow, 1);

        JDialog dialog = new JDialog(this, "Modifier la salle", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));

        JLabel lblDesign = new JLabel("Désignation:");
        JTextField txtDesign = new JTextField(ancienDesign);

        JButton btnValider = creerBoutonAvecIcone("Valider", new Color(51, 102, 255), "valider.png");
        JButton btnAnnuler = creerBoutonAvecIcone("Annuler", new Color(80, 80, 80), "annuler.png");

        dialog.add(lblDesign);
        dialog.add(txtDesign);
        dialog.add(btnValider);
        dialog.add(btnAnnuler);

        btnAnnuler.addActionListener(e -> dialog.dispose());

        btnValider.addActionListener(e -> {
            String newDesign = txtDesign.getText().trim();
            if (newDesign.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Veuillez remplir tous les champs !");
                return;
            }

            // Vérifier si la nouvelle désignation existe déjà, sauf si c'est la même que l'ancienne
            if (!newDesign.equals(ancienDesign) && estDesignationExiste(newDesign)) {
                JOptionPane.showMessageDialog(dialog, "Cette désignation existe déjà ! Veuillez en choisir une autre.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pst = conn.prepareStatement("UPDATE salle SET design = ? WHERE idsalle = ?")) {

                pst.setString(1, newDesign);
                pst.setInt(2, id);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Salle modifiée avec succès !");
                dialog.dispose();
                chargerSalles();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Erreur lors de la modification : " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void supprimerSalle() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une salle à supprimer !");
            return;
        }

        int id = (int) model.getValueAt(selectedRow, 0);
        String designation = (String) model.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment supprimer la salle \"" + designation + "\" ?",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pst = conn.prepareStatement("DELETE FROM salle WHERE idsalle = ?")) {

                pst.setInt(1, id);
                int result = pst.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Salle supprimée avec succès !");
                    chargerSalles();
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur : salle introuvable !");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur lors de la suppression : " + ex.getMessage());
            }
        }
    }

    private int genererProchainId() {
        int id = 1;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String query = """
                SELECT MIN(s1.idsalle + 1) AS next_id
                FROM salle s1
                WHERE NOT EXISTS (
                    SELECT 1 FROM salle s2 WHERE s2.idsalle = s1.idsalle + 1
                );
                """;

            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                int result = rs.getInt("next_id");
                if (!rs.wasNull()) id = result;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    private boolean estDesignationExiste(String design) {
        boolean existe = false;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM salle WHERE design = ?")) {

            pst.setString(1, design);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                existe = rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return existe;
    }

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            SwingUtilities.invokeLater(SalleListeFrame::new);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Pilote PostgreSQL introuvable !");
        }
    }
}