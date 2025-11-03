package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import services.TwilioService;
import services.AuthService;
import models.User;

public class VerificationFrame extends JFrame {

    private JTextField emailField;
    private JTextField codeField;
    private JButton sendCodeButton;
    private JButton verifyButton;
    private JLabel titleLabel;
    private JLabel emailLabel;
    private JLabel codeLabel;
    private JLabel statusLabel;

    private static final Color NAVY_BLUE = new Color(30, 58, 138); // Updated background color
    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color GREEN = new Color(34, 139, 34);
    private static final Color BRIGHT_GREEN = new Color(0, 180, 0); // Stronger green for hover
    private static final Color WHITE = Color.WHITE;

    private TwilioService twilioService; // service that sends email codes
    private User currentUser; // user trying to verify their email
    private boolean codeWasSent;
    private boolean autoSendCode; // whether to automatically send code on startup

    public VerificationFrame(User user) {
        this(user, false); // Default: don't auto-send code
    }

    public VerificationFrame(User user, boolean autoSendCode) {
        this.currentUser = user;
        this.twilioService = new TwilioService();
        this.codeWasSent = false;
        this.autoSendCode = autoSendCode;
        initializeFrame();                          // Set up the window basics
        createComponents();                         // Create all the UI elements
        layoutComponents();                         // Arrange them on the screen
        setupEventListeners();

        // Auto-send code if requested (for signup flow)
        if (autoSendCode && user.getEmail() != null && !user.getEmail().isEmpty()) {
            SwingUtilities.invokeLater(() -> autoSendVerificationCode());
        }
    }

    private void initializeFrame() {
        setTitle("Resume Builder - Email Verification");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 500); // window size
        setLocationRelativeTo(null); // Center the window to screen
        setResizable(false); // Don't allow user to resize
        getContentPane().setBackground(NAVY_BLUE);
    }

    // COMPONENTS

    private void createComponents() {
        titleLabel = new JLabel("Email Verification");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBackground(WHITE);
        titleLabel.setForeground(WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Only create email fields and send button if not auto-sending
        if (!autoSendCode) {
            // EMAIL ADDRESS LABEL & INPUT FIELDS
            emailLabel = new JLabel("Email Address:");
            emailLabel.setForeground(WHITE);
            emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            emailField = new JTextField(20);
            emailField.setFont(new Font("Arial", Font.PLAIN, 14));
            emailField.setPreferredSize(new Dimension(250, 30));
            emailField.setToolTipText("Enter your email address");

            // SEND CODE BUTTON
            sendCodeButton = new JButton("Send Code");
            sendCodeButton.setBackground(LIGHT_BLUE);
            sendCodeButton.setForeground(NAVY_BLUE);
            sendCodeButton.setFont(new Font("Arial", Font.BOLD, 14));
            sendCodeButton.setPreferredSize(new Dimension(150, 35));
            sendCodeButton.setFocusPainted(false);
        }

        // VERIFICATION CODE LABEL & TEXT FIELD
        codeLabel = new JLabel("Verification Code:");
        codeLabel.setForeground(WHITE);
        codeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        codeField = new JTextField(20);
        codeField.setFont(new Font("Arial", Font.PLAIN, 14));
        codeField.setPreferredSize(new Dimension(250, 30));
        codeField.setEditable(false); // DISABLED
        codeField.setToolTipText("Enter the 6-digit code from your email");

        // VERIFY BUTTON

        verifyButton = new JButton("Verify Code");
        verifyButton.setBackground(GREEN);
        verifyButton.setForeground(WHITE);
        verifyButton.setFont(new Font("Arial", Font.BOLD, 14));
        verifyButton.setPreferredSize(new Dimension(150, 35));
        verifyButton.setFocusPainted(false);
        verifyButton.setEnabled(false); // DISABLED

        // STATUS LABEL

        if (autoSendCode) {
            statusLabel = new JLabel(""); // Start empty, will show message after code is sent
        } else {
            statusLabel = new JLabel("Enter your email address and click 'Send Code'");
        }
        statusLabel.setForeground(LIGHT_BLUE);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    // LAYOUT

    private void layoutComponents() {
        setLayout(new BorderLayout());

        // TOP SECTION
        JPanel topPanel = new JPanel();
        topPanel.setBackground(NAVY_BLUE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 30, 0));
        topPanel.add(titleLabel);

        // CENTER SECTION

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(NAVY_BLUE);
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        int currentRow = 0;

        // Only add email fields and send button if not auto-sending
        if (!autoSendCode) {
            // Email Label
            gbc.gridx = 0;
            gbc.gridy = currentRow;
            gbc.anchor = GridBagConstraints.WEST;
            centerPanel.add(emailLabel, gbc);

            // Email Input Field
            gbc.gridx = 1;
            gbc.gridy = currentRow;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            centerPanel.add(emailField, gbc);

            currentRow++;

            // Send Code Button
            gbc.gridx = 0;
            gbc.gridy = currentRow;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.CENTER;
            centerPanel.add(sendCodeButton, gbc);

            currentRow++;

            // Spacer (empty row for spacing)
            gbc.gridy = currentRow;
            gbc.insets = new Insets(20, 10, 20, 10);
            centerPanel.add(new JLabel(""), gbc);

            currentRow++;
        }

        // Code Label
        gbc.gridx = 0;
        gbc.gridy = currentRow;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        centerPanel.add(codeLabel, gbc);

        // Code Input Field
        gbc.gridx = 1;
        gbc.gridy = currentRow;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(codeField, gbc);

        currentRow++;

        // Verification Button
        gbc.gridx = 0;
        gbc.gridy = currentRow;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        centerPanel.add(verifyButton, gbc);

        // BOTTOM SECTION

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(NAVY_BLUE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 50, 20));
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        // add to window
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        // Only add send code button listener if it exists (not in auto-send mode)
        if (!autoSendCode) {
            sendCodeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleSendCode(); // send code
                }
            });
        }

        verifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleVerifyCode();  // Verify the code user entered
            }
        });

        // Add hover animation to verify button (turns bright green on hover)
        Color originalColor = verifyButton.getBackground();
        verifyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                verifyButton.setBackground(BRIGHT_GREEN); // Stronger green glow
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                verifyButton.setBackground(originalColor);
            }
        });
    }

    private void autoSendVerificationCode() {
        String email = currentUser.getEmail();

        if (email == null || email.isEmpty()) {
            showError("No email address available for verification");
            return;
        }

        try {
            updateStatus("Sending verification code to " + email + "...", LIGHT_BLUE);

            // Ask TwilioService to send email code
            boolean codeSent = twilioService.sendVerificationCode(email);

            if (codeSent) {
                codeWasSent = true;
                codeField.setEditable(true);
                verifyButton.setEnabled(true);
                updateStatus("Verification code sent! Check your email inbox.", GREEN);

                // Focus on code field for convenience
                codeField.requestFocus();
            } else {
                showError("Failed to send verification code! Please try again.");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void handleSendCode() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Please enter an email address");
            return;
        }

        // Basic email format validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("Please enter a valid email address");
            return;
        }

        try {
            // Disable send button to prevent multiple clicks
            sendCodeButton.setEnabled(false);
            updateStatus("Sending verification code...", LIGHT_BLUE);

            // Ask TwilioService to send email code
            boolean codeSent = twilioService.sendVerificationCode(email);

            if (codeSent) {
                codeWasSent = true;
                emailField.setEditable(false);
                codeField.setEditable(true);
                verifyButton.setEnabled(true);
                updateStatus("Verification code sent! Check your email inbox.", GREEN);

                // Focus on code field for convenience
                codeField.requestFocus();
            } else {
                showError("Failed to send verification code! Please try again.");
                sendCodeButton.setEnabled(true);
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            sendCodeButton.setEnabled(true);
        }
    }

    private void handleVerifyCode() {
        String enteredCode = codeField.getText().trim();

        if (enteredCode.isEmpty()) {
            showError("Please enter the verification code");
            return;
        }

        // Validate code format (6 digits)
        if (!enteredCode.matches("^\\d{6}$")) {
            showError("Verification code must be 6 digits");
            return;
        }

        try {
            updateStatus("Verifying code...", LIGHT_BLUE);

            // Ask TwilioService to verify if the code is correct
            boolean isValid = twilioService.verifyCode(enteredCode);

            if (isValid) {
                // Complete the signup process (create the actual account)
                AuthService authService = new AuthService();
                boolean accountCreated = authService.completeSignup(currentUser.getVerificationCode());

                if (accountCreated) {
                    // Success - account created and email verified
                    updateStatus("Account created successfully!", GREEN);
                    showSuccess("Your account has been created!\n\nYou can now log in to your account.");

                    // Disable all inputs
                    verifyButton.setEnabled(false);
                    codeField.setEditable(false);

                    // Close this window and open login frame after a short delay
                    Timer timer = new Timer(1500, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Open login frame
                            new LoginFrame().setVisible(true);
                            // Close verification frame
                            dispose();
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    // Failed to create account
                    showError("Failed to create account. The verification link may have expired or email is already in use.");
                }
            } else {
                // Failed - code was wrong
                showError("Invalid verification code. Please try again.");
                codeField.setText("");  // Clear the code field
                codeField.requestFocus();
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        updateStatus(message, new Color(255, 0, 0));
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    public static void main(String[] args) {
        // Load environment variables
        utils.EnvLoader.load();

        // Create a dummy user for testing
        User testUser = new User();
        testUser.setName("Test User");

        // Open the verification frame
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VerificationFrame(testUser).setVisible(true);
            }
        });
    }
}