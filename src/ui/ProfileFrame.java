package ui;

import javax.swing.*;
import java.awt.*;
import models.User;
import utils.Constants;

public class ProfileFrame extends JFrame {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel emailLabel;
    private JLabel nameLabel;
    private JLabel emailValueLabel;
    private JLabel nameValueLabel;
    private JButton logoutButton;
    private JButton backButton;

    public ProfileFrame() {
        // Set look and feel for macOS compatibility
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Use default if setting fails
        }
        
        initializeComponents();
        setupLayout();
        setupListeners();
        loadUserData();
    }

    private void initializeComponents() {
        setTitle("User Profile - " + Constants.APP_NAME);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        titleLabel = new JLabel("User Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLUE);

        emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 14));

        nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        emailValueLabel = new JLabel();
        emailValueLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        nameValueLabel = new JLabel();
        nameValueLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setOpaque(true);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        backButton = new JButton("Back to Home");
    }

    private void setupLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        // Email label
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(emailLabel, gbc);

        // Email value
        gbc.gridx = 1; gbc.gridy = 1;
        mainPanel.add(emailValueLabel, gbc);

        // Name label
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(nameLabel, gbc);

        // Name value
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(nameValueLabel, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        mainPanel.add(buttonPanel, gbc);

        buttonPanel.add(backButton);
        buttonPanel.add(logoutButton);

        setContentPane(mainPanel);
    }

    private void setupListeners() {
        logoutButton.addActionListener(event -> {
            Constants.Session.logout();
            JOptionPane.showMessageDialog(this, "You have been logged out successfully.",
                                        "Logout", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
        });

        backButton.addActionListener(e -> {
            dispose();
            // If we have a reference to the homepage, we could show it
            // For now, just close this window
        });
    }

    private void loadUserData() {
        User currentUser = Constants.Session.getCurrentUser();
        if (currentUser != null) {
            emailValueLabel.setText(currentUser.getEmail());
            nameValueLabel.setText(currentUser.getName());
        } else {
            // This shouldn't happen if session is properly managed
            JOptionPane.showMessageDialog(this, "No user session found. Please log in again.",
                                        "Session Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
