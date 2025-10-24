package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import services.TwilioService;
import models.User;

public class VerificationFrame extends JFrame {

    private JTextField phoneField;
    private JTextField codeField;
    private JButton sendCodeButton;
    private JButton verifyButton;
    private JLabel titleLabel;
    private JLabel phoneLabel;
    private JLabel codeLabel;
    private JLabel statusLabel;


    private static final Color NAVY_BLUE = new Color(111, 111, 222);
    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color GREEN = new Color(34, 139, 34);
    private static final Color WHITE = Color.WHITE;

    private TwilioService twilioService; // service that sends SMS codes via Twilio
    private User currentUser; // user trying to verify their phone
    private boolean codeWasSent;

    public VerificationFrame(User user) {
        this.currentUser = user;
        this.twilioService = new TwilioService();
        this.codeWasSent = false;
        initializeFrame();                          // Set up the window basics
        createComponents();                         // Create all the UI elements
        layoutComponents();                         // Arrange them on the screen
        setupEventListeners();

    }

    private void initializeFrame() {
        setTitle("Resume Builder - Phone Verification");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450,500); // window size
        setLocationRelativeTo(null); // Center the window to screen
        setResizable(false); // Don't allow user to resize ?? may change ??
        getContentPane().setBackground(NAVY_BLUE);
    }

    // COMPONENTS

    private void createComponents() {
        titleLabel = new JLabel("Phone Verification");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBackground(WHITE);
        titleLabel.setForeground(WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        //PHONE NUMBER LABEL & INPUT FIELDS

        phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setForeground(WHITE);
        phoneLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        phoneField = new JTextField(20);
        phoneField.setFont(new Font("Arial", Font.PLAIN, 14));
        phoneField.setPreferredSize(new Dimension(250, 30));
        phoneField.setToolTipText("Format: +1234567890");  // Helper text

        // SEND CODE BUTTON

        sendCodeButton = new JButton("Send Code");
        sendCodeButton.setBackground(LIGHT_BLUE);
        sendCodeButton.setForeground(NAVY_BLUE);
        sendCodeButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendCodeButton.setPreferredSize(new Dimension(150, 35));
        sendCodeButton.setFocusPainted(false);

        // VERIFICATION CODE LABEL & TEXT FIELD
        codeLabel = new JLabel("Verification Code:");
        codeLabel.setForeground(WHITE);
        codeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        codeField = new JTextField(20);
        codeField.setFont(new Font("Arial", Font.PLAIN, 14));
        codeField.setPreferredSize(new Dimension(250, 30));
        codeField.setEditable(false); // DISABLED


        // VERIFY BUTTON

        verifyButton = new JButton("Verification Code");
        verifyButton.setBackground(GREEN);
        verifyButton.setForeground(WHITE);
        verifyButton.setFont(new Font("Arial", Font.BOLD, 14));
        verifyButton.setPreferredSize(new Dimension(150, 35));
        verifyButton.setFocusPainted(false);
        verifyButton.setEnabled(false); // DISABLED

        // STATUS LABEL

        statusLabel = new JLabel("Enter your phone number and click 'Send Code'");
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
        topPanel.setBorder(BorderFactory.createEmptyBorder(40,0,30,0));
        topPanel.add(titleLabel);


        // CENTER SECTION

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(NAVY_BLUE);
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);

        //Phone Label Field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(phoneLabel, gbc);

        //Phone Input Field
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(phoneField, gbc);

        // Send Code
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        centerPanel.add(sendCodeButton, gbc);

        // Spacer (empty row for spacing)
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 10, 20, 10);
        centerPanel.add(new JLabel(""), gbc);

        // Code Label

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(codeLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(codeField, gbc);

        // Verification Button
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10,10,10,10);
        centerPanel.add(verifyButton, gbc);

        // BOTTOM SECTION

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(NAVY_BLUE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20,20,50,20));
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        // add to window
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

    }

    private void setupEventListeners() {

        sendCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSendCode(); // send code
            }
        });

        verifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleVerifyCode();  // Verify the code user entered
            }
        });
    }

    private void handleSendCode() {
        String phoneNumber = phoneField.getText().trim();

        if(phoneNumber.isEmpty()) {
            showError("Please enter a phone number");
            return;
        }

        try {
            // update status message
            updateStatus("Sending verification code...", LIGHT_BLUE);

            // ask TwilioService to sen SMS code to the phone number
            boolean codeSent = twilioService.sendVerificationCode(phoneNumber);

            if(codeSent) {

                codeWasSent = true;
                phoneField.setEditable(false);
                codeField.setEditable(true);
                verifyButton.setEnabled(true);
                updateStatus("Verification Code sent! Check your phone.", GREEN);
            } else {
                showError("Failed to send verification code! Try Again.");
            }
        } catch (Exception e) {
            showError("Error:" + e.getMessage());

        }


    }


    private void handleVerifyCode() {
        String enteredCode = codeField.getText().trim();


        if(enteredCode.isEmpty()) {
            showError("Please enter verification code");
            return;
        }

        try {
            updateStatus("Verifying code...", LIGHT_BLUE);

            // Ask TwilioService to verify if the code is correct
            boolean isValid = twilioService.verifyCode(enteredCode);

            if (isValid) {
                // Success - phone is verified
                updateStatus("Phone verified successfully!", GREEN);
                showSuccess("Your phone has been verified!");

                // Store that this user's phone is verified
                currentUser.setPhoneVerified(true);

                // Close this window after a short delay
                Timer timer = new Timer(1500, e -> this.dispose());
                timer.setRepeats(false);
                timer.start();
            } else {
                // Failed - code was wrong
                showError("Invalid verification code. Try again.");
                codeField.setText("");  // Clear the code field
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());

        }

    }


    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        updateStatus(message, new Color(255, 0 ,0));
    }


    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatus(String message,Color color) {
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

