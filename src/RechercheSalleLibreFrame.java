import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import com.github.lgooddatepicker.components.DateTimePicker;

public class RechercheSalleLibreFrame extends JFrame {

    private final String URL = "jdbc:postgresql://localhost:5432/gestion_emploi_du_temps";
    private final String USER = "postgres";
    private final String PASSWORD = "24268641";

    private DateTimePicker dateTimePicker;
    private DefaultTableModel model;
    private JTable table;

    public RechercheSalleLibreFrame() {
        setTitle("Recherche de salles libres");
        setSize(750, 420);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(0xF5F5F5));
        setLayout(new BorderLayout(10, 10));

        // --- Haut : sélection de l'heure
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTop.setBackground(new Color(0xF5F5F5));

        JLabel label = new JLabel("Choisir une date et une heure : ");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelTop.add(label);

        dateTimePicker = new DateTimePicker();
        dateTimePicker.setDateTimeStrict(LocalDateTime.now());
        dateTimePicker.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelTop.add(dateTimePicker);

        JButton btnRechercher = new JButton("Rechercher");
        styliserBoutonAvecIcone(btnRechercher, new Color(0x2196F3), "images\\rechercher.png");
        panelTop.add(btnRechercher);

        JButton btnRetour = new JButton("Retour");
        styliserBoutonAvecIcone(btnRetour, new Color(0x9E9E9E), "images\\retour.png");
        panelTop.add(btnRetour);

        add(panelTop, BorderLayout.NORTH);

        // --- Centre : tableau de résultat
        model = new DefaultTableModel(new String[]{"ID Salle", "Désignation"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0x2196F3));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- Actions
        btnRechercher.addActionListener(e -> rechercherSallesLibres());
        btnRetour.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void styliserBoutonAvecIcone(JButton bouton, Color couleur, String cheminIcone) {
        bouton.setBackground(couleur);
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);
        bouton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setPreferredSize(new Dimension(160, 40));
        bouton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        bouton.setOpaque(true);
        bouton.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));

        try {
            ImageIcon icon = new ImageIcon(cheminIcone);
            Image resized = icon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            bouton.setIcon(new ImageIcon(resized));
            bouton.setHorizontalTextPosition(SwingConstants.RIGHT);
            bouton.setIconTextGap(10);
        } catch (Exception e) {
            System.err.println("Erreur chargement icône : " + cheminIcone);
        }
    }

    private void rechercherSallesLibres() {
        model.setRowCount(0); // Vider le tableau

        LocalDateTime selectedDateTime = dateTimePicker.getDateTimeStrict();
        if (selectedDateTime == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une date et une heure valides !");
            return;
        }
        Timestamp timestamp = Timestamp.valueOf(selectedDateTime);

        String query = """
            SELECT s.idsalle, s.design
            FROM salle s
            WHERE NOT EXISTS (
                SELECT 1 FROM emploi_du_temps edt
                WHERE edt.idsalle = s.idsalle
                AND ? BETWEEN edt.datedebut AND edt.datefin
            )
            ORDER BY s.idsalle
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setTimestamp(1, timestamp);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("idsalle");
                String design = rs.getString("design");
                model.addRow(new Object[]{id, design});
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Aucune salle libre à cette heure.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la recherche des salles libres !");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Class.forName("org.postgresql.Driver");
                new RechercheSalleLibreFrame();
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Pilote PostgreSQL introuvable !");
            }
        });
    }
}