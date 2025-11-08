package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private static final Color NAVY_BG      = new Color(0x1f2937); // slate-800
    private static final Color TEXT_MAIN    = new Color(0xE5E7EB); // gray-200
    private static final Color TEXT_MUTED   = new Color(0x9CA3AF); // gray-400
    private static final Color BTN_BG       = new Color(0x374151); // slate-700
    private static final Color BTN_FG       = new Color(0xF9FAFB); // gray-50
    private static final Color FIELD_BG     = new Color(0x111827); // slate-900
    private static final Color FIELD_FG     = new Color(0xF3F4F6); // gray-100
    private static final Color FIELD_BORDER = new Color(0x374151); // slate-700

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;
    private JButton googleLoginButton;
    private JButton githubLoginButton;

    public LoginFrame() {
        setTitle("Login");
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

        JLabel title = new JLabel("Welcome Back", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(TEXT_MAIN);
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(18, 8, 18, 8));
        center.setOpaque(true);
        center.setBackground(NAVY_BG);

        // form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(true);
        form.setBackground(NAVY_BG);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel emailLbl = new JLabel("Email");
        emailLbl.setForeground(TEXT_MAIN);
        emailField = compactField(18);

        JLabel pwLbl = new JLabel("Password");
        pwLbl.setForeground(TEXT_MAIN);
        passwordField = compactPasswordField(18);

        loginButton = new JButton("Log in");
        stylePrimaryButton(loginButton);
        loginButton.setForeground(Color.BLACK);

        g.gridx = 0; g.gridy = 0; form.add(emailLbl, g);
        g.gridx = 0; g.gridy = 1; form.add(emailField, g);
        g.gridx = 0; g.gridy = 2; form.add(pwLbl, g);
        g.gridx = 0; g.gridy = 3; form.add(passwordField, g);
        g.gridx = 0; g.gridy = 4; form.add(loginButton, g);

        center.add(form);
        center.add(Box.createVerticalStrut(16));

        // OAuth row
        JPanel oauth = new JPanel(new GridLayout(1, 2, 10, 0));
        oauth.setOpaque(true);
        oauth.setBackground(NAVY_BG);

        googleLoginButton = new JButton(" Continue with Google");
        googleLoginButton.setIcon(loadScaledIcon("/ui/images/Google.png", "src/ui/images/Google.png", 20, 20));
        styleSecondaryButton(googleLoginButton);
        googleLoginButton.setHorizontalAlignment(SwingConstants.LEFT);
        googleLoginButton.setForeground(Color.BLACK);

        githubLoginButton = new JButton(" Continue with GitHub");
        githubLoginButton.setIcon(loadScaledIcon("/ui/images/GitHub.png", "src/ui/images/GitHub.png", 20, 20));
        styleSecondaryButton(githubLoginButton);
        githubLoginButton.setHorizontalAlignment(SwingConstants.LEFT);
        githubLoginButton.setForeground(Color.BLACK);

        oauth.add(googleLoginButton);
        oauth.add(githubLoginButton);
        center.add(oauth);

        center.add(Box.createVerticalStrut(10));

        // footer: signup link
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomRow.setOpaque(true);
        bottomRow.setBackground(NAVY_BG);
        JLabel noAcc = new JLabel("Don't have an account?");
        noAcc.setForeground(TEXT_MUTED);
        signupButton = new JButton("Sign up");
        styleLinkishButton(signupButton);

        bottomRow.add(noAcc);
        bottomRow.add(signupButton);

        root.add(center, BorderLayout.CENTER);
        root.add(bottomRow, BorderLayout.SOUTH);
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
        b.setForeground(new Color(0x93C5FD)); // blue-300-ish
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static ImageIcon loadScaledIcon(String classpath, String fileFallback, int w, int h) {
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

    // MVC hooks
    public void setOnLogin(java.awt.event.ActionListener l)   { if (loginButton != null)   loginButton.addActionListener(l); }
    public void setOnSignup(java.awt.event.ActionListener l)  { if (signupButton != null)  signupButton.addActionListener(l); }
    public void setOnGoogle(java.awt.event.ActionListener l)  { if (googleLoginButton != null) googleLoginButton.addActionListener(l); }
    public void setOnGitHub(java.awt.event.ActionListener l)  { if (githubLoginButton != null) githubLoginButton.addActionListener(l); }

    public String getEmail()    { return (emailField != null) ? emailField.getText().trim() : ""; }
    public String getPassword() { return (passwordField != null) ? new String(passwordField.getPassword()) : ""; }

    public void setOAuthEnabled(boolean enabled) {
        if (googleLoginButton != null) googleLoginButton.setEnabled(enabled);
        if (githubLoginButton != null) githubLoginButton.setEnabled(enabled);
        if (loginButton != null)       loginButton.setEnabled(enabled);
    }

    public void showWarn(String msg, String title)  { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE); }
    public void showError(String msg, String title) { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE); }
    public void showInfo(String msg, String title)  { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE); }
}
