package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

/**
 * Landing / home panel shown when the app first opens.
 *
 * Layout (top to bottom):
 *   1. Title + subtitle (centered)
 *   2. Hero image (centered)
 *   3. "How it works" text (left-aligned)
 *   4. Start Here button (centered at very bottom)
 */
public class LandingPanel extends JPanel {

    private final String imagePath;
    private Runnable onStartHere;

    private JButton startButton;

    public LandingPanel(String imagePath) {
        this.imagePath = imagePath;
        buildUI();
    }

    public void setOnStartHere(Runnable onStartHere) {
        this.onStartHere = onStartHere;
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Color.WHITE);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(24, 24, 8, 24));

        JLabel title = new JLabel("Tailored Resume Builder", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Turn one resume into many job-specific versions.", SwingConstants.CENTER);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 17f));
        subtitle.setForeground(new Color(0x555555));
        subtitle.setBorder(new EmptyBorder(8, 0, 0, 0));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        top.add(title);
        top.add(subtitle);

        add(top, BorderLayout.NORTH);

        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.setOpaque(false);
        imagePanel.setBorder(new EmptyBorder(8, 32, 8, 32));

        JLabel hero = createHeroImageLabel(imagePath, 520, 320);
        imagePanel.add(hero);

        add(imagePanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 32, 24, 32));

        JPanel howPanel = new JPanel();
        howPanel.setOpaque(false);
        howPanel.setLayout(new BoxLayout(howPanel, BoxLayout.Y_AXIS));

        JLabel howItWorks = new JLabel("<html><b>How it works</b></html>");
        howItWorks.setFont(howItWorks.getFont().deriveFont(Font.PLAIN, 16f));
        howItWorks.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html><body style='width:620px'>" +
                        "1. <b>Upload</b> your existing resume (PDF, DOC, or DOCX).<br/>" +
                        "2. <b>Paste the job description</b> for the role you want to apply to.<br/>" +
                        "3. The app <b>parses your resume</b> and <b>generates a tailored version</b> " +
                        "that highlights the most relevant skills and keywords for that job.<br/><br/>" +
                        "You can then review, save, and export tailored resumes as PDF or DOCX " +
                        "for different positions and companies." +
                        "</body></html>"
        );
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setBorder(new EmptyBorder(8, 0, 0, 0));

        howPanel.add(howItWorks);
        howPanel.add(description);

        bottom.add(howPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

        startButton = new JButton("Start Here");
        stylePrimaryButton(startButton);

        startButton.addActionListener(e -> {
            if (onStartHere != null) {
                onStartHere.run();
            }
        });

        buttonPanel.add(startButton);
        bottom.add(buttonPanel, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);
    }

    private void stylePrimaryButton(JButton b) {
        b.setUI(new BasicButtonUI());
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);

        Color normal  = new Color(0x374151); // base color
        Color hover   = new Color(0x4B5563); // lighter on hover
        Color pressed = new Color(0x1F2937); // darker on press

        b.setFont(b.getFont().deriveFont(16f));
        b.setBorder(BorderFactory.createEmptyBorder(14, 48, 14, 48));
        b.setBackground(normal);
        b.setForeground(Color.WHITE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.setMinimumSize(new Dimension(200, 48));

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


    private JLabel createHeroImageLabel(String imagePath, int maxW, int maxH) {
        JLabel label = new JLabel("Image not found", SwingConstants.CENTER);
        label.setOpaque(false);
        label.setPreferredSize(new Dimension(maxW, maxH));
        label.setMinimumSize(new Dimension(maxW, maxH));
        label.setMaximumSize(new Dimension(maxW, maxH));

        Image img = null;
        try {
            java.net.URL url = getClass().getResource(imagePath);
            if (url != null) {
                img = new ImageIcon(url).getImage();
            } else {
                java.io.File f = new java.io.File(imagePath);
                if (f.exists()) {
                    img = new ImageIcon(f.getAbsolutePath()).getImage();
                }
            }
        } catch (Exception ignored) { }

        if (img != null) {
            Image scaled = img.getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
            label.setText(null);
            label.setIcon(new ImageIcon(scaled));
        }
        return label;
    }
}
