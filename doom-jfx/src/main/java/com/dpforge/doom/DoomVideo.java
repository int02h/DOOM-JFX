package com.dpforge.doom;

import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class DoomVideo {

    private static final int COLOR_SIZE = 3;

    private static int screenWidth;
    private static int screenHeight;
    private static ByteBuffer mainScreen;
    private static byte[] palette;
    private static BufferedImage renderedScreen;

    private static DoomDisplay display;

    public native static int getScreenWidth();

    public native static int getScreenHeight();

    public native static ByteBuffer getScreenBuffer(int index, int size);

    public static void initGraphics() {
        System.out.println("initGraphics");
        screenWidth = getScreenWidth();
        screenHeight = getScreenHeight();
        mainScreen = getScreenBuffer(0, screenWidth * screenHeight);
        renderedScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);

        display = new DoomDisplay(screenWidth, screenHeight, renderedScreen);
        display.start();

        System.out.println("initGraphics done");
    }

    public static void finishUpdate() {
        synchronized (renderedScreen) {
            for (int y = 0; y < screenHeight; y++) {
                for (int x = 0; x < screenWidth; x++) {
                    int colorIndex = mainScreen.get(y * screenWidth + x) & 0xFF;
                    int r = palette[colorIndex * COLOR_SIZE];
                    int g = palette[colorIndex * COLOR_SIZE + 1];
                    int b = palette[colorIndex * COLOR_SIZE + 2];
                    int color = (r << 16) + (g << 8) + b;
                    renderedScreen.setRGB(x, y, color);
                }
            }
        }
        display.onFinishUpdate();
    }

    public static void setPalette(byte[] palette) {
        DoomVideo.palette = palette;
    }
}
