package ui;

import services.ResumeTailoringService;
import services.ResumeParserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.nio.file.Files;
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

    // Services for tailoring
    private ResumeTailoringService tailoringService;
    private ResumeParserService parserService;

    public UploadPanel() {
        // Initialize services
        tailoringService = new ResumeTailoringService();
        parserService = new ResumeParserService();

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
        buildButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_NEUTRAL),
                new EmptyBorder(10, 16, 10, 16)
        ));
        buildButton.setBackground(BG_WHITE);
        buildButton.setFocusPainted(false);
        buildButton.setOpaque(true);

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
        if (onParseListener != null) {
            onParseListener.actionPerformed(new java.awt.event.ActionEvent(this, 0, "parse"));
            return;
        }

        // Default behavior: use tailoring service
        if (selectedFile == null) {
            showWarn("Please select or drop a resume file first.", "No file selected");
            return;
        }

        String jobDesc = getJobDescription();
        if (jobDesc == null || jobDesc.trim().isEmpty()) {
            showWarn("Please enter a job description.", "No job description");
            return;
        }

        // Process in background thread
        setBusy(true);
        setStatus("Processing resume...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // Parse resume completely with all sections
                setStatus("Parsing resume...");
                ResumeParserService.ParsedResume parsed = parserService.parseResumeComplete(selectedFile);

                // Tailor resume
                setStatus("Tailoring resume...");
                String tailored = tailoringService.tailorResume(parsed, jobDesc);

                return tailored;
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    String result = get();

                    // Display results in a dialog
                    JTextArea resultArea = new JTextArea(result);
                    resultArea.setEditable(false);
                    resultArea.setLineWrap(true);
                    resultArea.setWrapStyleWord(true);
                    resultArea.setCaretPosition(0);

                    JScrollPane scrollPane = new JScrollPane(resultArea);
                    scrollPane.setPreferredSize(new Dimension(700, 500));

                    JOptionPane.showMessageDialog(
                            UploadPanel.this,
                            scrollPane,
                            "Tailored Resume Result",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    setStatus("Resume tailored successfully!");

                } catch (Exception ex) {
                    showError("Failed to tailor resume: " + ex.getMessage(), "Error");
                    setStatus("Failed to tailor resume");
                }
            }
        }.execute();

        // If onBuild handler is set, also call it
        if (onBuild != null) {
            onBuild.accept(selectedFile, jobDesc);
        }
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

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            int w = getWidth(), h = getHeight();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(BG_WHITE);
            g.fillRoundRect(0, 0, w, h, 12, 12);

            float[] dash = {8f, 8f};
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
            g.setColor(hover ? ACCENT : BORDER_NEUTRAL);
            g.drawRoundRect(1, 1, w - 2, h - 2, 12, 12);

            g.setColor(TEXT_MUTED);
            String t1 = "Drag & drop your resume";
            String t2 = "or click to browse";
            Font f1 = getFont().deriveFont(Font.BOLD, 16f);
            Font f2 = getFont().deriveFont(Font.PLAIN, 13f);

            g.setFont(f1);
            int y = h / 2 - 6;
            drawCentered(g, t1, w, y);

            g.setFont(f2);
            drawCentered(g, t2, w, y + 22);

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