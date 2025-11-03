package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import services.AuthService;
import models.User;

public class SignupFrame extends JFrame {

    // UI Components
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton signupButton;
    private JButton backToLoginButton;
    private JButton googleSignupButton;
    private JButton githubSignupButton;
    private JLabel titleLabel;
    private JLabel emailLabel;
    private JLabel passwordLabel;
    private JLabel confirmPasswordLabel;
    private JLabel orLabel;

    // Track running OAuth operations
    private SwingWorker<?, ?> runningOAuthWorker;

    // Colors - macOS compatible
    private static final Color NAVY_BLUE = new Color(0x1f2937);
    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color WHITE = Color.WHITE;
    private static final Color DARK_GRAY = Color.DARK_GRAY;

    // Services
    private AuthService authService;

    public SignupFrame() {
        // Set system look and feel for better macOS compatibility
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback to cross-platform L&F if system L&F fails
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                // Use default if both fail
            }
        }

        this.authService = new AuthService();
        initializeFrame();
        createComponents();
        layoutComponents();
        setupEventListeners();
        setupWindowListeners();
    }

    private void initializeFrame() {
        setTitle("Resume Builder - Sign Up");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(NAVY_BLUE);
    }

    private void createComponents() {
        // Title
        titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Email components
        emailLabel = new JLabel("Email:");
        emailLabel.setForeground(WHITE);
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        emailField.setPreferredSize(new Dimension(200, 30));
        emailField.setMinimumSize(new Dimension(200, 30));

        // Password components
        passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(WHITE);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setMinimumSize(new Dimension(200, 30));

        // Confirm Password components
        confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setForeground(WHITE);
        confirmPasswordLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        confirmPasswordField.setPreferredSize(new Dimension(200, 30));
        confirmPasswordField.setMinimumSize(new Dimension(200, 30));

        // Buttons - ensure visibility on macOS
        signupButton = new JButton("Create Account");
        signupButton.setBackground(LIGHT_BLUE);
        signupButton.setForeground(Color.BLACK);
        signupButton.setFont(new Font("Arial", Font.BOLD, 14));
        signupButton.setPreferredSize(new Dimension(150, 35));
        signupButton.setFocusPainted(false);
        signupButton.setOpaque(true);

        backToLoginButton = new JButton("Back to Login");
        backToLoginButton.setBackground(Color.LIGHT_GRAY);
        backToLoginButton.setForeground(Color.BLACK);
        backToLoginButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backToLoginButton.setPreferredSize(new Dimension(120, 25));
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.setOpaque(true);

        // OAuth buttons - unified continue flow
        googleSignupButton = new JButton("Continue with Google");
        googleSignupButton.setBackground(LIGHT_BLUE);
        googleSignupButton.setForeground(Color.BLACK);
        googleSignupButton.setFont(new Font("Arial", Font.BOLD, 14));
        googleSignupButton.setPreferredSize(new Dimension(150, 35));
        googleSignupButton.setFocusPainted(false);
        googleSignupButton.setOpaque(true);

        githubSignupButton = new JButton("Continue with GitHub");
        githubSignupButton.setBackground(LIGHT_BLUE);
        githubSignupButton.setForeground(Color.BLACK);
        githubSignupButton.setFont(new Font("Arial", Font.BOLD, 14));
        githubSignupButton.setPreferredSize(new Dimension(150, 35));
        githubSignupButton.setFocusPainted(false);
        githubSignupButton.setOpaque(true);

        // Load OAuth icons
        ImageIcon googleIcon = loadScaledIcon("/ui/images/Google.png", "src/ui/images/Google.png", 20, 20);
        ImageIcon githubIcon = loadScaledIcon("/ui/images/Github.png", "src/ui/images/Github.png", 20, 20);

        // Set icons on buttons
        if (googleIcon != null) {
            googleSignupButton.setIcon(googleIcon);
            googleSignupButton.setIconTextGap(8);
        }
        if (githubIcon != null) {
            githubSignupButton.setIcon(githubIcon);
            githubSignupButton.setIconTextGap(8);
        }

        orLabel = new JLabel("or");
        orLabel.setForeground(WHITE);
        orLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        // Top panel for title
        JPanel topPanel = new JPanel();
        topPanel.setBackground(NAVY_BLUE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        topPanel.add(titleLabel);

        // Center panel for signup form
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(NAVY_BLUE);
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Email row
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 5, 10);
        centerPanel.add(emailLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 5, 10);
        centerPanel.add(emailField, gbc);

        // Password row
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 5, 10);
        centerPanel.add(passwordLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 5, 10);
        centerPanel.add(passwordField, gbc);

        // Confirm Password row
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 5, 10);
        centerPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 5, 10);
        centerPanel.add(confirmPasswordField, gbc);

        // Signup button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 0, 5, 0);
        centerPanel.add(signupButton, gbc);

        // OAuth section
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 0, 5, 0);
        centerPanel.add(orLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 50, 5, 50);
        centerPanel.add(googleSignupButton, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 50, 20, 50);
        centerPanel.add(githubSignupButton, gbc);

        // Bottom panel for back to login
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(NAVY_BLUE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

        JLabel backLabel = new JLabel("Already have an account?");
        backLabel.setForeground(WHITE);
        backLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        bottomPanel.add(backLabel);
        bottomPanel.add(backToLoginButton);

        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        // Signup button action
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSignup();
            }
        });

        // Back to login button action
        backToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBackToLogin();
            }
        });

        // Enter key on confirm password field
        confirmPasswordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSignup();
            }
        });

        // OAuth button actions
        googleSignupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleOAuthContinue("google");
            }
        });

        githubSignupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleOAuthContinue("github");
            }
        });
    }

    private void handleSignup() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Simple validation
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match",
                    "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Attempt to create account
            boolean success = authService.signup(email, password, "");

            if (success) {
                // Show success message
                JOptionPane.showMessageDialog(this,
                        "Account created successfully!\nPlease verify your email to continue.",
                        "Verification Required",
                        JOptionPane.INFORMATION_MESSAGE);

                // Create user object for verification
                User newUser = new User();
                newUser.setEmail(email);
                // Extract name from email (part before @)
                String name = email.substring(0, email.indexOf('@'));
                newUser.setName(name);

                // Open verification frame
                new VerificationFrame(newUser).setVisible(true);

                // Close signup frame
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create account. Email may already be in use.",
                        "Sign Up Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleBackToLogin() {
        // Open login frame
        new LoginFrame().setVisible(true);
        this.dispose();
    }

    private void handleOAuthContinue(String provider) {
        // Disable buttons during OAuth flow
        googleSignupButton.setEnabled(false);
        githubSignupButton.setEnabled(false);
        signupButton.setEnabled(false);

        // Run OAuth in background thread to keep UI responsive
        SwingWorker<User, Void> oauthWorker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                // Perform OAuth continue on background thread
                return "google".equals(provider) ? authService.continueWithGoogle() : authService.continueWithGitHub();
            }

            @Override
            protected void done() {
                runningOAuthWorker = null; // Clear reference when done
                try {
                    User user = get();

                    if (user != null) {
                        // OAuth users are automatically verified (trusted providers)
                        // Show success message
                        String providerName = "google".equals(provider) ? "Google" : "GitHub";
                        JOptionPane.showMessageDialog(SignupFrame.this,
                                "Successfully signed in with " + providerName + "!\nWelcome " + user.getName() + "!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);

                        // Close signup frame
                        dispose();
                    }

                } catch (Exception e) {
                    String errorMessage = e.getMessage();

                    // Handle OAuth errors with user-friendly messages
                    if (errorMessage != null && errorMessage.contains("OAuth")) {
                        JOptionPane.showMessageDialog(SignupFrame.this,
                                "OAuth authentication failed. Please try again or use regular signup.",
                                "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(SignupFrame.this, "Authentication failed: " + errorMessage,
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } finally {
                    // Re-enable buttons
                    googleSignupButton.setEnabled(true);
                    githubSignupButton.setEnabled(true);
                    signupButton.setEnabled(true);
                }
            }
        };

        // Track the running worker and start it
        runningOAuthWorker = oauthWorker;
        oauthWorker.execute();
    }

    private void setupWindowListeners() {
        // Cancel any running OAuth operations when window is closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (runningOAuthWorker != null && !runningOAuthWorker.isDone()) {
                    runningOAuthWorker.cancel(true); // Cancel the OAuth operation
                    runningOAuthWorker = null;
                }
            }
        });
    }

    /**
     * Load and scale an icon from classpath or file fallback
     */
    private ImageIcon loadScaledIcon(String classpath, String fileFallback, int w, int h) {
        try {
            java.net.URL url = SignupFrame.class.getResource(classpath);
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

    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SignupFrame().setVisible(true);
            }
        });
    }
}