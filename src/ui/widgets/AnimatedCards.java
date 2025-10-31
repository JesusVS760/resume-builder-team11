package ui.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Card stack with a vertical slide animation.
 * - Call addCard(name, comp) to register.
 * - Call slideTo(name, dir) where dir = +1 (up) or -1 (down).
 * - If a slide is in progress, further slide requests are queued and play next.
 */
public class AnimatedCards extends JPanel {

    private final CardLayout layout = new CardLayout();
    private final Map<String, JComponent> cards = new LinkedHashMap<>();

    private String current;

    // Animation state
    private boolean animating = false;
    private BufferedImage fromImg, toImg;
    private float progress;   // 0..1
    private int dir;          // +1 = up, -1 = down
    private Timer timer;

    // Queue a pending request if user clicks during animation
    private String queuedName = null;
    private int queuedDir = +1;

    public AnimatedCards() {
        setLayout(layout);
        setDoubleBuffered(true);
        setOpaque(true); // helps avoid transparency flicker
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

    /** Show immediately (no animation). */
    public void instantShow(String name) {
        if (!cards.containsKey(name)) return;
        current = name;
        layout.show(this, name);
        repaint();
    }

    /**
     * Slide to target card.
     * @param name target card key
     * @param direction +1 to slide up (new page comes from bottom), -1 to slide down (new page comes from top)
     */
    public void slideTo(String name, int direction) {
        if (!cards.containsKey(name)) {
            // Unknown card -> no-op
            return;
        }
        if (name.equals(current)) {
            // Already there -> nothing to animate
            return;
        }

        // If we're mid-animation, queue this request and play it next
        if (animating) {
            queuedName = name;
            queuedDir  = (direction >= 0) ? +1 : -1;
            return;
        }

        // Ensure we're laid out before animating (size must be valid)
        if (!isShowing() || getWidth() <= 0 || getHeight() <= 0) {
            // Defer until we have a real size
            waitForFirstLayoutThenSlide(name, direction);
            return;
        }

        final JComponent from = cards.get(current);
        final JComponent to   = cards.get(name);
        if (from == null || to == null) {
            instantShow(name);
            return;
        }

        // Make sure both cards are sized & laid out before snapshotting
        Dimension size = getSize();
        layoutCardForSnapshot(from, size);
        layoutCardForSnapshot(to, size);

        fromImg = snapshot(from, size);
        toImg   = snapshot(to, size);

        dir = (direction >= 0) ? +1 : -1;
        progress = 0f;
        animating = true;

        if (timer != null && timer.isRunning()) timer.stop();
        timer = new Timer(5, e -> {
            // Simple easing: approach 1.0
            progress = Math.min(1f, progress + 0.06f);
            repaint();

            if (progress >= 1f) {
                ((Timer) e.getSource()).stop();
                animating = false;
                current = name;
                layout.show(this, name);
                fromImg = toImg = null;
                repaint();

                // Play any queued request next
                if (queuedName != null && !queuedName.equals(current)) {
                    String nextName = queuedName;
                    int nextDir = queuedDir;
                    queuedName = null;
                    SwingUtilities.invokeLater(() -> slideTo(nextName, nextDir));
                } else {
                    queuedName = null;
                }
            }
        });
        timer.start();
    }

    // --- Painting ---

    @Override protected void paintChildren(Graphics g) {
        // While animating, we draw snapshots in paintComponent and
        // avoid painting live children to prevent flicker/doubling.
        if (!animating) super.paintChildren(g);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!animating || fromImg == null || toImg == null) return;

        int w = getWidth(), h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // dir=+1: from slides up, to comes from bottom
        // dir=-1: from slides down, to comes from top
        int yFrom = (dir == +1) ? (int)(-progress * h) : (int)( progress * h);
        int yTo   = (dir == +1) ? (int)((1 - progress) * h) : (int)((progress - 1) * h);

        // Stretch to current panel size to tolerate minor size changes
        g2.drawImage(fromImg, 0, yFrom, w, h, null);
        g2.drawImage(toImg,   0, yTo,   w, h, null);
        g2.dispose();
    }

    // --- Internals ---

    /** Wait until the component has a non-zero size, then try sliding again. */
    private void waitForFirstLayoutThenSlide(String name, int direction) {
        // If we are not showing yet, defer one layout cycle
        SwingUtilities.invokeLater(() -> {
            if (isShowing() && getWidth() > 0 && getHeight() > 0) {
                slideTo(name, direction);
            } else {
                // As a fallback, attach a one-shot listener for the first real resize
                ComponentAdapter onResize = new ComponentAdapter() {
                    @Override public void componentResized(ComponentEvent e) {
                        if (getWidth() > 0 && getHeight() > 0) {
                            removeComponentListener(this);
                            slideTo(name, direction);
                        }
                    }
                };
                addComponentListener(onResize);
            }
        });
    }

    /** Ensure a card has the target size & a valid layout before snapshotting. */
    private void layoutCardForSnapshot(JComponent c, Dimension size) {
        c.setSize(size);
        c.doLayout();
        // Validate to force LAF/UI delegates to realize bounds if needed
        c.validate();
    }

    /** Render a component to an offscreen image. */
    private static BufferedImage snapshot(JComponent c, Dimension size) {
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Disable double buffering while painting to an offscreen image
        RepaintManager mgr = RepaintManager.currentManager(c);
        boolean wasDB = mgr.isDoubleBufferingEnabled();
        try {
            mgr.setDoubleBufferingEnabled(false);
            c.printAll(g2);
        } finally {
            mgr.setDoubleBufferingEnabled(wasDB);
            g2.dispose();
        }
        return img;
    }
}
