package homepage;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class LandingPanel extends JPanel {

    // pass a file path like "src/homepage/images/landing_hero.png"
    public LandingPanel(String imagePath) {
        setLayout(new BorderLayout());
        setBackground(Color.GRAY);

        // LEFT: headline + subtext (centered)
        JPanel left = new JPanel(new GridBagLayout());   // centers its child
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        left.setPreferredSize(new Dimension(520, 400));  // keep some width

        // column that holds the labels
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel h1 = new JLabel("Welcome to Resume Builder!");
        h1.setFont(h1.getFont().deriveFont(Font.BOLD, 28f));
        h1.setForeground(Color.WHITE);
        h1.setAlignmentX(Component.CENTER_ALIGNMENT);  // center text within column

        JLabel h2 = new JLabel("Improve your resume within minutes.");
        h2.setFont(h2.getFont().deriveFont(Font.PLAIN, 18f));
        h2.setForeground(new Color(230, 230, 230));
        h2.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        h2.setAlignmentX(Component.CENTER_ALIGNMENT);

        col.add(h1);
        col.add(Box.createVerticalStrut(8));
        col.add(h2);

        // add the column to the center of the left panel
        left.add(col, new GridBagConstraints());

        add(left, BorderLayout.WEST);

        // RIGHT: centered image with a max size
        JLabel imageLabel = makeImage(imagePath, 520, 360); // max size
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        right.add(imageLabel, new GridBagConstraints());

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
    }

    private JLabel makeImage(String path, int maxW, int maxH) {
        Image img = null;
        File f = new File(path);
        if (f.exists()) {
            img = new ImageIcon(f.getAbsolutePath()).getImage();
        } else {
            // try a classpath resource as a fallback
            java.net.URL url = getClass().getResource(path.startsWith("/") ? path : "/" + path);
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
