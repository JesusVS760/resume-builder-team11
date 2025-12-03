package ui.widgets;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AnimatedCards (DPI-proof, no snapshots):
 * - addCard(name, comp)
 * - instantShow(name)
 * - slideTo(name, dir)  // dir: +1 forward (card comes from bottom), -1 back (from top)
 *
 * Implementation: we keep all cards as children and animate by changing their bounds.
 * No CardLayout, no BufferedImage, so it’s rock solid on any Windows display scale.
 */
public class AnimatedCards extends JPanel {

    private final Map<String, JComponent> cards = new LinkedHashMap<>();
    private String current;

    // Animation state
    private boolean animating = false;
    private JComponent fromComp, toComp;
    private Timer timer;
    private float progress;   // 0..1
    private int dir;          // +1 / -1

    // Queue a pending request if user clicks during animation
    private String queuedName = null;
    private int queuedDir = +1;

    // Tunables
    private static final int TICK_MS = 8;    // ~120 FPS timer tick
    private static final float STEP   = 0.05f; // progress per tick (lower = slower/smoother)

    public AnimatedCards() {
        setLayout(null);      // we position children ourselves
        setOpaque(true);

        // Keep the active card filling the container when resized
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if (!animating && current != null) {
                    JComponent c = cards.get(current);
                    if (c != null) c.setBounds(0, 0, getWidth(), getHeight());
                }
            }
        });
    }

    public void addCard(String name, JComponent comp) {
        cards.put(name, comp);
        comp.setVisible(false);
        add(comp);
        if (current == null) {
            current = name;
            comp.setBounds(0, 0, getWidth(), getHeight());
            comp.setVisible(true);
        }
    }

    public String getCurrentCard() { return current; }

    public void instantShow(String name) {
        if (!cards.containsKey(name)) return;
        if (current != null) {
            JComponent old = cards.get(current);
            if (old != null) old.setVisible(false);
        }
        current = name;
        JComponent c = cards.get(name);
        c.setBounds(0, 0, getWidth(), getHeight());
        c.setVisible(true);
        revalidate();
        repaint();
    }

    public void slideTo(String name, int direction) {
        if (!cards.containsKey(name) || name.equals(current)) return;

        if (animating) {
            queuedName = name;
            queuedDir  = (direction >= 0) ? +1 : -1;
            return;
        }

        if (!isShowing() || getWidth() <= 0 || getHeight() <= 0) {
            waitForFirstLayoutThenSlide(name, direction);
            return;
        }

        fromComp = cards.get(current);
        toComp   = cards.get(name);

        // Prepare positions
        int w = getWidth(), h = getHeight();
        dir = (direction >= 0) ? +1 : -1;
        progress = 0f;
        animating = true;

        fromComp.setBounds(0, 0, w, h);
        toComp.setBounds(0, (dir == +1 ? h : -h), w, h);
        fromComp.setVisible(true);
        toComp.setVisible(true);

        // To prevent clicks during the slide
        fromComp.setEnabled(false);
        toComp.setEnabled(false);

        if (timer != null && timer.isRunning()) timer.stop();
        timer = new Timer(TICK_MS, e -> {
            progress = Math.min(1f, progress + STEP);

            int yFrom = (dir == +1) ? (int)(-progress * h) : (int)( progress * h);
            int yTo   = (dir == +1) ? (int)((1f - progress) * h) : (int)((progress - 1f) * h);

            fromComp.setLocation(0, yFrom);
            toComp.setLocation(0, yTo);

            repaint();

            if (progress >= 1f) {
                ((Timer) e.getSource()).stop();
                animating = false;

                // Finish state
                int W = getWidth(), H = getHeight();
                fromComp.setVisible(false);
                fromComp.setEnabled(true);
                toComp.setBounds(0, 0, W, H);
                toComp.setEnabled(true);

                current = name;
                fromComp = toComp = null;
                revalidate();
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

    private void waitForFirstLayoutThenSlide(String name, int direction) {
        SwingUtilities.invokeLater(() -> {
            if (isShowing() && getWidth() > 0 && getHeight() > 0) {
                slideTo(name, direction);
            } else {
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
}
