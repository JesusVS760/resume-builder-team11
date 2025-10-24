package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import services.AuthService;

public class SignupFrame extends JFrame {
    
    // UI Components
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton signupButton;
    private JButton backToLoginButton;
    private JLabel titleLabel;
    private JLabel emailLabel;
    private JLabel passwordLabel;
    private JLabel confirmPasswordLabel;
    
    // Colors - macOS compatible
    private static final Color NAVY_BLUE = new Color(25, 25, 112);
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
    }
    
    private void initializeFrame() {
        setTitle("Resume Builder - Sign Up");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
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
        
        // Password components
        passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(WHITE);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 30));
        
        // Confirm Password components
        confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setForeground(WHITE);
        confirmPasswordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        confirmPasswordField.setPreferredSize(new Dimension(200, 30));
        
        // Buttons - ensure visibility on macOS
        signupButton = new JButton("Create Account");
        signupButton.setBackground(LIGHT_BLUE);
        signupButton.setForeground(Color.BLACK);  // Changed from NAVY_BLUE for better visibility
        signupButton.setFont(new Font("Arial", Font.BOLD, 14));
        signupButton.setPreferredSize(new Dimension(150, 35));
        signupButton.setFocusPainted(false);
        signupButton.setOpaque(true);  // Ensure background is painted

        backToLoginButton = new JButton("Back to Login");
        backToLoginButton.setBackground(Color.LIGHT_GRAY);  // Changed from GRAY for better contrast
        backToLoginButton.setForeground(Color.BLACK);  // Changed from WHITE for better visibility
        backToLoginButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backToLoginButton.setPreferredSize(new Dimension(120, 25));
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.setOpaque(true);  // Ensure background is painted
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
        gbc.insets = new Insets(10, 0, 5, 10);
        centerPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 5, 0);
        centerPanel.add(emailField, gbc);
        
        // Password row
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 5, 10);
        centerPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 5, 0);
        centerPanel.add(passwordField, gbc);
        
        // Confirm Password row
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 5, 10);
        centerPanel.add(confirmPasswordLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 5, 0);
        centerPanel.add(confirmPasswordField, gbc);
        
        // Signup button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 20, 0);
        centerPanel.add(signupButton, gbc);
        
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
    }
    
    private void handleSignup() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Simple validation
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in email and password fields", 
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
            // Attempt to create account (name will be auto-generated from email)
            boolean success = authService.signup(email, password, "");
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Account created successfully!\nYou can now log in.", 
                                            "Sign Up Successful", JOptionPane.INFORMATION_MESSAGE);
                handleBackToLogin();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create account. Please try again.", 
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
