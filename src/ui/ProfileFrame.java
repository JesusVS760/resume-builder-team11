package ui;

import javax.swing.*;
import java.awt.*;

public class ProfileFrame extends JFrame {

    // UI
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel emailLabel;
    private JLabel nameLabel;
    private JLabel emailValueLabel;
    private JLabel nameValueLabel;
    private JButton backButton;
    private JButton logoutButton;

    public ProfileFrame() {
        // Prefer system look & feel
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignore) {}

        initializeFrame();
        createComponents();
        layoutComponents();
        populateFromSessionIfPresent();
    }

    private void initializeFrame() {
        setTitle("User Profile");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 300);
        setLocationRelativeTo(null);
    }

    private void createComponents() {
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        titleLabel = new JLabel("User Profile");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        emailLabel = new JLabel("Email:");
        nameLabel  = new JLabel("Name:");

        emailValueLabel = new JLabel("—");
        nameValueLabel  = new JLabel("—");

        backButton   = new JButton("Back");
        logoutButton = new JButton("Logout");
    }

    private void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(titleLabel, gbc);

        // Email row
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(emailLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        mainPanel.add(emailValueLabel, gbc);

        // Name row
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(nameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(nameValueLabel, gbc);

        // Buttons row
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.add(backButton);
        btns.add(logoutButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(btns, gbc);
    }

    /** Minimal compatibility: read current user from session if present. */
    private void populateFromSessionIfPresent() {
        try {
            if (utils.Constants.Session.isLoggedIn()) {
                var user = utils.Constants.Session.getCurrentUser();
                if (user != null) {
                    setUser(user.getName(), user.getEmail());
                }
            }
        } catch (Throwable ignore) {
        }
    }

    // MVC controller hooks
    public void setOnBack(java.awt.event.ActionListener l)   { if (backButton != null) backButton.addActionListener(l); }
    public void setOnLogout(java.awt.event.ActionListener l) { if (logoutButton != null) logoutButton.addActionListener(l); }

    // Allow controller/app to set user info explicitly
    public void setUser(String name, String email) {
        nameValueLabel.setText(name != null && !name.isBlank() ? name : "—");
        emailValueLabel.setText(email != null && !email.isBlank() ? email : "—");
    }

    // Message helpers
    public void showInfo(String msg, String title)  { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE); }
    public void showError(String msg, String title) { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProfileFrame().setVisible(true));
    }
}
