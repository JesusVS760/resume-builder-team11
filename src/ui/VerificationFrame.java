package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VerificationFrame extends JFrame {
    private static final Color NAVY_BG     = new Color(0x1f2937);
    private static final Color TEXT_MAIN   = new Color(0xE5E7EB);
    private static final Color TEXT_MUTED  = new Color(0x9CA3AF);
    private static final Color BTN_BG      = new Color(0x374151);
    private static final Color BTN_FG      = new Color(0xF9FAFB);
    private static final Color FIELD_BG    = new Color(0x111827);
    private static final Color FIELD_FG    = new Color(0xF3F4F6);
    private static final Color FIELD_BORDER= new Color(0x374151);

    private JTextField emailField;
    private JTextField codeField;
    private JButton sendCodeButton;
    private JButton verifyButton;

    private String verificationToken = "";

    public VerificationFrame() {
        setTitle("Verify Email");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 500); // OG size
        setResizable(false);
        setLocationRelativeTo(null);

        setContentPane(buildContent());
    }

    public VerificationFrame(String email, String token) {
        this();
        if (emailField != null) emailField.setText(email);
        this.verificationToken = (token == null ? "" : token);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(NAVY_BG);

        // Title
        JLabel title = new JLabel("Email Verification", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(TEXT_MAIN);
        root.add(title, BorderLayout.NORTH);

        // CENTER (Email + Send Code)
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(true);
        center.setBackground(NAVY_BG);

        JLabel emailLbl = new JLabel("Email", SwingConstants.CENTER);
        emailLbl.setForeground(TEXT_MAIN);
        emailLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailField = new JTextField() {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = 26;                // compact height
                d.width = Math.max(d.width, 240);
                return d;
            }
            @Override public Dimension getMinimumSize() {
                Dimension d = getPreferredSize();
                return new Dimension(180, d.height);
            }
            @Override public Dimension getMaximumSize() {
                Dimension d = getPreferredSize();
                return new Dimension(260, d.height);
            }
        };
        emailField.setColumns(18);
        emailField.setFont(emailField.getFont().deriveFont(12f));
        emailField.setMargin(new Insets(2, 8, 2, 8));
        emailField.setBackground(FIELD_BG);
        emailField.setForeground(FIELD_FG);
        emailField.setCaretColor(FIELD_FG);
        emailField.setSelectionColor(new Color(0x2563EB));
        emailField.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(FIELD_BORDER),
                new javax.swing.border.EmptyBorder(2, 6, 2, 6)
        ));

        sendCodeButton = new JButton("Send Code");
        stylePrimaryButton(sendCodeButton);
        sendCodeButton.setForeground(Color.BLACK);

        // Row: [ emailField ][ sendCode ]
        JPanel emailRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        emailRow.setOpaque(true);
        emailRow.setBackground(NAVY_BG);
        emailRow.add(emailField);
        emailRow.add(sendCodeButton);

        center.add(Box.createVerticalStrut(8));
        center.add(emailLbl);
        center.add(Box.createVerticalStrut(8));
        center.add(emailRow);
        center.add(Box.createVerticalGlue()); // keep this in the center area

        root.add(center, BorderLayout.CENTER);

        // SOUTH (Enter Code + Verify)
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setOpaque(true);
        south.setBackground(NAVY_BG);

        JLabel codeLbl = new JLabel("Enter Code (6 digits)", SwingConstants.CENTER);
        codeLbl.setForeground(TEXT_MAIN);
        codeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        codeField = new JTextField();
        codeField.setColumns(6);
        Dimension sz = codeField.getPreferredSize();
        codeField.setPreferredSize(new Dimension(120, sz.height));
        codeField.setMaximumSize(new Dimension(120, sz.height));
        codeField.setBackground(FIELD_BG);
        codeField.setForeground(FIELD_FG);
        codeField.setCaretColor(FIELD_FG);
        codeField.setSelectionColor(new Color(0x2563EB));
        codeField.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(FIELD_BORDER),
                new javax.swing.border.EmptyBorder(2, 6, 2, 6)
        ));

        verifyButton = new JButton("Verify");
        stylePrimaryButton(verifyButton);
        verifyButton.setForeground(Color.BLACK);

        JPanel codeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        codeRow.setOpaque(true);
        codeRow.setBackground(NAVY_BG);
        codeRow.add(codeField);
        codeRow.add(verifyButton);

        JLabel hint = new JLabel("<html><div style='color:#9CA3AF;'>Code expires in 10 minutes.</div></html>", SwingConstants.CENTER);
        hint.setForeground(TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        south.add(codeLbl);
        south.add(Box.createVerticalStrut(8));
        south.add(codeRow);
        south.add(Box.createVerticalStrut(8));
        south.add(hint);

        root.add(south, BorderLayout.SOUTH);

        return root;
    }

    private void stylePrimaryButton(JButton b) {
        b.setBackground(BTN_BG);
        b.setForeground(BTN_FG);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
        b.setOpaque(true);
    }

    // MVC hooks
    public void setOnSendCode(java.awt.event.ActionListener l)   { if (sendCodeButton != null)  sendCodeButton.addActionListener(l); }
    public void setOnVerifyCode(java.awt.event.ActionListener l) { if (verifyButton != null)    verifyButton.addActionListener(l); }

    public String getEmail()       { return (emailField != null) ? emailField.getText().trim() : ""; }
    public String getEnteredCode() { return (codeField != null)  ? codeField.getText().trim()  : ""; }

    public String getVerificationToken() { return verificationToken != null ? verificationToken : ""; }
    public void setVerificationToken(String token) { this.verificationToken = (token == null ? "" : token); }

    public void setInputsEnabled(boolean enabled) {
        if (emailField != null)     emailField.setEnabled(enabled);
        if (codeField != null)      codeField.setEnabled(enabled);
        if (sendCodeButton != null) sendCodeButton.setEnabled(enabled);
        if (verifyButton != null)   verifyButton.setEnabled(enabled);
    }

    public void showError(String msg, String title) { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE); }
    public void showInfo(String msg, String title)  { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE); }
}
