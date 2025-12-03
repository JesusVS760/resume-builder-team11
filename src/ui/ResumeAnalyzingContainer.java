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

public class ResumeAnalyzingContainer extends JFrame {

    // Card Keys
    private static final String CARD_HOME     = "HOME";
    private static final String CARD_BUILD    = "BUILD";
    private static final String CARD_SAVED    = "SAVED";
    private static final String CARD_PROFILE  = "PROFILE";

    // Ordering is used to compute slide direction
    private static final List<String> CARD_ORDER =
            List.of(CARD_HOME, CARD_BUILD, CARD_SAVED, CARD_PROFILE);

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
    private ProfileFrame profilePanel;

    // Nav Buttons
    private HoverScaleButton resumeBuilderButton; // "Home"
    private HoverScaleButton buildResumeButton;
    private HoverScaleButton savedResumesButton;
    private HoverScaleButton profileButton;

    // Map card key -> button (for sticky/active state)
    private Map<String, HoverScaleButton> navButtons;

    // Profile UI bits (used inside the PROFILE card)
    private JLabel emailValueLabel;
    private JLabel nameValueLabel;
    private JButton logoutButton;

    // Reference to inner UploadPanel for controllers
    private UploadPanel uploadPanel;

    public ResumeAnalyzingContainer() {
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

        if (profilePanel != null) {
            profilePanel.setOnLogout(e -> {
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
        }

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
    public void setOnNavProfile(ActionListener l)  { profileButton.addActionListener(l); }

    public void showHome()     { go(CARD_HOME); }
    public void showBuild()    { go(CARD_BUILD); }
    public void showSaved()    { go(CARD_SAVED); }

    public void showProfile()  {
        updateProfilePanel();
        updateAuthUI();
        go(CARD_PROFILE);
    }

    public UploadPanel getUploadPanel()     { return uploadPanel; }
    public SavedResumesPanel getSavedPanel(){ return savedResumesPanel; }

    public void updateAuthUIPublic() { updateAuthUI(); }

    public void updateProfileView(models.User user) {
        if (profilePanel == null) return;
        try {
            if (user == null) {
                profilePanel.setUser(null, null);
            } else {
                profilePanel.setUser(user.getName(), user.getEmail());
            }
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

        JPanel navigationPanel = new JPanel(new GridLayout(4, 1, 0, 12));
        navigationPanel.setBackground(NAV_BG);
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        navigationPanel.setPreferredSize(new Dimension(220, 0));
        rootPanel.add(navigationPanel, BorderLayout.WEST);

        resumeBuilderButton = new HoverScaleButton("Home");
        buildResumeButton   = new HoverScaleButton("Analyze Resume");
        savedResumesButton  = new HoverScaleButton("Saved Resumes");
        profileButton       = new HoverScaleButton("Login");

        navigationPanel.add(resumeBuilderButton);
        navigationPanel.add(buildResumeButton);
        navigationPanel.add(savedResumesButton);
        navigationPanel.add(profileButton);

        navButtons = new LinkedHashMap<>();
        navButtons.put(CARD_HOME,    resumeBuilderButton);
        navButtons.put(CARD_BUILD,   buildResumeButton);
        navButtons.put(CARD_SAVED,   savedResumesButton);
        navButtons.put(CARD_PROFILE, profileButton);

        contentPanel = new AnimatedCards();
        rootPanel.add(contentPanel, BorderLayout.CENTER);

        homePanel         = createCardPanel("Landing Page");
        buildResumePanel  = createCardPanel("Drag and Drop");
        savedResumesPanel = new SavedResumesPanel();
        profilePanel      = new ProfileFrame();

        contentPanel.addCard(CARD_HOME,     homePanel);
        contentPanel.addCard(CARD_BUILD,    buildResumePanel);
        contentPanel.addCard(CARD_SAVED,    savedResumesPanel);
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

    private void updateProfilePanel() {
        if (profilePanel == null) return;
        try {
            if (utils.Constants.Session.isLoggedIn()) {
                models.User user = utils.Constants.Session.getCurrentUser();
                String name  = (user != null) ? user.getName()  : null;
                String email = (user != null) ? user.getEmail() : null;
                profilePanel.setUser(name, email);
            } else {
                profilePanel.setUser(null, null);
            }
        } catch (Throwable t) {
            profilePanel.setUser(null, null);
        }
    }

    private void setCardBackgrounds(Color bg) {
        JPanel[] cardsArr = { homePanel, buildResumePanel, savedResumesPanel, profilePanel };
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
            java.net.URL url = ResumeAnalyzingContainer.class.getResource(classpath);
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
}
