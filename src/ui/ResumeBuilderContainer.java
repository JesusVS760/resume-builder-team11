package ui;

import controllers.LandingController;
import ui.widgets.HoverScaleButton;
import ui.widgets.AnimatedCards;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResumeBuilderContainer extends JFrame {

    // Card Keys
    private static final String CARD_HOME     = "HOME";
    private static final String CARD_BUILD    = "BUILD";
    private static final String CARD_SAVED    = "SAVED";
    private static final String CARD_SETTINGS = "SETTINGS";
    private static final String CARD_PROFILE  = "PROFILE";

    // Ordering is used to compute slide direction
    private static final List<String> CARD_ORDER =
            List.of(CARD_HOME, CARD_BUILD, CARD_SAVED, CARD_SETTINGS, CARD_PROFILE);

    // Colors
    private static final Color NAV_BG      = new Color(0x1f2937);
    private static final Color NAV_BTN_BG  = new Color(0x374151);
    private static final Color CARD_BG     = Color.GRAY;
    private static final Color CARD_FG     = Color.WHITE;

    // Root + Cards
    private JPanel rootPanel;
    private AnimatedCards contentPanel;
    private JPanel homePanel;
    private JPanel buildResumePanel;
    private SavedResumesPanel savedResumesPanel;
    private JPanel settingsPanel;
    private JPanel profilePanel;

    // Nav Buttons
    private HoverScaleButton resumeBuilderButton; // "Home"
    private HoverScaleButton buildResumeButton;
    private HoverScaleButton savedResumesButton;
    private HoverScaleButton settingsButton;
    private HoverScaleButton profileButton;

    // Map card key -> button (for sticky/active state)
    private Map<String, HoverScaleButton> navButtons;

    // Profile UI bits
    private JLabel emailValueLabel;
    private JLabel nameValueLabel;
    private JButton logoutButton;

    // Reference to inner UploadPanel for controllers
    private UploadPanel uploadPanel;

    public ResumeBuilderContainer() {
        super("Resume Builder");

        initLookAndFeel();
        buildUI();
        setContentPane(rootPanel);
        setResizable(false);

        setCardBackgrounds(CARD_BG);
        styleNavButtons();

        plugContent();

        ImageIcon profileIcon = loadScaledIcon(
                "/ui/images/profilePic.png",
                "src/ui/images/profilePic.png",
                24, 24
        );
        if (profileIcon != null) profileButton.setIcon(profileIcon);

        contentPanel.instantShow(CARD_HOME);
        setActiveNav(CARD_HOME);
        updateAuthUI();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);

        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {
                try {
                    if (utils.Constants.Session.justLoggedIn()) {
                        updateProfilePanel();
                        updateAuthUI();
                        go(CARD_PROFILE);
                    }
                } catch (Throwable ignored) {}
            }
            @Override public void windowLostFocus(java.awt.event.WindowEvent e) { }
        });
    }

    public void setOnNavHome(ActionListener l)     { resumeBuilderButton.addActionListener(l); }
    public void setOnNavBuild(ActionListener l)    { buildResumeButton.addActionListener(l); }
    public void setOnNavSaved(ActionListener l)    { savedResumesButton.addActionListener(l); }
    public void setOnNavSettings(ActionListener l) { settingsButton.addActionListener(l); }
    public void setOnNavProfile(ActionListener l)  { profileButton.addActionListener(l); }

    public void showHome()     { go(CARD_HOME); }
    public void showBuild()    { go(CARD_BUILD); }
    public void showSaved()    { go(CARD_SAVED); }
    public void showSettings() { go(CARD_SETTINGS); }
    public void showProfile()  { updateProfilePanel(); updateAuthUI(); go(CARD_PROFILE); }

    public UploadPanel getUploadPanel() { return uploadPanel; }

    public SavedResumesPanel getSavedPanel() { return savedResumesPanel; }

    public void updateAuthUIPublic() { updateAuthUI(); }

    public void updateProfileView(models.User user) {
        try {
            if (user == null) return;
            emailValueLabel.setText(user.getEmail());
            nameValueLabel.setText(user.getName());
            logoutButton.setVisible(true);
            profilePanel.revalidate();
            profilePanel.repaint();
        } catch (Throwable ignored) {}
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
    }

    private void buildUI() {
        rootPanel = new JPanel(new BorderLayout());
        rootPanel.setPreferredSize(new Dimension(795, 538));

        JPanel navigationPanel = new JPanel(new GridLayout(5, 1, 0, 12));
        navigationPanel.setBackground(NAV_BG);
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        navigationPanel.setPreferredSize(new Dimension(220, 0));
        rootPanel.add(navigationPanel, BorderLayout.WEST);

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

        navButtons = new LinkedHashMap<>();
        navButtons.put(CARD_HOME,     resumeBuilderButton);
        navButtons.put(CARD_BUILD,    buildResumeButton);
        navButtons.put(CARD_SAVED,    savedResumesButton);
        navButtons.put(CARD_SETTINGS, settingsButton);
        navButtons.put(CARD_PROFILE,  profileButton);

        contentPanel = new AnimatedCards();
        rootPanel.add(contentPanel, BorderLayout.CENTER);

        homePanel         = createCardPanel("Landing Page");
        buildResumePanel  = createCardPanel("Drag and Drop");
        savedResumesPanel = new SavedResumesPanel();
        settingsPanel     = createCardPanel("Settings");
        profilePanel      = createProfilePanel();

        contentPanel.addCard(CARD_HOME,     homePanel);
        contentPanel.addCard(CARD_BUILD,    buildResumePanel);
        contentPanel.addCard(CARD_SAVED,    savedResumesPanel);
        contentPanel.addCard(CARD_SETTINGS, settingsPanel);
        contentPanel.addCard(CARD_PROFILE,  profilePanel);
    }

    private void styleNavButtons() {
        for (HoverScaleButton b : navButtons.values()) {
            b.setBackground(NAV_BTN_BG);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
        }
    }

    private void plugContent() {
        homePanel.removeAll();
        homePanel.setLayout(new BorderLayout());

        LandingPanel landing = new LandingPanel("src/ui/images/landing_hero.png");

        new LandingController(landing, () -> buildResumeButton.doClick());

        homePanel.add(landing, BorderLayout.CENTER);

        buildResumePanel.removeAll();
        buildResumePanel.setLayout(new BorderLayout());
        uploadPanel = new UploadPanel();
        buildResumePanel.add(uploadPanel, BorderLayout.CENTER);
    }



    private void updateAuthUI() {
        boolean loggedIn = false;
        try { loggedIn = utils.Constants.Session.isLoggedIn(); } catch (Throwable ignored) {}
        profileButton.setText(loggedIn ? "Profile" : "Login");
    }

    private JPanel createCardPanel(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(CARD_FG);

        panel.add(label, BorderLayout.NORTH);
        panel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);

        JLabel titleLabel = new JLabel("User Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(CARD_FG);

        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        infoPanel.setBackground(CARD_BG);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(CARD_FG);
        emailValueLabel = new JLabel("Not logged in");
        emailValueLabel.setForeground(CARD_FG);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(CARD_FG);
        nameValueLabel = new JLabel("Not logged in");
        nameValueLabel.setForeground(CARD_FG);

        infoPanel.add(emailLabel); infoPanel.add(emailValueLabel);
        infoPanel.add(nameLabel);  infoPanel.add(nameValueLabel);

        logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(0xD40000));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setOpaque(true);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        logoutButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) return;

            try { utils.Constants.Session.logout(); } catch (Throwable ignored) {}

            JOptionPane.showMessageDialog(
                    this,
                    "You have been logged out successfully.",
                    "Logout",
                    JOptionPane.INFORMATION_MESSAGE
            );
            updateProfilePanel();
            updateAuthUI();
            contentPanel.instantShow(CARD_HOME);
            setActiveNav(CARD_HOME);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(logoutButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void updateProfilePanel() {
        try {
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
        } catch (Throwable t) {
            emailValueLabel.setText("Not logged in");
            nameValueLabel.setText("Not logged in");
            logoutButton.setVisible(false);
        }
    }

    private void setCardBackgrounds(Color bg) {
        JPanel[] cardsArr = { homePanel, buildResumePanel, savedResumesPanel, settingsPanel, profilePanel };
        for (JPanel p : cardsArr) {
            if (p != null) { p.setBackground(bg); p.setOpaque(true); }
        }
    }

    private void setActiveNav(String key) {
        if (navButtons == null) return;
        for (HoverScaleButton b : navButtons.values()) b.setSticky(false);
        HoverScaleButton active = navButtons.get(key);
        if (active != null) active.setSticky(true);
    }

    private void go(String target) {
        setActiveNav(target);

        String current = contentPanel.getCurrentCard();
        if (current == null) {
            contentPanel.instantShow(target);
            return;
        }
        if (current.equals(target)) return;

        int curIdx = CARD_ORDER.indexOf(current);
        int tgtIdx = CARD_ORDER.indexOf(target);
        int dir = Integer.compare(tgtIdx, curIdx);
        contentPanel.slideTo(target, dir);
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ResumeBuilderContainer().setVisible(true));
    }
}
