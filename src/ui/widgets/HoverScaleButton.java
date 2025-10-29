package ui.widgets;

import javax.swing.*;
import java.awt.*;

public class HoverScaleButton extends JButton {
    private float scale = 1f;
    private float target = 1f;
    private final Timer anim;

    public HoverScaleButton(String text) {
        super(text);
        setFocusPainted(false);
        setOpaque(true);                 // so background colors show on macOS, etc.
        setRolloverEnabled(true);

        anim = new Timer(16, e -> {
            // simple tween toward target
            scale += (target - scale) * 0.2f;
            if (Math.abs(target - scale) < 0.01f) {
                scale = target;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                target = 1.06f;          // ~6% bigger on hover
                if (!anim.isRunning()) anim.start();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                target = 1f;             // back to normal
                if (!anim.isRunning()) anim.start();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        // paint scaled around the center, without affecting layout
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        g2.translate(w / 2.0, h / 2.0);
        g2.scale(scale, scale);
        g2.translate(-w / 2.0, -h / 2.0);

        super.paintComponent(g2);
        g2.dispose();
    }
}

