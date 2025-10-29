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
    private JLabel titleLabel;
    private JLabel emailLabel;
    private JLabel passwordLabel;
    
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
        
        // Password components
        passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(WHITE);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 30));
        
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
        gbc.insets = new Insets(10, 0, 20, 0);
        centerPanel.add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 20, 0);
        centerPanel.add(loginButton, gbc);
        
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

                    if (user.getId() == -1) {
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
