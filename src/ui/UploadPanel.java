package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class UploadPanel extends JPanel {

    private static final Set<String> ALLOWED = Set.of("pdf", "doc", "docx");

    private final JLabel title = new JLabel("Drag & drop your resume here");
    private final JLabel subtitle = new JLabel("(PDF, DOC, DOCX)");
    private final JLabel hint = new JLabel("…or click to choose a file");

    private Consumer<File> onFileDropped = f -> {}; // default no-op
    private boolean dragActive = false;

    public UploadPanel() {
        setOpaque(true);
        setBackground(new Color(246, 248, 250));
        setLayout(new GridBagLayout());
        setBorder(new CompoundBorder(
                new EmptyBorder(24, 24, 24, 24),
                new LineBorder(new Color(200, 210, 220), 2, true)
        ));

        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        subtitle.setFont(subtitle.getFont().deriveFont(13f));
        subtitle.setForeground(new Color(90, 95, 110));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 12f));
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

        // Click-to-choose fallback
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                chooseFileViaDialog();
            }
        });

        // Drag & drop using TransferHandler (simplest in Swing)
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
                        JOptionPane.showMessageDialog(UploadPanel.this,
                                "Please drop a PDF, DOC, or DOCX file.",
                                "Unsupported file", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                    onFileDropped.accept(f);
                    showSuccess(f);
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(UploadPanel.this,
                            "Failed to import file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                } finally {
                    dragActive = false;
                    repaint();
                }
            }
        });

        // Optional: subtle highlight when dragging over
        new DropTarget(this, new DropTargetAdapter() {
            @Override public void dragEnter(DropTargetDragEvent dtde) { dragActive = true; repaint(); }
            @Override public void dragExit(DropTargetEvent dte) { dragActive = false; repaint(); }
            @Override public void drop(DropTargetDropEvent dtde) { dragActive = false; repaint(); }
        });
    }

    public void setOnFileDropped(Consumer<File> handler) {
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
                JOptionPane.showMessageDialog(this,
                        "Please choose a PDF, DOC, or DOCX file.",
                        "Unsupported file", JOptionPane.WARNING_MESSAGE);
                return;
            }
            onFileDropped.accept(f);
            showSuccess(f);
        }
    }

    private void showSuccess(File f) {
        title.setText("File selected:");
        subtitle.setText(f.getName());
        hint.setText("You can drop a different file to replace it.");
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!dragActive) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(100, 140, 255, 60));
        var r = getInsets();
        g2.fillRoundRect(r.left, r.top,
                getWidth() - r.left - r.right,
                getHeight() - r.top - r.bottom, 16, 16);
        g2.dispose();
    }
}
