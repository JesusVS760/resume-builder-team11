package homepage;

import ui.LandingPanel;
import ui.UploadPanel;
import ui.widgets.HoverScaleButton;

import javax.swing.*;
import java.awt.*;

public class ResumeBuilderContainer extends JFrame {

    private static final Color NEW_BLUE = new Color(0x374151); // pick your shade

    private java.util.Map<String, HoverScaleButton> navButtons;
    private final java.util.List<String> CARD_ORDER =
            java.util.List.of("HOME", "BUILD", "SAVED", "SETTINGS", "PROFILE");

    private JPanel rootPanel;

    // Right Side
    private ui.widgets.AnimatedCards contentPanel;
    private JPanel buildResumePanel;
    private JPanel savedResumesPanel;
    private JPanel settingsPanel;
    private JPanel profilePanel;
    private JPanel homePanel;

    // Navigation Buttons
    private JButton buildResumeButton;
    private JButton savedResumesButton;
    private JButton settingsButton;
    private JButton profileButton;
    private JButton resumeBuilderButton;

    // Profile panel components
    private JLabel emailValueLabel;
    private JLabel nameValueLabel;
    private JButton logoutButton;

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

        // profile icon
        ImageIcon profileIcon = loadScaledIcon("/homepage/images/profilePic.png",
                "src/homepage/images/profilePic.png", 24, 24);
        if (profileIcon != null) profileButton.setIcon(profileIcon);

        contentPanel.instantShow("HOME");
        styleNavButtons();
        setActiveNav("HOME");
        updateAuthUI();

        // navigation
        resumeBuilderButton.addActionListener(e -> go("HOME"));
        buildResumeButton.addActionListener(e -> go("BUILD"));
        savedResumesButton.addActionListener(e -> go("SAVED"));
        settingsButton.addActionListener(e -> go("SETTINGS"));
        profileButton.addActionListener(e -> {
            if (utils.Constants.Session.isLoggedIn()) {
                updateProfilePanel();
                updateAuthUI();
                go("PROFILE");                 // use animated nav
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
                    go("PROFILE");
                }
            }
            @Override public void windowLostFocus(java.awt.event.WindowEvent e) { }
        });
    }

    private void styleNavButtons() {
        for (JButton b : new JButton[]{ resumeBuilderButton, buildResumeButton, savedResumesButton, settingsButton, profileButton }) {
            if (b == null) continue;
            b.setBackground(NEW_BLUE);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
        }
    }

    private void setActiveNav(String key) {
        if (navButtons == null) return;
        for (HoverScaleButton b : navButtons.values()) b.setSticky(false);
        HoverScaleButton active = navButtons.get(key);
        if (active != null) active.setSticky(true);
    }


    private void setCardBackgrounds(Color bg) {
        JPanel[] cardsArr = { homePanel, buildResumePanel, savedResumesPanel, settingsPanel, profilePanel };
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
        JPanel navigationPanel = new JPanel(new GridLayout(5, 1, 0, 12));
        navigationPanel.setBackground(new Color(0x1f2937));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        navigationPanel.setPreferredSize(new Dimension(220, 0));
        rootPanel.add(navigationPanel, BorderLayout.WEST);

        // nav buttons
        resumeBuilderButton = new HoverScaleButton("Home");
        buildResumeButton   = new HoverScaleButton("Build Resume");
        savedResumesButton  = new HoverScaleButton("Saved Resumes");
        settingsButton      = new HoverScaleButton("Settings");
        profileButton       = new HoverScaleButton("Login");

        navigationPanel.add(resumeBuilderButton);
        navigationPanel.add(buildResumeButton);
        navigationPanel.add(savedResumesButton);
        navigationPanel.add(settingsButton);
        navigationPanel.add(profileButton);

        navButtons = new java.util.LinkedHashMap<>();
        navButtons.put("HOME",     (HoverScaleButton) resumeBuilderButton);
        navButtons.put("BUILD",    (HoverScaleButton) buildResumeButton);
        navButtons.put("SAVED",    (HoverScaleButton) savedResumesButton);
        navButtons.put("SETTINGS", (HoverScaleButton) settingsButton);
        navButtons.put("PROFILE",  (HoverScaleButton) profileButton);

        // center cards
        contentPanel = new ui.widgets.AnimatedCards();
        rootPanel.add(contentPanel, BorderLayout.CENTER);

        // cards (simple placeholders; replaced in ctor)
        homePanel         = createCardPanel("Landing Page");
        buildResumePanel  = createCardPanel("Drag and Drop");
        savedResumesPanel = createCardPanel("Your saved resumes");
        settingsPanel     = createCardPanel("Settings");
        profilePanel      = createProfilePanel();

        contentPanel.addCard("HOME",     homePanel);
        contentPanel.addCard("BUILD",    buildResumePanel);
        contentPanel.addCard("SAVED",    savedResumesPanel);
        contentPanel.addCard("SETTINGS", settingsPanel);
        contentPanel.addCard("PROFILE",  profilePanel);
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
            contentPanel.instantShow("HOME");
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

    private void go(String target) {
        setActiveNav(target);

        if (!contentPanel.isShowing() || contentPanel.getWidth() <= 0 || contentPanel.getHeight() <= 0) {
            SwingUtilities.invokeLater(() -> contentPanel.slideTo(target, 0));
            return;
        }
        String current = contentPanel.getCurrentCard();
        if (current == null || current.equals(target)) {
            contentPanel.instantShow(target);
            return;
        }
        int curIdx = CARD_ORDER.indexOf(current);
        int tgtIdx = CARD_ORDER.indexOf(target);
        int dir = Integer.compare(tgtIdx, curIdx);
        contentPanel.slideTo(target, dir);
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ResumeBuilderContainer().setVisible(true));
    }
}
