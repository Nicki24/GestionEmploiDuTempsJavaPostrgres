import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AjouterProfesseurFrame extends JFrame {

    private JTextField nomField;
    private JTextField prenomsField;
    private JComboBox<String> gradeComboBox;

    public AjouterProfesseurFrame() {
        setTitle("Ajouter un Professeur");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(0xF5F5F5)); // Fond clair

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel nomLabel = new JLabel("Nom :");
        nomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0;
        add(nomLabel, gbc);

        nomField = new JTextField(20); // largeur réduite
        nomField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        add(nomField, gbc);

        JLabel prenomsLabel = new JLabel("Prénoms :");
        prenomsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1;
        add(prenomsLabel, gbc);

        prenomsField = new JTextField(20);
        prenomsField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        add(prenomsField, gbc);

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
        gradeComboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXX"); // pour forcer une largeur suffisante
        gbc.gridx = 1;
        add(gradeComboBox, gbc);

        JButton addButton = new JButton("Ajouter");
        styliserBouton(addButton, new Color(0x4CAF50)); // Vert
        addButton.addActionListener(e -> ajouterProfesseur());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(addButton, gbc);

        setSize(500, 300); // Ajusté pour éviter de couper le texte
        setLocationRelativeTo(null);
    }

    private void styliserBouton(JButton bouton, Color couleur) {
        bouton.setBackground(couleur);
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);
        bouton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        bouton.setOpaque(true);
        bouton.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));
    }

    private void ajouterProfesseur() {
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
        String sqlInsert = "INSERT INTO PROFESSEUR (idprof, Nom, Prénoms, Grade) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            int id = trouverPlusPetitIdDisponible(conn);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, nom);
                pstmt.setString(3, prenoms);
                pstmt.setString(4, gradeComboBox.getSelectedItem().toString());

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Professeur ajouté avec succès !");
                    nomField.setText("");
                    prenomsField.setText("");
                    gradeComboBox.setSelectedIndex(0);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    private int trouverPlusPetitIdDisponible(Connection conn) throws SQLException {
        String sql = "SELECT MIN(idprof) + 1 FROM PROFESSEUR WHERE idprof + 1 NOT IN (SELECT idprof FROM PROFESSEUR)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                Integer id = rs.getInt(1);
                if (rs.wasNull()) {
                    String sqlMaxId = "SELECT COALESCE(MAX(idprof), 0) + 1 FROM PROFESSEUR";
                    try (PreparedStatement pstmtMax = conn.prepareStatement(sqlMaxId);
                         ResultSet rsMax = pstmtMax.executeQuery()) {

                        if (rsMax.next()) {
                            return rsMax.getInt(1);
                        }
                    }
                } else {
                    return id;
                }
            }
        }
        return 1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AjouterProfesseurFrame frame = new AjouterProfesseurFrame();
            frame.setVisible(true);
        });
    }
}
