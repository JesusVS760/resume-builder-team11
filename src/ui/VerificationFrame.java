package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import services.TwilioService;
import models.User;

public class VerificationFrame extends JFrame {

    private JTextField emailField;
    private JTextField codeField;
    private JButton sendCodeButton;
    private JButton verifyButton;
    private JLabel statusLabel;
    private TwilioService twilioService;
    private User currentUser;
    private boolean codeWasSent;

    private static final Color NAVY_BLUE = new Color(0x1f2937);
    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color WHITE = Color.WHITE;
    private static final Color GREEN = new Color(34, 139, 34);

    public VerificationFrame(User user) {
        this.currentUser = user;
        this.twilioService = new TwilioService();
        this.codeWasSent = false;
        initUI();
    }

    private void initUI() {
        setTitle("Resume Builder - Email Verification");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(NAVY_BLUE);
        setLayout(new GridBagLayout()); // Center everything
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);

        // Main panel with rounded border
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(NAVY_BLUE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BLUE, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints pgbc = new GridBagConstraints();
        pgbc.fill = GridBagConstraints.HORIZONTAL;
        pgbc.insets = new Insets(10, 10, 10, 10);
        pgbc.gridx = 0;
        pgbc.gridy = 0;

        JLabel titleLabel = new JLabel("Email Verification", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pgbc.gridwidth = 2;
        panel.add(titleLabel, pgbc);

        // Email label & field
        pgbc.gridy++;
        pgbc.gridwidth = 1;
        JLabel emailLabel = new JLabel("Email Address:");
        emailLabel.setForeground(WHITE);
        panel.add(emailLabel, pgbc);

        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(200, 30));
        panel.add(emailField, pgbc = copyGbc(pgbc, 1, pgbc.gridy));

        // Send code button
        pgbc.gridy++;
        pgbc.gridx = 0;
        pgbc.gridwidth = 2;
        sendCodeButton = new JButton("Send Code");
        styleButton(sendCodeButton, LIGHT_BLUE, NAVY_BLUE);
        panel.add(sendCodeButton, pgbc);

        // Code label & field
        pgbc.gridy++;
        pgbc.gridwidth = 1;
        pgbc.gridx = 0;
        JLabel codeLabel = new JLabel("Verification Code:");
        codeLabel.setForeground(WHITE);
        panel.add(codeLabel, pgbc);

        codeField = new JTextField();
        codeField.setPreferredSize(new Dimension(200, 30));
        codeField.setEditable(false);
        panel.add(codeField, copyGbc(pgbc, 1, pgbc.gridy));

        // Verify button
        pgbc.gridy++;
        pgbc.gridx = 0;
        pgbc.gridwidth = 2;
        verifyButton = new JButton("Verify Code");
        verifyButton.setEnabled(false);
        styleButton(verifyButton, GREEN, WHITE);
        panel.add(verifyButton, pgbc);

        // Status label
        pgbc.gridy++;
        statusLabel = new JLabel("Enter your email and click 'Send Code'", SwingConstants.CENTER);
        statusLabel.setForeground(LIGHT_BLUE);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        panel.add(statusLabel, pgbc);

        // Add panel to frame
        add(panel);

        setupListeners();
    }

    private void setupListeners() {
        sendCodeButton.addActionListener(e -> handleSendCode());
        verifyButton.addActionListener(e -> handleVerifyCode());
    }

    private void handleSendCode() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError("Please enter an email address");
            return;
        }
        try {
            sendCodeButton.setEnabled(false);
            statusLabel.setText("Sending code...");
            boolean sent = twilioService.sendVerificationCode(email);
            if (sent) {
                codeWasSent = true;
                emailField.setEditable(false);
                codeField.setEditable(true);
                verifyButton.setEnabled(true);
                statusLabel.setText("Verification code sent!");
            } else {
                showError("Failed to send verification code");
                sendCodeButton.setEnabled(true);
            }
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
            sendCodeButton.setEnabled(true);
        }
    }

    private void handleVerifyCode() {
        String code = codeField.getText().trim();
        if (!code.matches("\\d{6}")) {
            showError("Enter a valid 6-digit code");
            return;
        }
        try {
            boolean valid = twilioService.verifyCode(code);
            if (valid) {
                currentUser.setPhoneVerified(true);
                statusLabel.setText("Email verified successfully!");
                JOptionPane.showMessageDialog(this, "Email verified! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                new LoginFrame().setVisible(true);
                dispose();
            } else {
                showError("Invalid verification code");
                codeField.setText("");
            }
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setOpaque(true);
    }

    private GridBagConstraints copyGbc(GridBagConstraints gbc, int x, int y) {
        GridBagConstraints c = (GridBagConstraints) gbc.clone();
        c.gridx = x;
        c.gridy = y;
        return c;
    }

    public static void main(String[] args) {
        User testUser = new User();
        testUser.setName("Test User");
        SwingUtilities.invokeLater(() -> new VerificationFrame(testUser).setVisible(true));
    }
}
