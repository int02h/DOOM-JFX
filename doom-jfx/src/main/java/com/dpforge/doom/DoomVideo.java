package com.dpforge.doom;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DoomVideo {

    private static final int COLOR_SIZE = 3;

    private static int screenWidth;
    private static int screenHeight;
    private static ByteBuffer mainScreen;
    private static byte[] palette;
    private static BufferedImage renderedScreen;

    private static final List<DoomEvent> keyboardEvents = new ArrayList<>();
    private static DoomDisplay display;

    public native static int getScreenWidth();

    public native static int getScreenHeight();

    public native static ByteBuffer getScreenBuffer(int index, int size);

    private native static void onKeyDown(int keyCode);

    private native static void onKeyUp(int keyCode);

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

    public static void startFrame() {
        keyboardEvents.clear();
        display.keyboard.getAllCodes(keyboardEvents);
        for (DoomEvent event : keyboardEvents) {
            switch (event.type()) {
                case KEY_DOWN -> onKeyDown(event.code());
                case KEY_UP -> onKeyUp(event.code());
            }
        }
    }
}
