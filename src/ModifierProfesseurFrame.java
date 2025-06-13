import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ModifierProfesseurFrame extends JFrame {

    private JTextField nomField;
    private JTextField prenomsField;
    private JComboBox<String> gradeComboBox;
    private int idProf;

    public ModifierProfesseurFrame(int idProf) {
        this.idProf = idProf;

        setTitle("Modifier un Professeur");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(0xF5F5F5));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nom
        JLabel nomLabel = new JLabel("Nom :");
        nomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0;
        add(nomLabel, gbc);

        nomField = new JTextField(20);
        nomField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        add(nomField, gbc);

        // Prénoms
        JLabel prenomsLabel = new JLabel("Prénoms :");
        prenomsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1;
        add(prenomsLabel, gbc);

        prenomsField = new JTextField(20);
        prenomsField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        add(prenomsField, gbc);

        // Grade
        JLabel gradeLabel = new JLabel("Grade :");
        gradeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 2;
        add(gradeLabel, gbc);

        gradeComboBox = new JComboBox<>(new String[]{
            "Professeur titulaire",
            "Maître de Conférences",
            "Assistant d’Enseignement Supérieur et de Recherche",
            "Docteur HDR",
            "Docteur en Informatique",
            "Doctorant en informatique"
        });
        gradeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        add(gradeComboBox, gbc);

        // Bouton Modifier
        JButton saveButton = new JButton("Modifier");
        styliserBouton(saveButton, new Color(0x4CAF50)); // Vert
        saveButton.addActionListener(e -> modifierProfesseur());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(saveButton, gbc);

        // Charger les données
        chargerDonneesProfesseur();

        setSize(500, 280);
        setLocationRelativeTo(null);
    }

    private void styliserBouton(JButton bouton, Color couleur) {
        bouton.setBackground(couleur);
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);
        bouton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        bouton.setOpaque(true);
        bouton.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));
    }

    private void chargerDonneesProfesseur() {
        String url = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
        String user = "postgres";
        String password = "24268641";

        String query = "SELECT Nom, Prénoms, Grade FROM PROFESSEUR WHERE idprof = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idProf);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nomField.setText(rs.getString("Nom"));
                    prenomsField.setText(rs.getString("Prénoms"));
                    gradeComboBox.setSelectedItem(rs.getString("Grade"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des données : " + e.getMessage());
        }
    }

    private void modifierProfesseur() {
        String nom = nomField.getText().trim();
        String prenoms = prenomsField.getText().trim();

        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le champ 'Nom' est obligatoire. Veuillez le remplir !");
            return;
        }

        if (prenoms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le champ 'Prénoms' est vide. Veuillez le remplir !");
            return;
        }

        String url = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
        String user = "postgres";
        String password = "24268641";

        String sql = "UPDATE PROFESSEUR SET Nom = ?, Prénoms = ?, Grade = ? WHERE idprof = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nom);
            pstmt.setString(2, prenoms);
            pstmt.setString(3, gradeComboBox.getSelectedItem().toString());
            pstmt.setInt(4, idProf);

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Modification réussie !");
                dispose();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la modification : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ModifierProfesseurFrame frame = new ModifierProfesseurFrame(1);
            frame.setVisible(true);
        });
    }
}
