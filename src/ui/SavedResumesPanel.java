package ui;

import models.Resume;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class SavedResumesPanel extends JPanel {

    private static final Color BG_DARK  = new Color(0x1f2937);
    private static final Color BG_CARD  = new Color(0x374151);
    private static final Color FG_TEXT  = Color.BLACK;

    private final JButton sortByDateBtn;
    private final JButton sortByNameBtn;
    private final JButton uploadBtn;
    private final JPanel listPanel;
    private final JLabel emptyLabel;

    // Callbacks set by controller
    private Runnable onUpload;
    private Runnable onSortByDate;
    private Runnable onSortByName;
    private Consumer<Resume> onEdit;
    private Consumer<Resume> onDelete;

    public SavedResumesPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        // TOP BAR
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
        topBar.setBackground(BG_DARK);
        topBar.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel title = new JLabel("Saved Resumes");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        sortByDateBtn = createTopButton("Sort by Date");
        sortByNameBtn = createTopButton("Sort by Name");
        uploadBtn     = createAccentButton("Upload Resume");

        sortByDateBtn.addActionListener(e -> {
            if (onSortByDate != null) onSortByDate.run();
        });
        sortByNameBtn.addActionListener(e -> {
            if (onSortByName != null) onSortByName.run();
        });
        uploadBtn.addActionListener(e -> {
            if (onUpload != null) onUpload.run();
        });

        topBar.add(title);
        topBar.add(Box.createHorizontalStrut(16));
        topBar.add(sortByDateBtn);
        topBar.add(Box.createHorizontalStrut(8));
        topBar.add(sortByNameBtn);
        topBar.add(Box.createHorizontalGlue());
        topBar.add(uploadBtn);

        add(topBar, BorderLayout.NORTH);

        // ----- LIST AREA -----
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        emptyLabel = new JLabel("There are no saved resumes.");
        emptyLabel.setForeground(Color.LIGHT_GRAY);
        emptyLabel.setFont(emptyLabel.getFont().deriveFont(14f));
        emptyLabel.setBorder(new EmptyBorder(16, 4, 4, 4));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBackground(BG_DARK);
        add(scroll, BorderLayout.CENTER);
    }

    private JButton createTopButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(FG_TEXT);
        b.setBackground(BG_CARD);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return b;
    }

    private JButton createAccentButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(FG_TEXT);
        b.setBackground(BG_CARD);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                new EmptyBorder(6, 14, 6, 14)
        ));
        return b;
    }

    // Public API used by controller

    public void showResumes(List<Resume> resumes) {
        listPanel.removeAll();

        if (resumes == null || resumes.isEmpty()) {
            listPanel.add(emptyLabel);
        } else {
            for (Resume r : resumes) {
                listPanel.add(createResumeCard(r));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    public void setOnUpload(Runnable onUpload) {
        this.onUpload = onUpload;
    }

    public void setOnSortByDate(Runnable onSortByDate) {
        this.onSortByDate = onSortByDate;
    }

    public void setOnSortByName(Runnable onSortByName) {
        this.onSortByName = onSortByName;
    }

    public void setOnEdit(Consumer<Resume> onEdit) {
        this.onEdit = onEdit;
    }

    public void setOnDelete(Consumer<Resume> onDelete) {
        this.onDelete = onDelete;
    }

    // Card UI (thumbnail + info + 3-dot menu)

    private JComponent createResumeCard(Resume resume) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(BG_CARD);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Left: preview / thumbnail block
        JPanel preview = new JPanel(new BorderLayout());
        preview.setPreferredSize(new Dimension(80, 90));
        preview.setBackground(BG_DARK);

        JLabel icon = new JLabel("Preview", SwingConstants.CENTER);
        icon.setForeground(FG_TEXT);
        icon.setFont(icon.getFont().deriveFont(11f));
        preview.add(icon, BorderLayout.CENTER);

        card.add(preview, BorderLayout.WEST);

        // Center: resume info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(resume.getFileName());
        nameLabel.setForeground(FG_TEXT);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));

        String uploaded = resume.getUploadedAt() != null ? resume.getUploadedAt() : "Unknown";
        JLabel metaLabel = new JLabel("Uploaded: " + uploaded);
        metaLabel.setForeground(Color.LIGHT_GRAY);
        metaLabel.setFont(metaLabel.getFont().deriveFont(11f));

        String path = resume.getFilePath() != null ? resume.getFilePath() : "";
        JLabel pathLabel = new JLabel(path);
        pathLabel.setForeground(Color.LIGHT_GRAY);
        pathLabel.setFont(pathLabel.getFont().deriveFont(10f));

        info.add(nameLabel);
        info.add(Box.createVerticalStrut(4));
        info.add(metaLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(pathLabel);

        card.add(info, BorderLayout.CENTER);

        // Right: 3-dot menu
        JButton menuBtn = new JButton("⋮");
        menuBtn.setFocusPainted(false);
        menuBtn.setContentAreaFilled(false);
        menuBtn.setBorderPainted(false);
        menuBtn.setForeground(FG_TEXT);
        menuBtn.setPreferredSize(new Dimension(32, 24));

        JPopupMenu menu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit");
        JMenuItem deleteItem = new JMenuItem("Delete");
        menu.add(editItem);
        menu.add(deleteItem);

        editItem.addActionListener(e -> {
            if (onEdit != null) onEdit.accept(resume);
        });
        deleteItem.addActionListener(e -> {
            if (onDelete != null) onDelete.accept(resume);
        });

        menuBtn.addActionListener(e ->
                menu.show(menuBtn, 0, menuBtn.getHeight())
        );

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(menuBtn, BorderLayout.NORTH);
        card.add(right, BorderLayout.EAST);

        return card;
    }
}
