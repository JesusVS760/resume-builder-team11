package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

import services.ResumeParserService;

public class UploadPanel extends JPanel {

    private static final Set<String> ALLOWED = Set.of("pdf", "doc", "docx");

    // Resume parser + extracted text storage
    private final ResumeParserService parser = new ResumeParserService();
    private String parsedResumeText = "";

    // Job description card
    private final JTextArea jobArea = new JTextArea(12, 28);
    private final JLabel counter = new JLabel("0 chars, 0 words");

    // Build resume button
    private final JButton buildBtn = new JButton("Build Tailored Resume");
    private File selectedFile = null;
    private BiConsumer<File, String> onBuild = (f, jd) -> {};

    private Consumer<File> onFileDropped = f -> {};

    public UploadPanel() {
        setOpaque(true);
        setBackground(new Color(246, 248, 250));
        setLayout(new BorderLayout());

        // Drag & drop card
        DropZoneCard dropCard = new DropZoneCard();

        // Job description card
        JPanel jdCard = buildJobDescriptionCard();

        // Makes the two cards perfectly split in half
        JPanel halves = new JPanel(new GridLayout(1, 2, 16, 0)); // 16px gap
        halves.setOpaque(false);
        halves.add(wrapAsCard(dropCard));
        halves.add(wrapAsCard(jdCard));

        add(halves, BorderLayout.CENTER);

        // Build resume button
        styleBuildButton(buildBtn);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(0, 24, 24, 24));
        south.add(buildBtn);
        add(south, BorderLayout.SOUTH);

        // Build button action
        buildBtn.addActionListener(e -> onBuild.accept(selectedFile, getJobDescription()));
        buildBtn.setEnabled(false);

        // Handle file drop/selection → parse resume
        dropCard.setOnFileDropped(f -> {
            selectedFile = f;

            try {
                parsedResumeText = parser.parseResume(f);

                JTextArea textArea = new JTextArea(parsedResumeText, 20, 50);
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                JScrollPane scrollPane = new JScrollPane(textArea);

                JOptionPane.showMessageDialog(
                        this,
                        scrollPane,
                        "Parsed Resume Preview",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                parsedResumeText = "";
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to parse resume: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

            onFileDropped.accept(f);
            updateBuildButtonEnabled();
        });
    }

    // Public API

    public String getJobDescription() {
        return jobArea.getText().trim();
    }

    public String getParsedResumeText() {
        return parsedResumeText;
    }

    public void setOnFileDropped(Consumer<File> handler) {
        this.onFileDropped = (handler != null) ? handler : f -> {};
    }

    /** Hook to handle the Build button click. */
    public void setOnBuild(BiConsumer<File, String> handler) {
        this.onBuild = (handler != null) ? handler : (f, jd) -> {};
    }

    // UI Builder

    private JPanel buildJobDescriptionCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setOpaque(false);

        JLabel title = new JLabel("Enter Job Description");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        jobArea.setLineWrap(true);
        jobArea.setWrapStyleWord(true);
        jobArea.setFont(jobArea.getFont().deriveFont(13f));
        jobArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(jobArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(new LineBorder(new Color(200, 210, 220), 1, true));

        // Buttons row
        JButton paste = new JButton("Paste");
        JButton clear = new JButton("Clear");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(paste);
        actions.add(clear);

        // Counter + actions container
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        counter.setForeground(new Color(90, 95, 110));
        south.add(counter, BorderLayout.WEST);
        south.add(actions, BorderLayout.EAST);

        // Wire actions
        paste.addActionListener(e -> {
            try {
                var cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                var clip = cb.getData(DataFlavor.stringFlavor);
                if (clip instanceof String s) {
                    jobArea.replaceSelection(s);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Nothing to paste from clipboard.",
                        "Paste", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        clear.addActionListener(e -> jobArea.setText(""));

        // Live counter + enable logic for Build button
        jobArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateCounter(); updateBuildButtonEnabled(); }
            public void removeUpdate(DocumentEvent e) { updateCounter(); updateBuildButtonEnabled(); }
            public void changedUpdate(DocumentEvent e) { updateCounter(); updateBuildButtonEnabled(); }
        });
        updateCounter();

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private void styleBuildButton(JButton b) {
        Color navy = new Color(0x1f2937); // your app's navy
        b.setBackground(navy);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(12, 20, 12, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(navy.darker());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(navy);
            }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                b.setBackground(navy.darker().darker());
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                b.setBackground(navy.darker());
            }
        });
    }

    private void updateCounter() {
        String text = jobArea.getText();
        int chars = text.length();
        int words = (text.isBlank()) ? 0 : text.trim().split("\\s+").length;
        counter.setText(chars + " chars, " + words + " words");
    }

    private void updateBuildButtonEnabled() {
        boolean ok = (selectedFile != null) && !getJobDescription().isBlank();
        buildBtn.setEnabled(ok);
    }

    private JComponent wrapAsCard(JComponent inner) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new CompoundBorder(
                new EmptyBorder(24, 24, 24, 24),
                new LineBorder(new Color(200, 210, 220), 2, true)
        ));
        outer.add(inner, BorderLayout.CENTER);
        return outer;
    }

    // ─────────────────────────────────────────────────────────
    // Drag & Drop Zone
    // ─────────────────────────────────────────────────────────
    private class DropZoneCard extends JPanel {
        private final JLabel title = new JLabel("Drag & drop your resume here");
        private final JLabel subtitle = new JLabel("(PDF, DOC, DOCX)");
        private final JLabel hint = new JLabel("…or click to choose a file");

        private Consumer<File> onFileDropped = f -> {};
        private boolean dragActive = false;

        DropZoneCard() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
            title.setHorizontalAlignment(SwingConstants.CENTER);

            subtitle.setFont(subtitle.getFont().deriveFont(13f));
            subtitle.setForeground(new Color(90, 95, 110));
            subtitle.setHorizontalAlignment(SwingConstants.CENTER);

            hint.setFont(hint.getFont().deriveFont(12f));
            hint.setForeground(new Color(120, 125, 140));
            hint.setHorizontalAlignment(SwingConstants.CENTER);

            var g = new GridBagConstraints();
            g.gridx = 0; g.gridy = 0; g.insets = new Insets(4, 4, 8, 4);
            g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.CENTER;
            add(title, g);

            g.gridy = 1; g.insets = new Insets(0, 4, 12, 4);
            add(subtitle, g);

            g.gridy = 2;
            add(hint, g);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    chooseFileViaDialog();
                }
            });

            setTransferHandler(new TransferHandler() {
                @Override public boolean canImport(TransferSupport support) {
                    boolean fileList = support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                    support.setDropAction(COPY);
                    return fileList;
                }

                @Override public boolean importData(TransferSupport support) {
                    if (!canImport(support)) return false;
                    try {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) support.getTransferable()
                                .getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);

                        if (files == null || files.isEmpty()) return false;
                        File f = files.get(0);

                        if (!isAllowed(f)) {
                            JOptionPane.showMessageDialog(
                                    UploadPanel.this,
                                    "Please drop a PDF, DOC, or DOCX file.",
                                    "Unsupported file",
                                    JOptionPane.WARNING_MESSAGE
                            );
                            return false;
                        }

                        onFileDropped.accept(f);
                        showSuccess(f);
                        return true;

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(
                                UploadPanel.this,
                                "Failed to import file: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return false;

                    } finally {
                        dragActive = false;
                        repaint();
                    }
                }
            });

            // Highlight on drag
            new DropTarget(this, new DropTargetAdapter() {
                @Override public void dragEnter(DropTargetDragEvent dtde) { dragActive = true; repaint(); }
                @Override public void dragExit(DropTargetEvent dte) { dragActive = false; repaint(); }
                @Override public void drop(DropTargetDropEvent dtde) { dragActive = false; repaint(); }
            });
        }

        void setOnFileDropped(Consumer<File> handler) {
            this.onFileDropped = (handler != null) ? handler : f -> {};
        }

        private boolean isAllowed(File f) {
            String name = f.getName();
            int dot = name.lastIndexOf('.');
            if (dot < 0) return false;
            String ext = name.substring(dot + 1).toLowerCase();
            return ALLOWED.contains(ext);
        }

        private void chooseFileViaDialog() {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Choose resume");
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Resume files (PDF, DOC, DOCX)", "pdf", "doc", "docx"));

            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (!isAllowed(f)) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Please choose a PDF, DOC, or DOCX file.",
                            "Unsupported file",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                onFileDropped.accept(f);
                showSuccess(f);
            }
        }

        private void showSuccess(File f) {
            title.setText("File selected:");
            subtitle.setText(f.getName());
            hint.setText("Drop a different file to replace it.");
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!dragActive) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(100, 140, 255, 60));
            var r = getInsets();
            g2.fillRoundRect(
                    r.left, r.top,
                    getWidth() - r.left - r.right,
                    getHeight() - r.top - r.bottom,
                    16, 16
            );
            g2.dispose();
        }
    }
}
