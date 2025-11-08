package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SignupFrame extends JFrame {
    // Theme
    private static final Color NAVY_BG      = new Color(0x1f2937);
    private static final Color TEXT_MAIN    = new Color(0xE5E7EB);
    private static final Color TEXT_MUTED   = new Color(0x9CA3AF);
    private static final Color BTN_BG       = new Color(0x374151);
    private static final Color BTN_FG       = new Color(0xF9FAFB);
    private static final Color FIELD_BG     = new Color(0x111827);
    private static final Color FIELD_FG     = new Color(0xF3F4F6);
    private static final Color FIELD_BORDER = new Color(0x374151);

    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton signupButton;
    private JButton backToLoginButton;
    private JButton googleSignupButton;
    private JButton githubSignupButton;

    public SignupFrame() {
        setTitle("Sign Up");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 500);     // OG size
        setResizable(false);
        setLocationRelativeTo(null);

        setContentPane(buildContent());
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(NAVY_BG);

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(TEXT_MAIN);
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(18, 8, 18, 8));
        center.setOpaque(true);
        center.setBackground(NAVY_BG);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(true);
        form.setBackground(NAVY_BG);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel emailLbl = new JLabel("Email");
        emailLbl.setForeground(TEXT_MAIN);
        emailField = compactField(20);

        JLabel pwLbl = new JLabel("Password");
        pwLbl.setForeground(TEXT_MAIN);
        passwordField = compactPasswordField(20);

        JLabel cpwLbl = new JLabel("Confirm Password");
        cpwLbl.setForeground(TEXT_MAIN);
        confirmPasswordField = compactPasswordField(20);

        signupButton = new JButton("Create account");
        stylePrimaryButton(signupButton);
        signupButton.setForeground(Color.BLACK);

        g.gridx = 0; g.gridy = 0; form.add(emailLbl, g);
        g.gridx = 0; g.gridy = 1; form.add(emailField, g);
        g.gridx = 0; g.gridy = 2; form.add(pwLbl, g);
        g.gridx = 0; g.gridy = 3; form.add(passwordField, g);
        g.gridx = 0; g.gridy = 4; form.add(cpwLbl, g);
        g.gridx = 0; g.gridy = 5; form.add(confirmPasswordField, g);
        g.gridx = 0; g.gridy = 6; form.add(signupButton, g);

        center.add(form);
        center.add(Box.createVerticalStrut(16));

        // OAuth
        JPanel oauth = new JPanel(new GridLayout(1, 2, 10, 0));
        oauth.setOpaque(true);
        oauth.setBackground(NAVY_BG);

        googleSignupButton = new JButton(" Continue with Google");
        googleSignupButton.setIcon(loadScaledIcon("/ui/images/Google.png", "src/ui/images/Google.png", 20, 20));
        styleSecondaryButton(googleSignupButton);
        googleSignupButton.setHorizontalAlignment(SwingConstants.LEFT);
        googleSignupButton.setForeground(Color.BLACK);

        githubSignupButton = new JButton(" Continue with GitHub");
        githubSignupButton.setIcon(loadScaledIcon("/ui/images/GitHub.png", "src/ui/images/GitHub.png", 20, 20));
        styleSecondaryButton(githubSignupButton);
        githubSignupButton.setHorizontalAlignment(SwingConstants.LEFT);
        githubSignupButton.setForeground(Color.BLACK);

        oauth.add(googleSignupButton);
        oauth.add(githubSignupButton);
        center.add(oauth);

        center.add(Box.createVerticalStrut(10));

        // Footer: back to login
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(true);
        bottom.setBackground(NAVY_BG);
        JLabel haveAcc = new JLabel("Already have an account?");
        haveAcc.setForeground(TEXT_MUTED);
        backToLoginButton = new JButton("Log in");
        styleLinkishButton(backToLoginButton);

        bottom.add(haveAcc);
        bottom.add(backToLoginButton);

        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        return root;
    }

    private JTextField compactField(int columns) {
        JTextField f = new JTextField() {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = 26;
                d.width = Math.max(d.width, 240);
                return d;
            }
            @Override public Dimension getMaximumSize() {
                Dimension d = getPreferredSize();
                return new Dimension(260, d.height);
            }
        };
        f.setColumns(columns);
        f.setFont(f.getFont().deriveFont(12f));
        f.setMargin(new Insets(2, 8, 2, 8));
        f.setBackground(FIELD_BG);
        f.setForeground(FIELD_FG);
        f.setCaretColor(FIELD_FG);
        f.setSelectionColor(new Color(0x2563EB));
        f.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(FIELD_BORDER),
                new javax.swing.border.EmptyBorder(2, 6, 2, 6)
        ));
        return f;
    }

    private JPasswordField compactPasswordField(int columns) {
        JPasswordField f = new JPasswordField() {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = 26;
                d.width = Math.max(d.width, 240);
                return d;
            }
            @Override public Dimension getMaximumSize() {
                Dimension d = getPreferredSize();
                return new Dimension(260, d.height);
            }
        };
        f.setColumns(columns);
        f.setFont(f.getFont().deriveFont(12f));
        f.setMargin(new Insets(2, 8, 2, 8));
        f.setBackground(FIELD_BG);
        f.setForeground(FIELD_FG);
        f.setCaretColor(FIELD_FG);
        f.setSelectionColor(new Color(0x2563EB));
        f.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(FIELD_BORDER),
                new javax.swing.border.EmptyBorder(2, 6, 2, 6)
        ));
        return f;
    }

    private void stylePrimaryButton(JButton b) {
        b.setBackground(BTN_BG);
        b.setForeground(BTN_FG);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
        b.setOpaque(true);
    }

    private void styleSecondaryButton(JButton b) {
        b.setBackground(new Color(0x303745));
        b.setForeground(BTN_FG);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 10, 8, 10));
        b.setOpaque(true);
    }

    private void styleLinkishButton(JButton b) {
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(new Color(0x93C5FD));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static ImageIcon loadScaledIcon(String classpath, String fileFallback, int w, int h) {
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

    // MVC hooks
    public void setOnSignup(java.awt.event.ActionListener l)      { if (signupButton != null)       signupButton.addActionListener(l); }
    public void setOnBackToLogin(java.awt.event.ActionListener l) { if (backToLoginButton != null)  backToLoginButton.addActionListener(l); }
    public void setOnGoogle(java.awt.event.ActionListener l)      { if (googleSignupButton != null) googleSignupButton.addActionListener(l); }
    public void setOnGitHub(java.awt.event.ActionListener l)      { if (githubSignupButton != null) githubSignupButton.addActionListener(l); }

    public String getEmail()           { return (emailField != null) ? emailField.getText().trim() : ""; }
    public String getPassword()        { return (passwordField != null) ? new String(passwordField.getPassword()) : ""; }
    public String getConfirmPassword() { return (confirmPasswordField != null) ? new String(confirmPasswordField.getPassword()) : ""; }

    public void setOAuthEnabled(boolean enabled) {
        if (googleSignupButton != null)  googleSignupButton.setEnabled(enabled);
        if (githubSignupButton != null)  githubSignupButton.setEnabled(enabled);
        if (signupButton != null)        signupButton.setEnabled(enabled);
    }

    public void showWarn(String msg, String title)  { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE); }
    public void showError(String msg, String title) { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE); }
    public void showInfo(String msg, String title)  { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE); }
}
