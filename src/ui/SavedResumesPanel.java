package ui;

import models.Resume;
import ui.widgets.HoverScaleButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI; // <- add this import
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class SavedResumesPanel extends JPanel {

    private final JButton sortByDateBtn;
    private final JButton sortByNameBtn;
    private final JButton uploadBtn;
    private final JPanel listPanel;
    private final JLabel emptyLabel;

    // Callbacks provided by controller
    private Runnable onUpload;
    private Runnable onSortByDate;
    private Runnable onSortByName;
    private Consumer<Resume> onEdit;
    private Consumer<Resume> onDelete;

    public SavedResumesPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // TOP BAR (title + sort buttons on the right)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("Saved Resumes");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(0x111827));

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sortPanel.setOpaque(false);

        sortByDateBtn = createChipButton("Sort by date");
        sortByNameBtn = createChipButton("Sort by name");

        sortByDateBtn.addActionListener(e -> {
            if (onSortByDate != null) onSortByDate.run();
        });
        sortByNameBtn.addActionListener(e -> {
            if (onSortByName != null) onSortByName.run();
        });

        sortPanel.add(sortByDateBtn);
        sortPanel.add(sortByNameBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(sortPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // CENTER: outer container box + scrollable list
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(true);
        listPanel.setBackground(Color.LIGHT_GRAY);
        listPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        emptyLabel = new JLabel("There are no saved resumes.");
        emptyLabel.setForeground(Color.BLACK);
        emptyLabel.setFont(emptyLabel.getFont().deriveFont(14f));
        emptyLabel.setBorder(new EmptyBorder(24, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(0xF3F4F6));
        scrollPane.setBackground(new Color(0xF3F4F6));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Box that holds all resume rectangles
        JPanel boxContainer = new JPanel(new BorderLayout());
        boxContainer.setBackground(Color.WHITE);
        boxContainer.setBorder(new EmptyBorder(8, 16, 8, 16));

        JPanel innerBox = new JPanel(new BorderLayout());
        innerBox.setBackground(new Color(0xF9FAFB));
        innerBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        innerBox.add(scrollPane, BorderLayout.CENTER);

        boxContainer.add(innerBox, BorderLayout.CENTER);
        add(boxContainer, BorderLayout.CENTER);

        // BOTTOM BAR (upload button)
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        bottomBar.setBackground(Color.WHITE);

        uploadBtn = new JButton("Upload resume");
        stylePrimaryButton(uploadBtn);
        uploadBtn.addActionListener(e -> {
            if (onUpload != null) onUpload.run();
        });

        bottomBar.add(uploadBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JButton createChipButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setBackground(new Color(0xE5E7EB)); // light gray
        b.setForeground(new Color(0x111827)); // dark gray
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setFont(b.getFont().deriveFont(12f));
        return b;
    }

    private void stylePrimaryButton(JButton b) {
        // Make LAF stay out of the way so our colors show
        b.setUI(new BasicButtonUI());
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);

        Color normal  = new Color(0x374151); // base color
        Color hover   = new Color(0x4B5563); // lighter on hover
        Color pressed = new Color(0x1F2937); // darker on press

        b.setBackground(normal);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        b.setFont(b.getFont().deriveFont(13f));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Simple hover + press color behavior
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


    // Public API used by controller
    public void showResumes(List<Resume> resumes) {
        listPanel.removeAll();

        if (resumes == null || resumes.isEmpty()) {
            listPanel.add(emptyLabel);
        } else {
            for (Resume r : resumes) {
                JComponent card = createResumeCard(r);
                listPanel.add(card);
                listPanel.add(Box.createVerticalStrut(12));
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


    private JComponent createResumeCard(Resume resume) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(new Color(0x374151)); // dark slate
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x111827)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Full-width, uniform height
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Left: preview placeholder block
        JPanel preview = new JPanel(new BorderLayout());
        preview.setPreferredSize(new Dimension(90, 96));
        preview.setBackground(new Color(0x1F2937)); // slightly darker
        preview.setBorder(BorderFactory.createLineBorder(new Color(0x4B5563)));

        JLabel previewLabel = new JLabel("Preview", SwingConstants.CENTER);
        previewLabel.setForeground(new Color(0x9CA3AF));
        previewLabel.setFont(previewLabel.getFont().deriveFont(11f));
        preview.add(previewLabel, BorderLayout.CENTER);

        card.add(preview, BorderLayout.WEST);

        // Center: resume info (on dark background -> light text)
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        String name = resume.getFileName() != null ? resume.getFileName() : "(unnamed resume)";
        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));

        String uploaded = resume.getUploadedAt() != null ? resume.getUploadedAt() : "Unknown date";
        JLabel metaLabel = new JLabel("Uploaded: " + uploaded);
        metaLabel.setForeground(new Color(0xD1D5DB));
        metaLabel.setFont(metaLabel.getFont().deriveFont(11f));

        String path = resume.getFilePath() != null ? resume.getFilePath() : "";
        JLabel pathLabel = new JLabel(path);
        pathLabel.setForeground(new Color(0x9CA3AF));
        pathLabel.setFont(pathLabel.getFont().deriveFont(10f));

        info.add(nameLabel);
        info.add(Box.createVerticalStrut(4));
        info.add(metaLabel);
        if (!path.isEmpty()) {
            info.add(Box.createVerticalStrut(2));
            info.add(pathLabel);
        }

        card.add(info, BorderLayout.CENTER);

        HoverScaleButton menuBtn = new HoverScaleButton("⋮");
        menuBtn.setFocusPainted(false);
        menuBtn.setBackground(new Color(0x4B5563));
        menuBtn.setForeground(Color.WHITE);
        Font base = menuBtn.getFont();
        menuBtn.setFont(base.deriveFont(Font.BOLD, 24f));
        menuBtn.setBorderPainted(false);
        menuBtn.setMargin(new Insets(4, 8, 4, 8));
        menuBtn.setPreferredSize(new Dimension(40, 36));
        menuBtn.setHorizontalAlignment(SwingConstants.CENTER);
        menuBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


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
                menu.show(menuBtn, menuBtn.getWidth() / 2, menuBtn.getHeight())
        );

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(menuBtn, BorderLayout.NORTH);

        card.add(right, BorderLayout.EAST);

        return card;
    }
}
