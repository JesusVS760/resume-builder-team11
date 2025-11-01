package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import services.AuthService;
import models.User;

public class LoginFrame extends JFrame {
    
    // UI Components
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;
    private JButton googleLoginButton;
    private JButton githubLoginButton;
    private JLabel titleLabel;
    private JLabel emailLabel;
    private JLabel passwordLabel;
    private JLabel orLabel;

    // Track running OAuth operations
    private SwingWorker<?, ?> runningOAuthWorker;
    
    // Colors
    private static final Color NAVY_BLUE = new Color(0x1f2937);
    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color WHITE = Color.WHITE;
    
    // Services
    private AuthService authService;
    
    public LoginFrame() {
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
        setTitle(" Tailored Resume Builder - Login");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(NAVY_BLUE);
    }
    
    private void createComponents() {
        // Title
        titleLabel = new JLabel("Tailored Resume Builder");
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
        
        // Buttons
        loginButton = new JButton("Login");
        loginButton.setBackground(LIGHT_BLUE);
        loginButton.setForeground(Color.BLACK);  // Changed for better macOS visibility
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(true);  // Ensure background is painted

        signupButton = new JButton("Sign Up");
        signupButton.setBackground(Color.LIGHT_GRAY);  // Changed for better contrast
        signupButton.setForeground(Color.BLACK);  // Changed for better visibility
        signupButton.setFont(new Font("Arial", Font.PLAIN, 12));
        signupButton.setPreferredSize(new Dimension(80, 25));
        signupButton.setFocusPainted(false);
        signupButton.setOpaque(true);  // Ensure background is painted

        // OAuth buttons - unified continue flow
        googleLoginButton = new JButton("Continue with Google");
        googleLoginButton.setBackground(LIGHT_BLUE);
        googleLoginButton.setForeground(Color.BLACK);
        googleLoginButton.setFont(new Font("Arial", Font.BOLD, 14));
        googleLoginButton.setPreferredSize(new Dimension(120, 35));
        googleLoginButton.setFocusPainted(false);
        googleLoginButton.setOpaque(true);

        githubLoginButton = new JButton("Continue with GitHub");
        githubLoginButton.setBackground(LIGHT_BLUE);
        githubLoginButton.setForeground(Color.BLACK);
        githubLoginButton.setFont(new Font("Arial", Font.BOLD, 14));
        githubLoginButton.setPreferredSize(new Dimension(120, 35));
        githubLoginButton.setFocusPainted(false);
        githubLoginButton.setOpaque(true);

        // Load OAuth icons
        ImageIcon googleIcon = loadScaledIcon("/ui/images/Google.png", "src/ui/images/Google.png", 20, 20);
        ImageIcon githubIcon = loadScaledIcon("/ui/images/Github.png", "src/ui/images/Github.png", 20, 20);

        // Set icons on buttons
        if (googleIcon != null) {
            googleLoginButton.setIcon(googleIcon);
            googleLoginButton.setIconTextGap(8); // Space between icon and text
        }
        if (githubIcon != null) {
            githubLoginButton.setIcon(githubIcon);
            githubLoginButton.setIconTextGap(8); // Space between icon and text
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
        topPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        topPanel.add(titleLabel);
        
        // Center panel for login form
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(NAVY_BLUE);
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Email row
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 20, 2, 5);
        centerPanel.add(emailLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(10, 5, 5, 20);
        centerPanel.add(emailField, gbc);

        // Password row
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(8, 20, 5, 5);
        centerPanel.add(passwordLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(8, 5, 20, 20);
        centerPanel.add(passwordField, gbc);

        // Login button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0);
        centerPanel.add(loginButton, gbc);

        // "or" label
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0);
        centerPanel.add(orLabel, gbc);

        // Google OAuth button
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 0, 5, 0);
        centerPanel.add(googleLoginButton, gbc);

        // GitHub OAuth button
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 0, 20, 0);
        centerPanel.add(githubLoginButton, gbc);

        // Bottom panel for signup
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(NAVY_BLUE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 50, 0));
        
        JLabel signupLabel = new JLabel("Don't have an account?");
        signupLabel.setForeground(WHITE);
        signupLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        bottomPanel.add(signupLabel);
        bottomPanel.add(signupButton);
        
        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        // Login button action
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        
        // Signup button action
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSignup();
            }
        });

        // Google OAuth button action
        googleLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleGoogleContinue();
            }
        });

        // GitHub OAuth button action
        githubLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleGitHubContinue();
            }
        });

        // Enter key on password field
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
    }
    
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Simple validation
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password", 
                                        "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Attempt to login
            User user = authService.login(email, password);
            
                if (user != null) {
                    // Store user session
                    utils.Constants.Session.login(user);

                    if ("ADMIN".equals(user.getId())) {
                        // Admin login
                        JOptionPane.showMessageDialog(this, "Welcome Administrator! You have admin privileges.",
                                                    "Admin Login Successful", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // Regular user login
                        JOptionPane.showMessageDialog(this, "Welcome " + user.getName(),
                                                    "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                    }

                    // Close login frame - user can now click Profile button on homepage
                    dispose();
                } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password", 
                                            "Login Failed", JOptionPane.WARNING_MESSAGE);
                passwordField.setText("");
            }
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleSignup() {
        // Open signup frame
        new SignupFrame().setVisible(true);
        this.dispose();
    }

    private void handleGoogleContinue() {
        // Disable buttons during OAuth flow
        googleLoginButton.setEnabled(false);
        githubLoginButton.setEnabled(false);
        loginButton.setEnabled(false);

        // Run OAuth in background thread to keep UI responsive
        SwingWorker<User, Void> oauthWorker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                // Attempt Google OAuth continue on background thread
                return authService.continueWithGoogle();
            }

            @Override
            protected void done() {
                runningOAuthWorker = null; // Clear reference when done
                try {
                    User user = get();

                    if (user != null) {
                        // Show success message
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Welcome " + user.getName() + "! You have successfully signed in with Google.",
                            "Google Login Successful", JOptionPane.INFORMATION_MESSAGE);

                        // Close login frame
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this, "Google authentication failed. Please try again.",
                                                    "Login Failed", JOptionPane.WARNING_MESSAGE);
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Google authentication failed: " + e.getMessage(),
                                                "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Re-enable buttons
                    googleLoginButton.setEnabled(true);
                    githubLoginButton.setEnabled(true);
                    loginButton.setEnabled(true);
                }
            }
        };

        // Track the running worker and start it
        runningOAuthWorker = oauthWorker;
        oauthWorker.execute();
    }

    private void handleGitHubContinue() {
        // Disable buttons during OAuth flow
        googleLoginButton.setEnabled(false);
        githubLoginButton.setEnabled(false);
        loginButton.setEnabled(false);

        // Run OAuth in background thread to keep UI responsive
        SwingWorker<User, Void> oauthWorker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                // Attempt GitHub OAuth continue on background thread
                return authService.continueWithGitHub();
            }

            @Override
            protected void done() {
                runningOAuthWorker = null; // Clear reference when done
                try {
                    User user = get();

                    if (user != null) {
                        // Show success message
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Welcome " + user.getName() + "! You have successfully signed in with GitHub.",
                            "GitHub Login Successful", JOptionPane.INFORMATION_MESSAGE);

                        // Close login frame
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this, "GitHub authentication failed. Please try again.",
                                                    "Login Failed", JOptionPane.WARNING_MESSAGE);
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "GitHub authentication failed: " + e.getMessage(),
                                                "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Re-enable buttons
                    googleLoginButton.setEnabled(true);
                    githubLoginButton.setEnabled(true);
                    loginButton.setEnabled(true);
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
            java.net.URL url = LoginFrame.class.getResource(classpath);
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
                new LoginFrame().setVisible(true);
            }
        });
    }
}
