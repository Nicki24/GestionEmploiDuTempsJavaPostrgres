import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ProfesseurListFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    public ProfesseurListFrame() {
        setTitle("Liste des Professeurs");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xF5F5F5)); // Fond clair

        // Modèle pour le tableau
        tableModel = new DefaultTableModel(new String[]{"ID", "Nom", "Prénoms", "Grade"}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(0x2196F3));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((DefaultTableCellRenderer) table.getDefaultRenderer(Object.class)).setHorizontalAlignment(JLabel.CENTER);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Charger les données
        chargerProfesseurs();

        // Boutons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBackground(new Color(0xF5F5F5));
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

        JButton addButton = new JButton("Ajouter");
        JButton editButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        JButton backButton = new JButton("Retour");

        // Appliquer style et icônes
        styliserBouton(addButton, new Color(0x4CAF50), "ajouter.png");
        styliserBouton(editButton, new Color(0x2196F3), "modifier.png");
        styliserBouton(deleteButton, new Color(0xF44336), "supprimer.png");
        styliserBouton(backButton, new Color(0x9E9E9E), "retour.png");

        // Définir une taille préférée
        Dimension buttonSize = new Dimension(150, 40);
        addButton.setPreferredSize(buttonSize);
        editButton.setPreferredSize(buttonSize);
        deleteButton.setPreferredSize(buttonSize);
        backButton.setPreferredSize(buttonSize);

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(backButton);

        add(buttonsPanel, BorderLayout.SOUTH);

        // Action pour Ajouter
        addButton.addActionListener(e -> {
            AjouterProfesseurFrame ajouterProfesseurFrame = new AjouterProfesseurFrame();
            ajouterProfesseurFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    actualiserTable();
                }
            });
            ajouterProfesseurFrame.setVisible(true);
        });

        // Action pour Modifier
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner le professeur à modifier.");
                return;
            }
            int idProf = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            ModifierProfesseurFrame modifierProfesseurFrame = new ModifierProfesseurFrame(idProf);
            modifierProfesseurFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    actualiserTable();
                }
            });
            modifierProfesseurFrame.setVisible(true);
        });

        // Action pour Supprimer
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner le professeur à supprimer.");
                return;
            }
            int idProf = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Voulez-vous vraiment supprimer ce professeur ?",
                    "Confirmation de suppression",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                supprimerProfesseur(idProf);
                actualiserTable();
            }
        });

        // Action pour Retour
        backButton.addActionListener(e -> dispose());

        // Taille de la fenêtre
        setSize(750, 480);
        setLocationRelativeTo(null);
    }

    private void styliserBouton(JButton bouton, Color couleur, String iconName) {
        bouton.setBackground(couleur);
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);
        bouton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setOpaque(true);
        bouton.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));

        // Ajouter l'icône redimensionnée
        String iconPath = "C:/Users/N I C K I/Pictures/Icones/" + iconName;
        ImageIcon originalIcon = new ImageIcon(iconPath);
        Image scaledImage = originalIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        bouton.setIcon(new ImageIcon(scaledImage));
        bouton.setHorizontalAlignment(SwingConstants.LEFT);
        bouton.setIconTextGap(10);
    }

    private void chargerProfesseurs() {
        String url = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
        String user = "postgres";
        String password = "24268641";
        String query = "SELECT * FROM PROFESSEUR ORDER BY idprof ASC";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("idprof"),
                        rs.getString("Nom"),
                        rs.getString("Prénoms"),
                        rs.getString("Grade")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des professeurs : " + e.getMessage());
        }
    }

    private void supprimerProfesseur(int idProf) {
        String url = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
        String user = "postgres";
        String password = "24268641";
        String sql = "DELETE FROM PROFESSEUR WHERE idprof = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProf);
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Professeur supprimé avec succès !");
            } else {
                JOptionPane.showMessageDialog(this, "Erreur : le professeur n'a pas pu être supprimé.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression : " + e.getMessage());
        }
    }

    private void actualiserTable() {
        tableModel.setRowCount(0);
        chargerProfesseurs();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProfesseurListFrame frame = new ProfesseurListFrame();
            frame.setVisible(true);
        });
    }
}
