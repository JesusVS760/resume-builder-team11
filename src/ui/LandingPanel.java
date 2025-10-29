package ui;

import javax.swing.*;
import java.awt.*;

public class LandingPanel extends JPanel {

    public LandingPanel(String imagePath) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Color.WHITE);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel h1 = new JLabel("Welcome to Resume Builder!");
        h1.setFont(h1.getFont().deriveFont(Font.BOLD, 28f));
        h1.setForeground(Color.DARK_GRAY);
        h1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel h2 = new JLabel("Improve your resume within minutes.");
        h2.setFont(h2.getFont().deriveFont(Font.PLAIN, 18f));
        h2.setForeground(Color.DARK_GRAY);
        h2.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        h2.setAlignmentX(Component.CENTER_ALIGNMENT);

        col.add(h1);
        col.add(Box.createVerticalStrut(8));
        col.add(h2);
        top.add(col, new GridBagConstraints());

        JLabel imageLabel = makeImage(imagePath, 520, 360);
        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        bottom.add(imageLabel, new GridBagConstraints());

        center.add(top);
        center.add(Box.createVerticalGlue());
        center.add(bottom);

        add(center, BorderLayout.CENTER);
    }

    private JLabel makeImage(String path, int maxW, int maxH) {
        Image img = null;
        java.io.File f = new java.io.File(path);
        if (f.exists()) {
            img = new ImageIcon(f.getAbsolutePath()).getImage();
        } else {
            var url = getClass().getResource(path.startsWith("/") ? path : "/" + path);
            if (url != null) img = new ImageIcon(url).getImage();
        }

        JLabel label = new JLabel("Image not found", SwingConstants.CENTER);
        label.setOpaque(false);
        label.setPreferredSize(new Dimension(maxW, maxH));
        label.setMinimumSize(new Dimension(maxW, maxH));
        label.setMaximumSize(new Dimension(maxW, maxH));

        if (img != null) {
            Image scaled = img.getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
            label.setText(null);
            label.setIcon(new ImageIcon(scaled));
        }
        return label;
    }
}
