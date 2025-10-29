package ui.widgets;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class HoverScaleButton extends JButton {
    private float scale = 0.94f;
    private float target = 0.94f;
    private final Timer anim;

    public HoverScaleButton(String text) {
        super(text);
        setFocusPainted(false);
        setRolloverEnabled(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorderPainted(false);
        setMargin(new Insets(10, 14, 10, 14));

        anim = new Timer(16, e -> {
            scale += (target - scale) * 0.2f;
            if (Math.abs(target - scale) < 0.005f) {
                scale = target;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                target = 1.00f; if (!anim.isRunning()) anim.start();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                target = 0.94f; if (!anim.isRunning()) anim.start();
            }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                target = 0.92f; if (!anim.isRunning()) anim.start(); // tactile press
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                target = 1.00f; if (!anim.isRunning()) anim.start();
            }
        });
    }

    @Override public void updateUI() {
        super.updateUI();
        setUI(new BasicButtonUI());
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth(), h = getHeight();
        int arc = 12;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float insetX = (1f - scale) * w / 2f;
        float insetY = (1f - scale) * h / 2f;
        int x = Math.round(insetX);
        int y = Math.round(insetY);
        int ww = Math.round(w - insetX * 2);
        int hh = Math.round(h - insetY * 2);

        g2.setColor(getBackground());
        g2.fillRoundRect(x, y, ww, hh, arc, arc);

        super.paintComponent(g2);
        g2.dispose();
    }
}
