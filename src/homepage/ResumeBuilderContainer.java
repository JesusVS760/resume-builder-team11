package homepage;

import ui.LandingPanel;
import ui.UploadPanel;

import javax.swing.*;
import java.awt.*;

public class ResumeBuilderContainer extends JFrame {

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

    public ResumeBuilderContainer () {
        super("Resume Builder");

        // Set look and feel for macOS compatibility
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Use default if setting fails
        }

        $$$setupUI$$$();

        setContentPane(rootPanel);
        setResizable(false);

        Color CARD_BG = Color.GRAY;
        setCardBackgrounds(CARD_BG);

        homePanel.setLayout(new BorderLayout());
        homePanel.removeAll();
        homePanel.add(new LandingPanel("src/homepage/images/landing_hero.png"), BorderLayout.CENTER);
        homePanel.revalidate();
        homePanel.repaint();

        buildResumePanel.setLayout(new BorderLayout());
        buildResumePanel.removeAll();
        buildResumePanel.add(new UploadPanel(), BorderLayout.CENTER);
        // refreshes the card
        buildResumePanel.revalidate();
        buildResumePanel.repaint();

        contentPanel.revalidate();
        contentPanel.repaint();

        this.cards = (CardLayout) contentPanel.getLayout();
        showCard("HOME");

        try {
            // Try multiple possible paths for the profile icon
            java.net.URL url = null;

            // First try: from classpath (build directory)
            url = ResumeBuilderContainer.class.getResource("/homepage/images/profilePic.png");

            // Second try: from src directory (development)
            if (url == null) {
                try {
                    java.io.File file = new java.io.File("src/homepage/images/profilePic.png");
                    if (file.exists()) {
                        url = file.toURI().toURL();
                    }
                } catch (Exception e) {
                    // Ignore file access issues
                }
            }

            if (url != null) {
                var base = new ImageIcon(url).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                profileButton.setIcon(new ImageIcon(base));
            } else {
                System.out.println("Profile icon not found at any location");
            }
        } catch (Exception e) {
            System.out.println("Error loading profile icon: " + e.getMessage());
            // Profile icon not found, continue without it
        }

        styleNavButtons();

        if (!(contentPanel.getLayout() instanceof CardLayout)) {
            contentPanel.setLayout(new CardLayout());
        }

        resumeBuilderButton.addActionListener(e -> showCard("HOME"));
        buildResumeButton.addActionListener(e -> showCard("BUILD"));
        savedResumesButton.addActionListener(e -> showCard("SAVED"));
        FAQButton.addActionListener(e -> showCard("FAQ"));
        settingsButton.addActionListener(e -> showCard("SETTINGS"));
        profileButton.addActionListener(e -> {
            if (utils.Constants.Session.isLoggedIn()) {
                // User is logged in, show profile page with user info
                updateProfilePanel();
                showCard("PROFILE");
            } else {
                // User not logged in, show login frame
                new ui.LoginFrame().setVisible(true);
            }
        });

        navigationPanel.setBackground(new Color(0x1f2937));
        navigationPanel.setOpaque(true);
        navigationPanel.setPreferredSize(new Dimension(220, 0));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);

        // Add window focus listener to check for login state changes
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                // Check if user just logged in
                if (utils.Constants.Session.justLoggedIn()) {
                    // Automatically show profile panel
                    updateProfilePanel();
                    showCard("PROFILE");
                }
            }

            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                // Not needed
            }
        });
    }

    private void styleNavButtons() {
        JButton[] nav = {
                resumeBuilderButton, buildResumeButton, savedResumesButton, FAQButton, settingsButton, profileButton
        };
        for (JButton b : nav) {
            if (b == null) continue;
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(0x374151));
            b.setOpaque(true);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        }
    }

    private void addCard(JPanel panel, String name, String placeholderTitle) {
        if (panel == null) {
            panel = new JPanel(new BorderLayout());
            panel.add(new JLabel(placeholderTitle, SwingConstants.CENTER), BorderLayout.CENTER);
        }
        contentPanel.add(panel, name);
    }

    private void showCard(String name){
        cards.show(contentPanel, name);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void setCardBackgrounds(Color bg) {
        JPanel[] cardsArr = {
                homePanel, buildResumePanel, savedResumesPanel,
                faqPanel, settingsPanel, profilePanel
        };
        for (JPanel p : cardsArr) {
            if (p == null) continue;
            p.setBackground(bg);
            p.setOpaque(true);
        }
    }

    // IntelliJ GUI Designer initialization method (recreated with standard Swing)
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout(0, 0));
        rootPanel.setPreferredSize(new Dimension(795, 538));

        // Create navigation panel (west side)
        navigationPanel = new JPanel();
        navigationPanel.setLayout(new GridLayout(6, 1, 0, 0));
        navigationPanel.setBackground(new Color(0x1f2937));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        navigationPanel.setPreferredSize(new Dimension(220, 0));
        rootPanel.add(navigationPanel, BorderLayout.WEST);

        // Create navigation buttons
        resumeBuilderButton = new JButton("Home");
        buildResumeButton = new JButton("Build Resume");
        savedResumesButton = new JButton("Saved Resumes");
        FAQButton = new JButton("FAQ");
        settingsButton = new JButton("Settings");
        profileButton = new JButton("Profile");

        // Add buttons to navigation panel
        navigationPanel.add(resumeBuilderButton);
        navigationPanel.add(buildResumeButton);
        navigationPanel.add(savedResumesButton);
        navigationPanel.add(FAQButton);
        navigationPanel.add(settingsButton);
        navigationPanel.add(profileButton);

        // Create content panel (center) with CardLayout
        contentPanel = new JPanel();
        contentPanel.setLayout(new CardLayout(0, 0));
        rootPanel.add(contentPanel, BorderLayout.CENTER);

        // Create card panels
        homePanel = createCardPanel("Landing Page");
        buildResumePanel = createCardPanel("Drag and Drop");
        savedResumesPanel = createCardPanel("Your saved resumes");
        faqPanel = createCardPanel("This is where questions will be answered");
        settingsPanel = createCardPanel("Settings");
        profilePanel = createProfilePanel();

        // Add panels to card layout
        contentPanel.add(homePanel, "HOME");
        contentPanel.add(buildResumePanel, "BUILD");
        contentPanel.add(savedResumesPanel, "SAVED");
        contentPanel.add(faqPanel, "FAQ");
        contentPanel.add(settingsPanel, "SETTINGS");
        contentPanel.add(profilePanel, "PROFILE");

    }

    private JPanel createCardPanel(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.GRAY);

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);

        panel.add(label, BorderLayout.NORTH);
        panel.add(Box.createVerticalGlue(), BorderLayout.CENTER); // Vertical spacer

        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.GRAY);

        // Profile info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        infoPanel.setBackground(Color.GRAY);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("User Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(Color.WHITE);
        emailValueLabel = new JLabel("Not logged in");
        emailValueLabel.setForeground(Color.WHITE);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(Color.WHITE);
        nameValueLabel = new JLabel("Not logged in");
        nameValueLabel.setForeground(Color.WHITE);

        infoPanel.add(emailLabel);
        infoPanel.add(emailValueLabel);
        infoPanel.add(nameLabel);
        infoPanel.add(nameValueLabel);

        // Logout button
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setOpaque(true);  // Ensure background is painted on macOS
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);  // Remove default border
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));  // Add custom padding
        logoutButton.addActionListener(e -> {
            utils.Constants.Session.logout();
            JOptionPane.showMessageDialog(this, "You have been logged out successfully.",
                                        "Logout", JOptionPane.INFORMATION_MESSAGE);
            updateProfilePanel(); // Update to show "Not logged in"
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
