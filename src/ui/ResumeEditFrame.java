package ui;

import models.Resume;
import services.ResumeParserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Frame for editing resume content.
 * Supports viewing and editing text extracted from PDF/DOCX files.
 */
public class ResumeEditFrame extends JFrame {

    // Colors
    private static final Color BG_DARK = new Color(0x1f2937);
    private static final Color BG_CARD = new Color(0x374151);
    private static final Color TEXT_COLOR = new Color(0xF9FAFB);
    private static final Color ACCENT_GREEN = new Color(0x10B981);
    private static final Color ACCENT_RED = new Color(0xEF4444);
    private static final Color BORDER_COLOR = new Color(0x4B5563);

    private final Resume resume;
    private String originalContent;  // Not final - updated after save
    private final String fileType; // "pdf" or "docx"

    private JTextArea contentArea;
    private JButton saveButton;
    private JButton discardButton;
    private JLabel statusLabel;
    private JLabel fileTypeLabel;

    private boolean hasUnsavedChanges = false;
    private Consumer<Resume> onSaveCallback;

    public ResumeEditFrame(Resume resume) {
        super("Edit Resume - " + (resume.getFileName() != null ? resume.getFileName() : "Untitled"));
        this.resume = resume;
        this.fileType = detectFileType(resume.getFilePath());
        this.originalContent = loadContent();

        initUI();
        setupWindowListener();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 500));
    }

    /**
     * Sets the callback to be invoked when save is clicked
     */
    public void setOnSaveCallback(Consumer<Resume> callback) {
        this.onSaveCallback = callback;
    }

    /**
     * Gets the current content in the editor
     */
    public String getCurrentContent() {
        return contentArea.getText();
    }

    /**
     * Checks if there are unsaved changes
     */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content Area
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Footer with buttons
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(16, 20, 12, 20));

        // Title and file info
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(resume.getFileName() != null ? resume.getFileName() : "Resume");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        fileTypeLabel = new JLabel("File Type: " + fileType.toUpperCase());
        fileTypeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fileTypeLabel.setForeground(new Color(0x9CA3AF));
        fileTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(fileTypeLabel);

        header.add(titlePanel, BorderLayout.WEST);

        // Status indicator
        statusLabel = new JLabel("No changes");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(0x9CA3AF));
        header.add(statusLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(0, 20, 16, 20));

        // Text area for editing
        contentArea = new JTextArea(originalContent);
        contentArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        contentArea.setBackground(BG_CARD);
        contentArea.setForeground(TEXT_COLOR);
        contentArea.setCaretColor(TEXT_COLOR);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Add document listener to track changes
        contentArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkForChanges();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkForChanges();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkForChanges();
            }
        });

        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_CARD);

        // Info banner about editing
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(0x1E3A5F));
        infoPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        String infoText = fileType.equals("pdf")
                ? "📄 Editing a PDF: Changes will overwrite the original file. Note: Original formatting (fonts, layout) may differ."
                : "📝 Editing a DOCX: Changes will overwrite the original file. Note: Original formatting may differ.";

        JLabel infoLabel = new JLabel("<html>" + infoText + "</html>");
        infoLabel.setForeground(new Color(0x93C5FD));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoPanel.add(infoLabel, BorderLayout.CENTER);

        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        contentWrapper.setBorder(new EmptyBorder(10, 0, 0, 0));

        panel.add(contentWrapper, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_DARK);
        footer.setBorder(new EmptyBorder(12, 20, 16, 20));

        // Left side: file path
        JLabel pathLabel = new JLabel(resume.getFilePath() != null ? resume.getFilePath() : "");
        pathLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        pathLabel.setForeground(new Color(0x6B7280));
        footer.add(pathLabel, BorderLayout.WEST);

        // Right side: buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);

        discardButton = createStyledButton("Discard Changes", ACCENT_RED, false);
        discardButton.setEnabled(false);
        discardButton.setForeground(new Color(0x6B7280)); // Start gray (disabled)
        discardButton.addActionListener(e -> discardChanges());

        saveButton = createStyledButton("Save Changes", ACCENT_GREEN, true);
        saveButton.setEnabled(false);
        saveButton.setForeground(new Color(0x6B7280)); // Start gray (disabled)
        saveButton.addActionListener(e -> saveChanges());

        buttonPanel.add(discardButton);
        buttonPanel.add(saveButton);

        footer.add(buttonPanel, BorderLayout.EAST);

        return footer;
    }

    private JButton createStyledButton(String text, Color accentColor, boolean isSaveButton) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        // Initial styling - text is accent color, background is dark
        button.setBackground(BG_DARK);
        button.setForeground(accentColor);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 1),
                new EmptyBorder(8, 20, 8, 20)
        ));

        // Hover effects - background lightens but text stays the accent color
        Color hoverBg = new Color(
                Math.min(BG_DARK.getRed() + 30, 255),
                Math.min(BG_DARK.getGreen() + 30, 255),
                Math.min(BG_DARK.getBlue() + 30, 255)
        );

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBg);
                    // Keep text color as the accent color
                    button.setForeground(accentColor);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(BG_DARK);
                button.setForeground(accentColor);
            }
        });

        return button;
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleClose();
            }
        });
    }

    private void handleClose() {
        if (hasUnsavedChanges) {
            Object[] options = {"Save", "Discard", "Cancel"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "You have unsaved changes. What would you like to do?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            switch (choice) {
                case 0: // Save then close
                    saveChanges();
                    dispose();
                    break;
                case 1: // Discard
                    dispose();
                    break;
                case 2: // Cancel
                default:
                    // Do nothing, stay open
                    break;
            }
        } else {
            dispose();
        }
    }

    private void checkForChanges() {
        String currentContent = contentArea.getText();
        hasUnsavedChanges = !currentContent.equals(originalContent);

        saveButton.setEnabled(hasUnsavedChanges);
        discardButton.setEnabled(hasUnsavedChanges);

        // Update button text colors based on enabled state
        if (hasUnsavedChanges) {
            saveButton.setForeground(ACCENT_GREEN);    // Green text when enabled
            discardButton.setForeground(ACCENT_RED);   // Red text when enabled
            statusLabel.setText("● Unsaved changes");
            statusLabel.setForeground(new Color(0xFBBF24)); // Yellow/warning
        } else {
            saveButton.setForeground(new Color(0x6B7280));    // Gray text when disabled
            discardButton.setForeground(new Color(0x6B7280)); // Gray text when disabled
            statusLabel.setText("No changes");
            statusLabel.setForeground(new Color(0x9CA3AF));
        }
    }

    private void discardChanges() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to discard all changes?",
                "Discard Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            contentArea.setText(originalContent);
            hasUnsavedChanges = false;
            checkForChanges();
        }
    }

    private void saveChanges() {
        if (onSaveCallback != null) {
            onSaveCallback.accept(resume);
        } else {
            // Default save behavior - just update state, no popup
            originalContent = contentArea.getText();
            hasUnsavedChanges = false;
            checkForChanges();
        }
    }

    /**
     * Called after successful save to update state
     */
    public void notifySaveSuccess() {
        // Update original content to current content so change detection works correctly
        originalContent = contentArea.getText();
        hasUnsavedChanges = false;
        checkForChanges();
    }

    /**
     * Called if save fails
     */
    public void notifySaveError(String message) {
        JOptionPane.showMessageDialog(
                this,
                "Failed to save changes: " + message,
                "Save Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private String loadContent() {
        if (resume.getFilePath() == null || resume.getFilePath().isEmpty()) {
            return "";
        }

        File file = new File(resume.getFilePath());
        if (!file.exists()) {
            return "(File not found: " + resume.getFilePath() + ")";
        }

        try {
            ResumeParserService parser = new ResumeParserService();
            return parser.extractText(file);
        } catch (IOException e) {
            return "(Error reading file: " + e.getMessage() + ")";
        }
    }

    private String detectFileType(String filePath) {
        if (filePath == null) return "unknown";
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".pdf")) return "pdf";
        if (lower.endsWith(".docx")) return "docx";
        if (lower.endsWith(".doc")) return "doc";
        return "unknown";
    }
}

