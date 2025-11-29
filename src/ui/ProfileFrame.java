package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ProfileFrame extends JPanel {

    // UI bits we need to update from the container / controllers
    private JLabel emailValueLabel;
    private JLabel nameValueLabel;
    private JButton logoutButton;

    private ActionListener logoutHandler;
    @SuppressWarnings("unused")
    private ActionListener backHandler;  // kept for compatibility with ProfileController

    public ProfileFrame() {
        setOpaque(true);
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Color.WHITE);
        titleBar.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));

        JLabel titleLabel = new JLabel("Profile & Settings");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        titleLabel.setForeground(new Color(0x111827));
        titleBar.add(titleLabel, BorderLayout.WEST);

        add(titleBar, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 24, 0));
        center.setBackground(new Color(0xF3F4F6));
        center.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JPanel accountCard = new JPanel(new BorderLayout());
        accountCard.setBackground(Color.WHITE);
        accountCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel accountTitle = new JLabel("Account");
        accountTitle.setFont(accountTitle.getFont().deriveFont(Font.BOLD, 14f));
        accountTitle.setForeground(new Color(0x111827));
        accountTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel infoGrid = new JPanel(new GridLayout(2, 2, 10, 8));
        infoGrid.setOpaque(false);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(new Color(0x4B5563));
        emailValueLabel = new JLabel("Not logged in");
        emailValueLabel.setForeground(new Color(0x111827));

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(new Color(0x4B5563));
        nameValueLabel = new JLabel("Not logged in");
        nameValueLabel.setForeground(new Color(0x111827));

        infoGrid.add(emailLabel); infoGrid.add(emailValueLabel);
        infoGrid.add(nameLabel);  infoGrid.add(nameValueLabel);

        accountCard.add(accountTitle, BorderLayout.NORTH);
        accountCard.add(infoGrid, BorderLayout.CENTER);

        JPanel settingsCard = new JPanel();
        settingsCard.setBackground(Color.WHITE);
        settingsCard.setLayout(new BoxLayout(settingsCard, BoxLayout.Y_AXIS));
        settingsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel settingsTitle = new JLabel("Settings");
        settingsTitle.setFont(settingsTitle.getFont().deriveFont(Font.BOLD, 14f));
        settingsTitle.setForeground(new Color(0x111827));
        settingsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JCheckBox emailUpdates = new JCheckBox("Email me updates about tailored resumes");
        emailUpdates.setOpaque(false);
        emailUpdates.setForeground(new Color(0x111827));
        emailUpdates.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailUpdates.setSelected(true);

        JCheckBox showTips = new JCheckBox("Show tips when the app opens");
        showTips.setOpaque(false);
        showTips.setForeground(new Color(0x111827));
        showTips.setAlignmentX(Component.LEFT_ALIGNMENT);
        showTips.setSelected(true);

        JLabel note = new JLabel(
                "<html><span style='font-size:10px;color:#6B7280'>Settings are demo-only in this version.</span></html>"
        );
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        note.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        settingsCard.add(settingsTitle);
        settingsCard.add(Box.createVerticalStrut(4));
        settingsCard.add(emailUpdates);
        settingsCard.add(Box.createVerticalStrut(4));
        settingsCard.add(showTips);
        settingsCard.add(Box.createVerticalStrut(8));
        settingsCard.add(note);

        center.add(accountCard);
        center.add(settingsCard);

        add(center, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        bottomBar.setBackground(Color.WHITE);

        logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(0xDC2626));  // red
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(10, 32, 10, 32));
        logoutButton.setFont(logoutButton.getFont().deriveFont(Font.BOLD, 13f));
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.setOpaque(true);
        logoutButton.setBorderPainted(false);
        logoutButton.addActionListener(e -> {
            if (logoutHandler != null) {
                logoutHandler.actionPerformed(e);
                return;
            }

            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) return;

            try { utils.Constants.Session.logout(); } catch (Throwable ignored) {}

            showInfo("You have been logged out successfully.", "Logout");
        });

        bottomBar.add(logoutButton);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void setUser(String name, String email) {
        String safeName  = (name  != null && !name.isBlank())  ? name  : "Not logged in";
        String safeEmail = (email != null && !email.isBlank()) ? email : "Not logged in";

        nameValueLabel.setText(safeName);
        emailValueLabel.setText(safeEmail);
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

    public void setOnLogout(ActionListener l) {
        for (ActionListener al : logoutButton.getActionListeners()) {
            logoutButton.removeActionListener(al);
        }
        this.logoutHandler = l;
        if (l != null) {
            logoutButton.addActionListener(l);
        }
    }

    public void showInfo(String msg, String title)  {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

}
