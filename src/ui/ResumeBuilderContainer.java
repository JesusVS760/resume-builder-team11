package ui;

import ui.LandingPanel;
import ui.UploadPanel;
import ui.widgets.HoverScaleButton;
import ui.widgets.AnimatedCards;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Main container frame for the Resume Builder app.
 * - Left: navigation with animated buttons.
 * - Right: card stack (AnimatedCards) with HOME/BUILD/SAVED/SETTINGS/PROFILE views.
 *
 * Responsibilities:
 * 1) Build the UI (nav + cards)
 * 2) Wire navigation (including sticky "active" nav button)
 * 3) Handle login/logout transitions and profile panel updates
 */
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

    // The root panel and the cards
    private JPanel rootPanel;
    private AnimatedCards contentPanel;
    private JPanel homePanel;
    private JPanel buildResumePanel;
    private JPanel savedResumesPanel;
    private JPanel settingsPanel;
    private JPanel profilePanel;

    // Nav Buttons
    private HoverScaleButton resumeBuilderButton; // "Home"
    private HoverScaleButton buildResumeButton;
    private HoverScaleButton savedResumesButton;
    private HoverScaleButton settingsButton;
    private HoverScaleButton profileButton;

    // Just for the user to be able to know where they are
    private Map<String, HoverScaleButton> navButtons;

    // Profile UI
    private JLabel emailValueLabel;
    private JLabel nameValueLabel;
    private JButton logoutButton;

    public ResumeBuilderContainer() {
        super("Resume Builder");

        initLookAndFeel();
        buildUI();          // build UI structure (nav + cards + placeholders)
        setContentPane(rootPanel);
        setResizable(false);

        // Style
        setCardBackgrounds(CARD_BG);
        styleNavButtons();

        // Plug real content into the placeholder cards
        plugContent();

        // Icon for profile if available (classpath first, then file fallback)
        ImageIcon profileIcon = loadScaledIcon(
                "/ui/images/profilePic.png",
                "src/ui/images/profilePic.png",
                24, 24
        );
        if (profileIcon != null) profileButton.setIcon(profileIcon);

        // When the user opens the program it defaults to the home page
        contentPanel.instantShow(CARD_HOME);
        setActiveNav(CARD_HOME);
        updateAuthUI();

        // Nav actions
        wireNavigationActions();

        // focus behavior for the frame
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);

        // After user logs in via LoginFrame, bring them to PROFILE when focus returns
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {
                if (utils.Constants.Session.justLoggedIn()) {
                    updateProfilePanel();
                    updateAuthUI();
                    go(CARD_PROFILE);
                }
            }
            @Override public void windowLostFocus(java.awt.event.WindowEvent e) { /* no-op */ }
        });
    }

    // Lifecycle Helpers

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
    }

    /**
     * Build the static UI structure:
     * - Left navigation (buttons)
     * - Right card stack (placeholders; replaced in plugContent())
     */
    private void buildUI() {
        rootPanel = new JPanel(new BorderLayout());
        rootPanel.setPreferredSize(new Dimension(795, 538));

        // Left navigation
        JPanel navigationPanel = new JPanel(new GridLayout(5, 1, 0, 12));
        navigationPanel.setBackground(NAV_BG);
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        navigationPanel.setPreferredSize(new Dimension(220, 0));
        rootPanel.add(navigationPanel, BorderLayout.WEST);

        // Nav buttons (HoverScaleButton supports sticky scale)
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

        // Map card key -> button (for sticky/active state)
        navButtons = new LinkedHashMap<>();
        navButtons.put(CARD_HOME,     resumeBuilderButton);
        navButtons.put(CARD_BUILD,    buildResumeButton);
        navButtons.put(CARD_SAVED,    savedResumesButton);
        navButtons.put(CARD_SETTINGS, settingsButton);
        navButtons.put(CARD_PROFILE,  profileButton);

        // Right side card stack
        contentPanel = new AnimatedCards();
        rootPanel.add(contentPanel, BorderLayout.CENTER);

        // Placeholders (text + spacing); real content added in plugContent()
        homePanel         = createCardPanel("Landing Page");
        buildResumePanel  = createCardPanel("Drag and Drop");
        savedResumesPanel = createCardPanel("Your saved resumes");
        settingsPanel     = createCardPanel("Settings");
        profilePanel      = createProfilePanel();

        contentPanel.addCard(CARD_HOME,     homePanel);
        contentPanel.addCard(CARD_BUILD,    buildResumePanel);
        contentPanel.addCard(CARD_SAVED,    savedResumesPanel);
        contentPanel.addCard(CARD_SETTINGS, settingsPanel);
        contentPanel.addCard(CARD_PROFILE,  profilePanel);
    }

    /** Apply consistent background/foreground and basic button styling. */
    private void styleNavButtons() {
        for (HoverScaleButton b : navButtons.values()) {
            b.setBackground(NAV_BTN_BG);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
        }
    }

    /** Replace placeholders with real content panels. */
    private void plugContent() {
        homePanel.removeAll();
        homePanel.setLayout(new BorderLayout());
        homePanel.add(new LandingPanel("src/ui/images/landing_hero.png"), BorderLayout.CENTER);

        buildResumePanel.removeAll();
        buildResumePanel.setLayout(new BorderLayout());
        buildResumePanel.add(new UploadPanel(), BorderLayout.CENTER);
    }

    /** Show correct "Login"/"Profile" label depending on session state. */
    private void updateAuthUI() {
        boolean loggedIn = utils.Constants.Session.isLoggedIn();
        profileButton.setText(loggedIn ? "Profile" : "Login");
    }

    /** Create a simple labeled card with our theme. */
    private JPanel createCardPanel(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(CARD_FG);

        panel.add(label, BorderLayout.NORTH);
        panel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Profile card with user info + logout.
     * Logout clears session and returns to HOME.
     */
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
        logoutButton.setBackground(Color.RED);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setOpaque(true);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        logoutButton.addActionListener(e -> {
            utils.Constants.Session.logout();
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

    /** Reflect current session data into the profile card. */
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

    /** Set all cards to a uniform background. */
    private void setCardBackgrounds(Color bg) {
        JPanel[] cardsArr = { homePanel, buildResumePanel, savedResumesPanel, settingsPanel, profilePanel };
        for (JPanel p : cardsArr) {
            if (p != null) { p.setBackground(bg); p.setOpaque(true); }
        }
    }

    /**
     * Nav → Card wiring. Uses a single go(String) method that:
     * - sets sticky/active nav button
     * - decides slide direction based on CARD_ORDER
     * - gracefully handles first-show (no size yet)
     */
    private void wireNavigationActions() {
        resumeBuilderButton.addActionListener(e -> go(CARD_HOME));
        buildResumeButton.addActionListener(e -> go(CARD_BUILD));
        savedResumesButton.addActionListener(e -> go(CARD_SAVED));
        settingsButton.addActionListener(e -> go(CARD_SETTINGS));
        profileButton.addActionListener(e -> {
            if (utils.Constants.Session.isLoggedIn()) {
                updateProfilePanel();
                updateAuthUI();
                go(CARD_PROFILE);
            } else {
                new ui.LoginFrame().setVisible(true);
            }
        });
    }

    /** Visually pin/unpin which nav button is "active" (scaled up). */
    private void setActiveNav(String key) {
        if (navButtons == null) return;
        for (HoverScaleButton b : navButtons.values()) b.setSticky(false);
        HoverScaleButton active = navButtons.get(key);
        if (active != null) active.setSticky(true);
    }

    /** Centralized navigation handler for animated card transitions. */
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
        int dir = Integer.compare(tgtIdx, curIdx);   // +1 forward, -1 backward
        contentPanel.slideTo(target, dir);
    }

    // Utilities

    /**
     * Try to load an icon from classpath; if missing, fallback to a file path.
     * @param classpath   e.g. "/ui/images/profilePic.png"
     * @param fileFallback e.g. "src/ui/images/profilePic.png"
     */
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
