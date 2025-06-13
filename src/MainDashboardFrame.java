import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

// Centralized theme configuration class
class ThemeConfig {
    static final Color LIGHT_BG = new Color(240, 242, 245);
    static final Color DARK_BG = new Color(45, 45, 45);
    static final Color LIGHT_CARD = Color.WHITE;
    static final Color DARK_CARD = new Color(60, 63, 65);
    static final Color SIDEBAR_BG = new Color(60, 63, 65);
    static final Color BUTTON_BG = new Color(75, 78, 80);
    static final Color BUTTON_HOVER_BG = new Color(90, 93, 95);
    static final Color TOPBAR_BG = new Color(45, 85, 155);
    static final Color TOPBAR_BUTTON_BG = new Color(30, 65, 125);
    static final Color CARD_HOVER_BG = new Color(230, 240, 250);
}

public class MainDashboardFrame extends JFrame {

    private boolean isDarkTheme = false;
    private JPanel sideMenu;
    private JPanel topBar;
    private JPanel centerPanel;

    public MainDashboardFrame() {
        setTitle("Dashboard - Gestion Scolaire Moderne");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top bar
        topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ThemeConfig.TOPBAR_BG);
        topBar.setPreferredSize(new Dimension(0, 60)); // Width will adjust dynamically

        JLabel title = new JLabel("Tableau de bord - Emploi du temps des professeurs");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(10, 20, 10, 20)); // Standardized padding
        topBar.add(title, BorderLayout.WEST);

        JButton themeToggleButton = new JButton("Thème");
        themeToggleButton.setFocusPainted(false);
        themeToggleButton.setForeground(Color.WHITE);
        themeToggleButton.setBackground(ThemeConfig.TOPBAR_BUTTON_BG);
        themeToggleButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        themeToggleButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        themeToggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeToggleButton.setToolTipText("Basculer entre thème clair et sombre");
        themeToggleButton.addActionListener(e -> toggleTheme());
        topBar.add(themeToggleButton, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.setFocusPainted(false);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(ThemeConfig.TOPBAR_BUTTON_BG);
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setToolTipText("Fermer l'application");
        logoutButton.addActionListener(e -> dispose()); // Close the window
        topBar.add(logoutButton, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Sidebar
        sideMenu = new JPanel();
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setPreferredSize(new Dimension(280, 0)); // Adjusted for dynamic height
        updateSidebarBackground();

        sideMenu.add(Box.createVerticalStrut(80));
        sideMenu.add(createLogoPanel());
        sideMenu.add(Box.createVerticalStrut(-150));

        addMenuButton(sideMenu, "Recherche Emploi du Temps", "images/room.png", e -> new RechercheEmploiDuTempsFrame().setVisible(true), KeyEvent.VK_E);
        addMenuButton(sideMenu, "Recherche Salle Libre", "images/salle.png", e -> new RechercheSalleLibreFrame().setVisible(true), KeyEvent.VK_S);
        addMenuButton(sideMenu, "PDF Emploi du Temps", "images/pdf.png", e -> new ExportPDFFrame().setVisible(true), KeyEvent.VK_P);
        sideMenu.add(Box.createVerticalGlue());

        add(sideMenu, BorderLayout.WEST);

        // Centre
        centerPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        centerPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        updateCenterBackground();

        centerPanel.add(createCard("Salles", "images/salle.png", e -> new SalleListeFrame().setVisible(true)));
        centerPanel.add(createCard("Classes", "images/classe.png", e -> new ClasseListeFrame().setVisible(true)));
        centerPanel.add(createCard("Professeurs", "images/prof.png", e -> new ProfesseurListFrame().setVisible(true)));
        centerPanel.add(createCard("Emploi du Temps", "images/edt.png", e -> new EmploiDuTempsFrame().setVisible(true)));

        add(centerPanel, BorderLayout.CENTER);

        pack(); // Adjust window size based on content
        setLocationRelativeTo(null); // Center on screen
    }

    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        updateSidebarBackground();
        updateCenterBackground();
        System.out.println("Theme toggled to: " + (isDarkTheme ? "Dark" : "Light"));
        repaint();
    }

    private void updateSidebarBackground() {
        sideMenu.setBackground(isDarkTheme ? ThemeConfig.DARK_CARD : ThemeConfig.SIDEBAR_BG);
    }

    private void updateCenterBackground() {
        centerPanel.setBackground(isDarkTheme ? ThemeConfig.DARK_BG : ThemeConfig.LIGHT_BG);
    }

    private void addMenuButton(JPanel parent, String text, String iconPath, ActionListener action, int mnemonic) {
        JButton btn = new JButton("<html>" + text + "</html>");
        btn.setPreferredSize(new Dimension(240, 50));
        btn.setMaximumSize(new Dimension(240, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(ThemeConfig.BUTTON_BG);
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setIconTextGap(10);
        btn.setToolTipText(text);
        btn.setMnemonic(mnemonic); // Add keyboard mnemonic

        try {
            ImageIcon icon = new ImageIcon(iconPath);
            Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            System.out.println("Failed to load icon for " + text + ": " + e.getMessage());
            btn.setIcon(new ImageIcon(new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB))); // Fallback empty icon
        }

        btn.addActionListener(action);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ThemeConfig.BUTTON_HOVER_BG);
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(ThemeConfig.BUTTON_BG);
            }
        });

        parent.add(Box.createVerticalStrut(12));
        parent.add(btn);
    }

    private JPanel createCard(String title, String iconPath, ActionListener action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(isDarkTheme ? ThemeConfig.DARK_CARD : ThemeConfig.LIGHT_CARD);
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(iconPath);
            Image img = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            System.out.println("Failed to load icon for card " + title + ": " + e.getMessage());
            iconLabel.setText("❔");
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        }
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel(title);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel.setBorder(new EmptyBorder(10, 15, 10, 15)); // Standardized padding

        card.add(Box.createVerticalGlue());
        card.add(iconLabel);
        card.add(textLabel);
        card.add(Box.createVerticalGlue());
        card.setToolTipText("Ouvrir " + title);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(null);
            }

            public void mouseEntered(MouseEvent e) {
                card.setBackground(ThemeConfig.CARD_HOVER_BG);
            }

            public void mouseExited(MouseEvent e) {
                card.setBackground(isDarkTheme ? ThemeConfig.DARK_CARD : ThemeConfig.LIGHT_CARD);
            }
        });

        return card;
    }

    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(ThemeConfig.SIDEBAR_BG);
        logoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        try {
            BufferedImage image = ImageIO.read(new File("images/ENI.jpg"));
            BufferedImage circular = createCircularImage(image, 150);
            JLabel picLabel = new JLabel(new ImageIcon(circular));
            logoPanel.add(picLabel);
        } catch (Exception e) {
            System.out.println("Failed to load logo: " + e.getMessage());
            logoPanel.add(new JLabel("LOGO"));
        }

        return logoPanel;
    }

    private BufferedImage createCircularImage(BufferedImage image, int size) {
        int diameter = Math.min(image.getWidth(), image.getHeight());
        BufferedImage mask = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = mask.createGraphics();
        g2d.fillOval(0, 0, diameter, diameter);
        g2d.dispose();

        BufferedImage cropped = image.getSubimage(0, 0, diameter, diameter);
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setClip(new Ellipse2D.Float(0, 0, size, size));
        g.drawImage(cropped, 0, 0, size, size, null);
        g.dispose();
        return scaled;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainDashboardFrame().setVisible(true));
    }
}