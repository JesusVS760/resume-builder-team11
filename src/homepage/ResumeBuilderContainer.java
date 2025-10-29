package homepage;

import ui.LandingPanel;
import ui.UploadPanel;
import ui.widgets.HoverScaleButton;

import javax.swing.*;
import java.awt.*;

public class ResumeBuilderContainer extends JFrame {

    private static final Color NEW_BLUE = new Color(0x374151); // pick your shade
    private static final Color DARK_TEXT  = Color.BLACK;

    private JPanel rootPanel;
    private JPanel navigationPanel;

    // Right Side
    private JPanel contentPanel;
    private JPanel buildResumePanel;
    private JPanel savedResumesPanel;
    private JPanel faqPanel;
    private JPanel settingsPanel;
    private JPanel profilePanel;
    private JPanel homePanel;

    // Navigation Buttons
    private JButton buildResumeButton;
    private JButton savedResumesButton;
    private JButton FAQButton;
    private JButton settingsButton;
    private JButton profileButton;
    private JButton resumeBuilderButton;

    // Profile panel components
    private JLabel emailValueLabel;
    private JLabel nameValueLabel;
    private JButton logoutButton;

    private CardLayout cards;

    public ResumeBuilderContainer() {
        super("Resume Builder");

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        $$$setupUI$$$();                // build UI structure
        setContentPane(rootPanel);
        setResizable(false);

        // consistent card background
        setCardBackgrounds(Color.GRAY);

        // plug real content into cards
        homePanel.setLayout(new BorderLayout());
        homePanel.removeAll();
        homePanel.add(new LandingPanel("src/homepage/images/landing_hero.png"), BorderLayout.CENTER);

        buildResumePanel.setLayout(new BorderLayout());
        buildResumePanel.removeAll();
        buildResumePanel.add(new UploadPanel(), BorderLayout.CENTER);

        // wiring
        cards = (CardLayout) contentPanel.getLayout();
        showCard("HOME");

        // profile icon
        ImageIcon profileIcon = loadScaledIcon("/homepage/images/profilePic.png",
                "src/homepage/images/profilePic.png", 24, 24);
        if (profileIcon != null) profileButton.setIcon(profileIcon);

        styleNavButtons();
        updateAuthUI();

        // navigation
        resumeBuilderButton.addActionListener(e -> showCard("HOME"));
        buildResumeButton.addActionListener(e -> showCard("BUILD"));
        savedResumesButton.addActionListener(e -> showCard("SAVED"));
        FAQButton.addActionListener(e -> showCard("FAQ"));
        settingsButton.addActionListener(e -> showCard("SETTINGS"));
        profileButton.addActionListener(e -> {
            if (utils.Constants.Session.isLoggedIn()) {
                updateProfilePanel();
                showCard("PROFILE");
            } else {
                new ui.LoginFrame().setVisible(true);
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);

        // focus hook to auto-open profile after login
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {
                if (utils.Constants.Session.justLoggedIn()) {
                    updateProfilePanel();
                    updateAuthUI();
                    showCard("PROFILE");
                }
            }
            @Override public void windowLostFocus(java.awt.event.WindowEvent e) { }
        });
    }

    private void styleNavButtons() {
        for (JButton b : new JButton[]{ resumeBuilderButton, buildResumeButton, savedResumesButton, FAQButton, settingsButton, profileButton }) {
            if (b == null) continue;
            b.setBackground(NEW_BLUE);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
        }
    }

    private void showCard(String name) {
        cards.show(contentPanel, name);
    }

    private void setCardBackgrounds(Color bg) {
        JPanel[] cardsArr = { homePanel, buildResumePanel, savedResumesPanel, faqPanel, settingsPanel, profilePanel };
        for (JPanel p : cardsArr) {
            if (p != null) { p.setBackground(bg); p.setOpaque(true); }
        }
    }

    // --- Helpers ---
    private ImageIcon loadScaledIcon(String classpath, String fileFallback, int w, int h) {
        try {
            java.net.URL url = ResumeBuilderContainer.class.getResource(classpath);
            if (url == null) {
                java.io.File f = new java.io.File(fileFallback);
                if (f.exists()) url = f.toURI().toURL();
            }
            if (url == null) return null;
            Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- UI build ----------
    private void $$$setupUI$$$() {
        rootPanel = new JPanel(new BorderLayout());
        rootPanel.setPreferredSize(new Dimension(795, 538));

        // left nav
        navigationPanel = new JPanel(new GridLayout(6, 1, 0, 12));
        navigationPanel.setBackground(new Color(0x1f2937));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        navigationPanel.setPreferredSize(new Dimension(220, 0));
        rootPanel.add(navigationPanel, BorderLayout.WEST);

        // nav buttons (with hover scale)
        resumeBuilderButton = new HoverScaleButton("Home");
        buildResumeButton   = new HoverScaleButton("Build Resume");
        savedResumesButton  = new HoverScaleButton("Saved Resumes");
        FAQButton           = new HoverScaleButton("FAQ");
        settingsButton      = new HoverScaleButton("Settings");
        profileButton       = new HoverScaleButton("Login");

        navigationPanel.add(resumeBuilderButton);
        navigationPanel.add(buildResumeButton);
        navigationPanel.add(savedResumesButton);
        navigationPanel.add(FAQButton);
        navigationPanel.add(settingsButton);
        navigationPanel.add(profileButton);

        // center cards
        contentPanel = new JPanel(new CardLayout());
        rootPanel.add(contentPanel, BorderLayout.CENTER);

        // cards (simple placeholders; replaced in ctor)
        homePanel         = createCardPanel("Landing Page");
        buildResumePanel  = createCardPanel("Drag and Drop");
        savedResumesPanel = createCardPanel("Your saved resumes");
        faqPanel          = createCardPanel("This is where questions will be answered");
        settingsPanel     = createCardPanel("Settings");
        profilePanel      = createProfilePanel();

        contentPanel.add(homePanel, "HOME");
        contentPanel.add(buildResumePanel, "BUILD");
        contentPanel.add(savedResumesPanel, "SAVED");
        contentPanel.add(faqPanel, "FAQ");
        contentPanel.add(settingsPanel, "SETTINGS");
        contentPanel.add(profilePanel, "PROFILE");
    }

    private void updateAuthUI() {
        boolean loggedIn = utils.Constants.Session.isLoggedIn();
        profileButton.setText(loggedIn ? "Profile" : "Login");
    }

    private JPanel createCardPanel(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.GRAY);
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.NORTH);
        panel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.GRAY);

        JLabel titleLabel = new JLabel("User Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        infoPanel.setBackground(Color.GRAY);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(Color.WHITE);
        emailValueLabel = new JLabel("Not logged in");
        emailValueLabel.setForeground(Color.WHITE);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(Color.WHITE);
        nameValueLabel = new JLabel("Not logged in");
        nameValueLabel.setForeground(Color.WHITE);

        infoPanel.add(emailLabel); infoPanel.add(emailValueLabel);
        infoPanel.add(nameLabel);  infoPanel.add(nameValueLabel);

        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setOpaque(true);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        logoutButton.addActionListener(e -> {
            utils.Constants.Session.logout();
            JOptionPane.showMessageDialog(this, "You have been logged out successfully.",
                    "Logout", JOptionPane.INFORMATION_MESSAGE);
            updateProfilePanel();
            updateAuthUI();
            showCard("HOME");
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.GRAY);
        buttonPanel.add(logoutButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void updateProfilePanel() {
        if (utils.Constants.Session.isLoggedIn()) {
            models.User user = utils.Constants.Session.getCurrentUser();
            emailValueLabel.setText(user.getEmail());
            nameValueLabel.setText(user.getName());
            logoutButton.setVisible(true);
        } else {
            emailValueLabel.setText("Not logged in");
            nameValueLabel.setText("Not logged in");
            logoutButton.setVisible(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ResumeBuilderContainer().setVisible(true));
    }
}
