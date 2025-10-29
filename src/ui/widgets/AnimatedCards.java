package ui.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnimatedCards extends JPanel {
    private final CardLayout layout = new CardLayout();
    private final Map<String, JComponent> cards = new LinkedHashMap<>();
    private String current;

    private boolean animating = false;
    private BufferedImage fromImg, toImg;
    private float progress;   // 0..1
    private int dir;          // +1 = up, -1 = down
    private Timer timer;

    public AnimatedCards() {
        setLayout(layout);
        setDoubleBuffered(true);
        setOpaque(false);
    }

    public void addCard(String name, JComponent comp) {
        cards.put(name, comp);
        super.add(comp, name);
        if (current == null) {
            current = name;
            layout.show(this, name);
        }
    }

    public String getCurrentCard() { return current; }

    public void instantShow(String name) {
        current = name;
        layout.show(this, name);
        repaint();
    }

    public void slideTo(String name, int direction) {
        if (animating || name.equals(current)) {
            instantShow(name);
            return;
        }
        JComponent from = cards.get(current);
        JComponent to   = cards.get(name);
        if (from == null || to == null) { instantShow(name); return; }

        Dimension size = getSize();
        if (size.width <= 0 || size.height <= 0) {  // not laid out yet
            instantShow(name);
            return;
        }

        // layout before snapshot
        from.setSize(size); from.doLayout();
        to.setSize(size);   to.doLayout();

        fromImg = snapshot(from, size);
        toImg   = snapshot(to, size);

        dir = (direction >= 0) ? +1 : -1;  // +1 up, -1 down
        progress = 0f;
        animating = true;

        if (timer != null && timer.isRunning()) timer.stop();
        timer = new Timer(16, e -> {
            progress = Math.min(1f, progress + 0.06f);  // ease-ish
            repaint();
            if (progress >= 1f) {
                ((Timer) e.getSource()).stop();
                animating = false;
                current = name;
                layout.show(this, name);
                fromImg = toImg = null;
                repaint();
            }
        });
        timer.start();
    }

    @Override protected void paintChildren(Graphics g) {
        if (!animating) super.paintChildren(g);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!animating || fromImg == null || toImg == null) return;

        int w = getWidth(), h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int yFrom = (dir == +1) ? (int)(-progress * h) : (int)( progress * h);
        int yTo   = (dir == +1) ? (int)((1 - progress) * h) : (int)((progress - 1) * h);

        g2.drawImage(fromImg, 0, yFrom, w, h, null);
        g2.drawImage(toImg,   0, yTo,   w, h, null);
        g2.dispose();
    }

    private static BufferedImage snapshot(JComponent c, Dimension size) {
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        c.printAll(g2);
        g2.dispose();
        return img;
    }
}
