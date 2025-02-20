package com.dpforge.doom;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class DoomDisplay extends JPanel {

    private final int scale = 2;

    private final BufferedImage screenBuffer;
    private final int width;
    private final int height;

    final DoomKeyboard keyboard = new DoomKeyboard();

    DoomDisplay(int width, int height, BufferedImage screenBuffer) {
        this.width = width;
        this.height = height;
        this.screenBuffer = screenBuffer;
    }

    void start() {
        SwingUtilities.invokeLater(() -> {
            setPreferredSize(new Dimension(scale * width, scale * height));

            JFrame frame = new JFrame("Java DOOM");
            frame.setSize(scale * width, scale * height);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setVisible(true);
            frame.setContentPane(this);
            frame.pack();

            frame.addKeyListener(keyboard);
        });
    }

    void onFinishUpdate() {
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    protected void paintComponent(Graphics g) {
        synchronized (screenBuffer) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    g.setColor(new Color(screenBuffer.getRGB(x, y)));
                    g.fillRect(x * scale, y * scale, scale, scale);
                }
            }
        }
    }
}
