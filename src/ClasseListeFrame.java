import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ClasseListeFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JButton btnAjouter, btnModifier, btnSupprimer, btnRetour;

    private final String URL = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
    private final String USER = "postgres";
    private final String PASSWORD = "24268641";

    private final String ICON_PATH = "images/";

    public ClasseListeFrame() {
        setTitle("Liste des Classes");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout(10, 10));

        model = new DefaultTableModel(new String[]{"ID Classe", "Niveau"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(30, 144, 255));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);

        btnAjouter = new JButton("Ajouter");
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        btnRetour = new JButton("Retour");

        styliserBoutonAvecIcone(btnAjouter, new Color(46, 204, 113), "ajouter.png");
        styliserBoutonAvecIcone(btnModifier, new Color(52, 152, 219), "modifier.png");
        styliserBoutonAvecIcone(btnSupprimer, new Color(231, 76, 60), "supprimer.png");
        styliserBoutonAvecIcone(btnRetour, new Color(149, 165, 166), "retour.png");

        JPanel panelBoutons = new JPanel();
        panelBoutons.setBackground(new Color(245, 245, 245));
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnRetour);

        add(scrollPane, BorderLayout.CENTER);
        add(panelBoutons, BorderLayout.SOUTH);

        chargerClasses();

        btnAjouter.addActionListener(e -> afficherFormulaireAjout());
        btnModifier.addActionListener(e -> afficherFormulaireModification());
        btnSupprimer.addActionListener(e -> supprimerClasse());
        btnRetour.addActionListener(e -> {
            dispose();
            new MainDashboardFrame(); // Assurez-vous que cette classe existe
        });

        setVisible(true);
    }

    private void styliserBoutonAvecIcone(JButton bouton, Color couleur, String nomFichierIcone) {
        bouton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bouton.setFocusPainted(false);
        bouton.setBackground(couleur);
        bouton.setForeground(Color.WHITE);
        bouton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setIcon(redimensionnerIcone(nomFichierIcone));
        bouton.setHorizontalAlignment(SwingConstants.LEFT);
        bouton.setIconTextGap(10);
    }

    private ImageIcon redimensionnerIcone(String nomFichier) {
        String cheminComplet = ICON_PATH + nomFichier;
        ImageIcon icon = new ImageIcon(cheminComplet);
        Image image = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private void chargerClasses() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM classe ORDER BY CAST(idclasse AS INTEGER) ASC")) {

            while (rs.next()) {
                String id = rs.getString("idclasse");
                String niveau = rs.getString("niveau");
                model.addRow(new Object[]{id, niveau});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de chargement des classes !");
        }
    }

    private void afficherFormulaireAjout() {
        JDialog dialog = new JDialog(this, "Ajouter une classe", true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.getContentPane().setBackground(new Color(245, 245, 245));

        JLabel lblId = new JLabel("ID Classe:");
        JTextField txtId = new JTextField();

        JLabel lblNiveau = new JLabel("Niveau:");
        JTextField txtNiveau = new JTextField();

        JButton btnValider = new JButton("Valider");
        JButton btnAnnuler = new JButton("Annuler");
        styliserBoutonAvecIcone(btnValider, new Color(46, 204, 113), "ajouter.png");
        styliserBoutonAvecIcone(btnAnnuler, new Color(149, 165, 166), "retour.png");

        dialog.add(lblId); dialog.add(txtId);
        dialog.add(lblNiveau); dialog.add(txtNiveau);
        dialog.add(btnValider); dialog.add(btnAnnuler);

        btnAnnuler.addActionListener(e -> dialog.dispose());

        btnValider.addActionListener(e -> {
            String id = txtId.getText().trim();
            String niveau = txtNiveau.getText().trim();

            // Vérification que l'ID est numérique
            if (!id.matches("\\d+")) {
                JOptionPane.showMessageDialog(dialog, "L'ID Classe doit être un nombre entier positif !");
                return;
            }

            // Vérification que les champs ne sont pas vides
            if (id.isEmpty() || niveau.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Veuillez remplir tous les champs !");
                return;
            }

            // Vérifier si l'ID de classe existe déjà
            if (estIdClasseExiste(id)) {
                JOptionPane.showMessageDialog(dialog, "Cet ID de classe existe déjà ! Veuillez en choisir un autre.");
                return;
            }

            // Vérifier si le niveau existe déjà
            if (estNiveauExiste(niveau)) {
                JOptionPane.showMessageDialog(dialog, "Ce niveau existe déjà ! Veuillez en choisir un autre.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pst = conn.prepareStatement("INSERT INTO classe (idclasse, niveau) VALUES (?, ?)")) {

                pst.setString(1, id);
                pst.setString(2, niveau);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Classe ajoutée avec succès !");
                dialog.dispose();
                chargerClasses();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Erreur lors de l'ajout !");
            }
        });

        dialog.setVisible(true);
    }

    private void afficherFormulaireModification() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une classe à modifier !");
            return;
        }

        String id = (String) model.getValueAt(selectedRow, 0);
        String ancienNiveau = (String) model.getValueAt(selectedRow, 1);

        JDialog dialog = new JDialog(this, "Modifier la classe", true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.getContentPane().setBackground(new Color(245, 245, 245));

        JLabel lblId = new JLabel("ID Classe:");
        JTextField txtId = new JTextField(id);
        txtId.setEnabled(false);

        JLabel lblNiveau = new JLabel("Niveau:");
        JTextField txtNiveau = new JTextField(ancienNiveau);

        JButton btnValider = new JButton("Valider");
        JButton btnAnnuler = new JButton("Annuler");
        styliserBoutonAvecIcone(btnValider, new Color(52, 152, 219), "modifier.png");
        styliserBoutonAvecIcone(btnAnnuler, new Color(149, 165, 166), "retour.png");

        dialog.add(lblId); dialog.add(txtId);
        dialog.add(lblNiveau); dialog.add(txtNiveau);
        dialog.add(btnValider); dialog.add(btnAnnuler);

        btnAnnuler.addActionListener(e -> dialog.dispose());

        btnValider.addActionListener(e -> {
            String newNiveau = txtNiveau.getText().trim();

            if (newNiveau.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Veuillez remplir le champ niveau !");
                return;
            }

            // Vérifier si le nouveau niveau existe déjà, sauf s'il est identique à l'ancien
            if (!newNiveau.equals(ancienNiveau) && estNiveauExiste(newNiveau)) {
                JOptionPane.showMessageDialog(dialog, "Ce niveau existe déjà ! Veuillez en choisir un autre.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pst = conn.prepareStatement("UPDATE classe SET niveau = ? WHERE idclasse = ?")) {

                pst.setString(1, newNiveau);
                pst.setString(2, id);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Classe modifiée avec succès !");
                dialog.dispose();
                chargerClasses();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Erreur lors de la modification !");
            }
        });

        dialog.setVisible(true);
    }

    private void supprimerClasse() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une classe à supprimer !");
            return;
        }

        String id = (String) model.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Confirmez-vous la suppression de la classe " + id + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pst = conn.prepareStatement("DELETE FROM classe WHERE idclasse = ?")) {

            pst.setString(1, id);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Classe supprimée avec succès !");
            chargerClasses();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression !");
        }
    }

    private boolean estIdClasseExiste(String id) {
        boolean existe = false;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM classe WHERE idclasse = ?")) {

            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                existe = rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return existe;
    }

    private boolean estNiveauExiste(String niveau) {
        boolean existe = false;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM classe WHERE niveau = ?")) {

            pst.setString(1, niveau);
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
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Pilote PostgreSQL introuvable !");
            return;
        }

        new ClasseListeFrame();
    }
}