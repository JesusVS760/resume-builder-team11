package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UploadPanel extends JPanel {

    private static final Color BG_WHITE       = Color.WHITE;
    private static final Color TEXT_PRIMARY   = new Color(0x111111);
    private static final Color TEXT_MUTED     = new Color(0x666666);
    private static final Color BORDER_NEUTRAL = new Color(0xD0D7DE);
    private static final Color ACCENT         = new Color(0x2563EB);

    private DropArea dropArea;
    private JTextArea jobDescArea;

    private JButton buildButton;
    private JLabel  fileLabel;
    private JProgressBar progress;

    private JButton pasteButton;
    private JButton clearButton;

    private Consumer<File> onFileDropped;
    private BiConsumer<File, String> onBuild;
    private java.awt.event.ActionListener onParseListener;

    private File selectedFile;

    public UploadPanel() {

        setLayout(new BorderLayout());
        setBackground(BG_WHITE);

        JPanel center = new JPanel(new GridLayout(1, 2, 24, 0));
        center.setBackground(BG_WHITE);
        center.setBorder(new EmptyBorder(24, 24, 0, 24));
        add(center, BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(BG_WHITE);

        JLabel leftTitle = new JLabel("Resume file");
        leftTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        leftTitle.setForeground(TEXT_PRIMARY);
        leftTitle.setFont(leftTitle.getFont().deriveFont(Font.BOLD, 14f));
        left.add(leftTitle, BorderLayout.NORTH);

        dropArea = new DropArea();
        left.add(dropArea, BorderLayout.CENTER);
        center.add(left);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG_WHITE);

        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setOpaque(false);

        JLabel rightTitle = new JLabel("Job description");
        rightTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        rightTitle.setForeground(TEXT_PRIMARY);
        rightTitle.setFont(rightTitle.getFont().deriveFont(Font.BOLD, 14f));
        rightHeader.add(rightTitle, BorderLayout.WEST);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setOpaque(false);
        pasteButton = new JButton("Paste");
        clearButton = new JButton("Clear");
        styleSecondary(pasteButton);
        styleSecondary(clearButton);
        pasteButton.addActionListener(e -> pasteFromClipboard());
        clearButton.addActionListener(e -> jobDescArea.setText(""));
        rightTools.add(pasteButton);
        rightTools.add(clearButton);
        rightHeader.add(rightTools, BorderLayout.EAST);

        right.add(rightHeader, BorderLayout.NORTH);

        jobDescArea = new JTextArea();
        jobDescArea.setLineWrap(true);
        jobDescArea.setWrapStyleWord(true);
        jobDescArea.setForeground(TEXT_PRIMARY);
        jobDescArea.setBackground(BG_WHITE);
        jobDescArea.setCaretColor(TEXT_PRIMARY);
        jobDescArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_NEUTRAL),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JScrollPane jdScroll = new JScrollPane(
                jobDescArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        right.add(jdScroll, BorderLayout.CENTER);

        JLabel charCount = new JLabel("0 characters");
        charCount.setForeground(TEXT_MUTED);
        charCount.setBorder(new EmptyBorder(6, 0, 0, 0));
        right.add(charCount, BorderLayout.SOUTH);

        jobDescArea.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                int n = jobDescArea.getText() == null ? 0 : jobDescArea.getText().length();
                charCount.setText(n + " characters");
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        center.add(right);

        JPanel south = new JPanel();
        south.setBackground(BG_WHITE);
        south.setBorder(new EmptyBorder(16, 24, 24, 24));
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));

        fileLabel = new JLabel("No file selected");
        fileLabel.setForeground(TEXT_MUTED);
        fileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        buildButton = new JButton("Build Tailored Resume");
        buildButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buildButton.addActionListener(e -> triggerBuild());
        stylePrimaryButton(buildButton);

        progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        progress.setForeground(ACCENT);
        progress.setBackground(new Color(0xF3F4F6));
        progress.setBorderPainted(false);
        progress.setVisible(false);
        progress.setAlignmentX(Component.CENTER_ALIGNMENT);
        progress.setMaximumSize(new Dimension(360, 14));

        south.add(fileLabel);
        south.add(Box.createVerticalStrut(10));
        south.add(buildButton);
        south.add(Box.createVerticalStrut(10));
        south.add(progress);

        add(south, BorderLayout.SOUTH);

        new DropTarget(dropArea, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override public void dragEnter(DropTargetDragEvent dtde) {
                if (accepts(dtde)) { dtde.acceptDrag(DnDConstants.ACTION_COPY); dropArea.setHover(true); }
                else { dtde.rejectDrag(); }
                dropArea.repaint();
            }
            @Override public void dragExit(DropTargetEvent dte) {
                dropArea.setHover(false); dropArea.repaint();
            }
            @Override public void drop(DropTargetDropEvent dtde) {
                dropArea.setHover(false);
                if (!accepts(dtde)) { dtde.rejectDrop(); return; }
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> files = (List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (files != null && !files.isEmpty()) setSelectedFile(files.get(0));
                    dtde.dropComplete(true);
                } catch (Exception ex) {
                    dtde.dropComplete(false);
                    showError("Couldn't read dropped file: " + ex.getMessage(), "Drop Failed");
                }
            }
        }, true, null);
    }

    public void setOnFileDropped(Consumer<File> c) { this.onFileDropped = c; }
    public void setOnBuild(BiConsumer<File, String> c) { this.onBuild = c; }
    public void setOnParse(java.awt.event.ActionListener l) { this.onParseListener = l; }

    public File getSelectedFile() { return selectedFile; }
    public void setSelectedFile(File f) {
        this.selectedFile = f;
        fileLabel.setText(f == null ? "No file selected" : f.getName());
        if (f != null && onFileDropped != null) onFileDropped.accept(f);
        repaint();
    }

    public String getJobDescription() { return jobDescArea.getText(); }
    public void setJobDescription(String txt) { jobDescArea.setText(txt == null ? "" : txt); }

    public void setBusy(boolean busy) {
        progress.setIndeterminate(busy);
        progress.setVisible(busy || progress.getValue() > 0);
        buildButton.setEnabled(!busy);
        dropArea.setEnabled(!busy);
        jobDescArea.setEnabled(!busy);
        pasteButton.setEnabled(!busy);
        clearButton.setEnabled(!busy);
    }

    public void setProgressValue(int v) {
        progress.setIndeterminate(false);
        progress.setValue(Math.max(0, Math.min(100, v)));
        progress.setVisible(v > 0 && v < 100);
    }

    public void setStatus(String text) { fileLabel.setText(text); }

    public void showWarn(String msg, String title)  { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE); }
    public void showError(String msg, String title) { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE); }
    public void showInfo(String msg, String title)  { JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE); }

    private void triggerBuild() {
        // Validate inputs on the view side
        if (selectedFile == null) {
            showWarn("Please select or drop a resume file first.", "No file selected");
            return;
        }

        String jobDesc = getJobDescription();
        if (jobDesc == null || jobDesc.trim().isEmpty()) {
            showWarn("Please enter a job description.", "No job description");
            return;
        }

        // Prefer the controller callback if it was wired
        if (onBuild != null) {
            onBuild.accept(selectedFile, jobDesc);
            return;
        }

        // Fallback: allow a simple parse listener if someone is using it
        if (onParseListener != null) {
            onParseListener.actionPerformed(
                    new java.awt.event.ActionEvent(this, 0, "parse")
            );
            return;
        }

        // If neither is configured, let the user know
        showError("No handler is configured to process the resume.", "Not Configured");
    }

    /**
     * Called by the controller when a tailored resume string is ready.
     * It reuses the existing parsing/split logic to show the two-panel dialog.
     */
    public void showTailoringResult(String result) {
        if (result == null || result.isBlank()) {
            showWarn("Tailored resume text is empty.", "No content");
            return;
        }

        String[] sections = parseResult(result);
        String feedback = sections[0];
        String tailoredResume = sections[1];

        showSplitResultDialog(feedback, tailoredResume);
        setStatus("Resume tailored successfully!");
    }


    /**
     * Parse the result string to separate feedback from tailored resume
     */
    private String[] parseResult(String result) {
        StringBuilder feedback = new StringBuilder();
        StringBuilder tailoredResume = new StringBuilder();

        String[] lines = result.split("\n");
        boolean inResumeSection = false;
        boolean foundFirstResumeHeader = false;

        for (String line : lines) {
            String trimmed = line.trim();

            // Skip empty lines at the beginning
            if (!foundFirstResumeHeader && trimmed.isEmpty()) {
                continue;
            }

            // Feedback indicators - always goes to left panel
            if (trimmed.startsWith("Job Match Score:") ||
                    trimmed.startsWith("Keywords Matched:") ||
                    trimmed.contains("indicates strong alignment") ||
                    trimmed.startsWith("Summary:") ||
                    trimmed.startsWith("Comments:")) {
                feedback.append(line).append("\n");
                inResumeSection = false;
                continue;
            }

            // Detect first resume header (person's name or all-caps section)
            // This marks the start of actual resume content
            if (!foundFirstResumeHeader) {
                // Look for name line (mixed case, reasonable length) or section headers (all caps)
                if ((trimmed.length() > 2 && trimmed.length() < 50 &&
                        !trimmed.contains(":") && !trimmed.contains("★")) ||
                        (trimmed.matches("^[A-Z\\s]{3,}$") && trimmed.length() > 2)) {
                    foundFirstResumeHeader = true;
                    inResumeSection = true;
                }
            }

            // Once in resume section, process each line
            if (foundFirstResumeHeader) {
                // Remove ★ symbols from resume content
                String cleanLine = line.replace("★", "").trim();

                // Skip lines that are purely commentary
                if (cleanLine.toLowerCase().startsWith("comment:") ||
                        cleanLine.toLowerCase().startsWith("note:") ||
                        cleanLine.toLowerCase().startsWith("suggestion:")) {
                    feedback.append(line).append("\n");
                    continue;
                }

                // Add to resume, preserving formatting
                if (cleanLine.isEmpty()) {
                    tailoredResume.append("\n");
                } else {
                    tailoredResume.append(cleanLine).append("\n");
                }
            } else {
                // Before resume starts, everything is feedback
                feedback.append(line).append("\n");
            }
        }

        return new String[] { feedback.toString().trim(), tailoredResume.toString().trim() };
    }

    /**
     * Show dialog with feedback panel on left and tailored resume on right
     */
    private void showSplitResultDialog(String feedback, String tailoredResume) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Tailored Resume Result", true);
        dialog.setLayout(new BorderLayout());

        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300); // Left panel width
        splitPane.setResizeWeight(0.3); // 30% for feedback, 70% for resume

        // LEFT PANEL: Feedback
        JPanel feedbackPanel = createFeedbackPanel(feedback);
        splitPane.setLeftComponent(feedbackPanel);

        // RIGHT PANEL: Tailored Resume (clean, downloadable)
        JPanel resumePanel = createResumePanel(tailoredResume);
        splitPane.setRightComponent(resumePanel);

        dialog.add(splitPane, BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(BG_WHITE);

        JButton closeBtn = new JButton("Close");
        styleSecondary(closeBtn);
        closeBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(closeBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Create feedback panel (left side)
     */
    private JPanel createFeedbackPanel(String feedback) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xF9FAFB));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_NEUTRAL),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("Analysis & Feedback");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Feedback text area
        JTextArea feedbackArea = new JTextArea(feedback);
        feedbackArea.setEditable(false);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setBackground(new Color(0xF9FAFB));
        feedbackArea.setForeground(TEXT_PRIMARY);
        feedbackArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        feedbackArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_NEUTRAL));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create resume panel (right side - clean for future download)
     */
    private JPanel createResumePanel(String tailoredResume) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Title
        JLabel titleLabel = new JLabel("Tailored Resume");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Resume text area
        JTextArea resumeArea = new JTextArea(tailoredResume);
        resumeArea.setEditable(false);
        resumeArea.setLineWrap(true);
        resumeArea.setWrapStyleWord(true);
        resumeArea.setBackground(Color.WHITE);
        resumeArea.setForeground(TEXT_PRIMARY);
        resumeArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resumeArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(resumeArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_NEUTRAL));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void pasteFromClipboard() {
        try {
            var cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (cb.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                String txt = (String) cb.getData(DataFlavor.stringFlavor);
                if (txt != null) jobDescArea.replaceSelection(txt);
            } else {
                showWarn("No text available in clipboard.", "Paste");
            }
        } catch (Exception ex) {
            showError("Paste failed: " + ex.getMessage(), "Paste");
        }
    }

    private static boolean accepts(DropTargetDragEvent e) { return e.isDataFlavorSupported(DataFlavor.javaFileListFlavor); }
    private static boolean accepts(DropTargetDropEvent e) { return e.isDataFlavorSupported(DataFlavor.javaFileListFlavor); }

    private void styleSecondary(JButton b) {
        b.setBackground(BG_WHITE);
        b.setForeground(TEXT_PRIMARY);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_NEUTRAL),
                new EmptyBorder(6, 10, 6, 10)
        ));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void stylePrimaryButton(JButton b) {
        // Flat button so our colors show correctly
        b.setUI(new BasicButtonUI());
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);

        Color normal  = new Color(0x374151); // base slate
        Color hover   = new Color(0x4B5563); // lighter on hover
        Color pressed = new Color(0x1F2937); // darker on press

        b.setBackground(normal);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // nicer readable font
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover + press color behavior
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(normal);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                b.setBackground(pressed);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (b.getBounds().contains(e.getPoint())) {
                    b.setBackground(hover);
                } else {
                    b.setBackground(normal);
                }
            }
        });
    }


    private class DropArea extends JPanel {
        private boolean hover = false;

        DropArea() {
            setOpaque(true);
            setBackground(BG_WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 8, 8, 8));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    chooseFileWithDialog();
                }
            });
        }

        void setHover(boolean h) { this.hover = h; }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            int w = getWidth(), h = getHeight();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(0xe6e6e6));
            g.fillRect(0, 0, w, h);

            // Simple solid border (no rounded corners, no dash)
            g.setColor(hover ? ACCENT : BORDER_NEUTRAL);
            g.setStroke(new BasicStroke(2f));
            g.drawRect(1, 1, w - 2, h - 2);

            // Text lines
            String t1 = "Drag & Drop your resume";
            String t2 = "(in PDF, DOC, or DOCX format)";
            String t3 = "or click to choose file";

            Font f1 = new Font("Segoe UI", Font.BOLD, 16);   // main line
            Font f2 = new Font("Segoe UI", Font.PLAIN, 13);  // format line
            Font f3 = new Font("Segoe UI", Font.PLAIN, 12);  // hint line

            FontMetrics fm1 = g.getFontMetrics(f1);
            FontMetrics fm2 = g.getFontMetrics(f2);
            FontMetrics fm3 = g.getFontMetrics(f3);

            int gap = 4;
            int totalHeight = fm1.getHeight() + fm2.getHeight() + fm3.getHeight() + gap * 2;
            int y = (h - totalHeight) / 2 + fm1.getAscent();

            g.setFont(f1);
            g.setColor(TEXT_PRIMARY);
            drawCentered(g, t1, w, y);

            y += fm1.getHeight() + gap;
            g.setFont(f2);
            g.setColor(TEXT_MUTED);
            drawCentered(g, t2, w, y);

            y += fm2.getHeight() + gap;
            g.setFont(f3);
            g.setColor(TEXT_MUTED);
            drawCentered(g, t3, w, y);

            g.dispose();
        }

        private void drawCentered(Graphics2D g, String s, int width, int y) {
            FontMetrics fm = g.getFontMetrics();
            int x = (width - fm.stringWidth(s)) / 2;
            g.drawString(s, x, y);
        }

        private void chooseFileWithDialog() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose a resume file");
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Documents (*.pdf, *.doc, *.docx, *.txt)", "pdf", "doc", "docx", "txt"
            ));
            if (chooser.showOpenDialog(UploadPanel.this) == JFileChooser.APPROVE_OPTION) {
                setSelectedFile(chooser.getSelectedFile());
            }
        }
    }
}