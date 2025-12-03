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
    private static final Color BRIGHT_GREEN = new Color(0, 180, 0);

    private JTextField emailField;
    private JTextField codeField;
    private JButton sendCodeButton;
    private JButton verifyButton;
    private JLabel statusLabel;

    private String verificationToken = "";
    private boolean autoSendMode = false;
    private String prefilledEmail = "";

    public VerificationFrame() {
        setTitle("Verify Email");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 500);
        setResizable(false);
        setLocationRelativeTo(null);

        setContentPane(buildContent(false));
    }

    public VerificationFrame(String email, String token) {
        setTitle("Verify Email");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 350); // Smaller since we hide email section
        setResizable(false);
        setLocationRelativeTo(null);
        
        this.autoSendMode = true;
        this.prefilledEmail = (email == null ? "" : email);
        this.verificationToken = (token == null ? "" : token);
        
        setContentPane(buildContent(true));
    }

    private JPanel buildContent(boolean hideEmailSection) {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(NAVY_BG);
        
        // Create a centered content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Title
        JLabel title = new JLabel("Email Verification", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(title);
        contentPanel.add(Box.createVerticalStrut(30));

        // Email field (always create, but only show if not hiding)
        emailField = new JTextField() {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = 26;
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

        // Only show email section if not in auto-send mode
        if (!hideEmailSection) {
            JLabel emailLbl = new JLabel("Email", SwingConstants.CENTER);
            emailLbl.setForeground(TEXT_MAIN);
            emailLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JPanel emailRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            emailRow.setOpaque(false);
            emailRow.add(emailField);
            emailRow.add(sendCodeButton);

            contentPanel.add(emailLbl);
            contentPanel.add(Box.createVerticalStrut(8));
            contentPanel.add(emailRow);
            contentPanel.add(Box.createVerticalStrut(20));
        } else {
            // In auto-send mode, set the prefilled email
            emailField.setText(prefilledEmail);
        }

        // Enter Code label
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
        
        // Add hover effect with bright green
        verifyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                verifyButton.setBackground(BRIGHT_GREEN);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                verifyButton.setBackground(BTN_BG);
            }
        });

        JPanel codeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        codeRow.setOpaque(false);
        codeRow.add(codeField);
        codeRow.add(verifyButton);

        // Status label - shows sending status in auto-send mode
        statusLabel = new JLabel(" ", SwingConstants.CENTER); // Space to reserve height
        statusLabel.setForeground(BRIGHT_GREEN);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Code expires in 10 minutes.", SwingConstants.CENTER);
        hint.setForeground(TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(codeLbl);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(codeRow);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(statusLabel);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(hint);

        // Add content panel to root (centered by GridBagLayout)
        root.add(contentPanel);

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

    public boolean isAutoSendMode() { return autoSendMode; }
    
    public void setStatusText(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }
    
    public void setStatusColor(Color color) {
        if (statusLabel != null) {
            statusLabel.setForeground(color);
        }
    }

    public void setInputsEnabled(boolean enabled) {
        if (emailField != null)     emailField.setEnabled(enabled);
        if (codeField != null)      codeField.setEnabled(enabled);
        if (sendCodeButton != null) sendCodeButton.setEnabled(enabled);
        if (verifyButton != null)   verifyButton.setEnabled(enabled);
    }

    public void showError(String msg, String title) { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE); }
    public void showInfo(String msg, String title)  { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE); }
}
