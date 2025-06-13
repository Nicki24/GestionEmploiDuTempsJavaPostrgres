import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class RechercheEmploiDuTempsFrame extends JFrame {

    private JComboBox<String> classeComboBox;
    private JTable table;
    private DefaultTableModel model;

    private final String URL = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
    private final String USER = "postgres";
    private final String PASSWORD = "24268641";

    public RechercheEmploiDuTempsFrame() {
        setTitle("Recherche Emploi du Temps par Classe");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(950, 450);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(0xF5F5F5));
        setLayout(new BorderLayout(10, 10));

        // Panneau supérieur
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(0xF5F5F5));

        JLabel classeLabel = new JLabel("Classe :");
        classeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        topPanel.add(classeLabel);

        classeComboBox = new JComboBox<>();
        classeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        classeComboBox.setPreferredSize(new Dimension(250, 28));
        topPanel.add(classeComboBox);

        JButton rechercherButton = new JButton("Rechercher");
        styliserBoutonAvecIcone(rechercherButton, new Color(0x2196F3), "images\\rechercher.png");
        topPanel.add(rechercherButton);

        JButton retourButton = new JButton("Retour");
        styliserBoutonAvecIcone(retourButton, new Color(0x9E9E9E), "images\\retour.png");
        topPanel.add(retourButton);

        add(topPanel, BorderLayout.NORTH);

        // Tableau
        model = new DefaultTableModel(new String[]{"Salle", "Professeur", "Classe", "Cours", "Date Début", "Date Fin"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0x2196F3));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Charger les classes
        chargerClasses();

        // Actions
        rechercherButton.addActionListener(e -> rechercherEmploiDuTemps());
        retourButton.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void styliserBoutonAvecIcone(JButton bouton, Color couleur, String cheminIcone) {
        bouton.setBackground(couleur);
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);
        bouton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setPreferredSize(new Dimension(170, 40));
        bouton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        bouton.setOpaque(true);
        bouton.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));

        try {
            ImageIcon icon = new ImageIcon(cheminIcone);
            Image imageRedimensionnee = icon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            bouton.setIcon(new ImageIcon(imageRedimensionnee));
            bouton.setHorizontalTextPosition(SwingConstants.RIGHT);
            bouton.setIconTextGap(10);
        } catch (Exception e) {
            System.err.println("Erreur chargement icône : " + cheminIcone);
        }
    }

    private void chargerClasses() {
        String query = "SELECT idclasse, niveau FROM CLASSE ORDER BY CAST(idclasse AS INTEGER) ASC";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                classeComboBox.addItem(rs.getString("idclasse") + " - " + rs.getString("niveau"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des classes : " + e.getMessage());
        }
    }

    private void rechercherEmploiDuTemps() {
        model.setRowCount(0);

        String classeSelectionnee = (String) classeComboBox.getSelectedItem();
        if (classeSelectionnee == null || classeSelectionnee.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une classe !");
            return;
        }

        String idClasse = classeSelectionnee.split(" - ")[0];

        String query = """
            SELECT s.design AS Salle, 
                   p.nom || ' ' || p.prénoms AS Professeur,
                   c.niveau AS Classe,
                   edt.cours AS Cours,
                   edt.datedebut AS DateDebut,
                   edt.datefin AS DateFin
            FROM emploi_du_temps edt
            JOIN salle s ON edt.idsalle = s.idsalle
            JOIN professeur p ON edt.idprof = p.idprof
            JOIN classe c ON edt.idclasse = c.idclasse
            WHERE edt.idclasse = ?
            ORDER BY edt.datedebut ASC
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, idClasse);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("Salle"),
                            rs.getString("Professeur"),
                            rs.getString("Classe"),
                            rs.getString("Cours"),
                            rs.getTimestamp("DateDebut").toString(),
                            rs.getTimestamp("DateFin").toString()
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la recherche : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Class.forName("org.postgresql.Driver");
                new RechercheEmploiDuTempsFrame();
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Pilote PostgreSQL introuvable !");
            }
        });
    }
}