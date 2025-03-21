package com.dpforge.doom.wad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

public class GraphicRenderer {
    private Color[] palette;

    public void setPalette(Color[] palette) {
        this.palette = palette;
    }

    public BufferedImage render(Graphic graphic) {
        if (palette == null) {
            throw new IllegalStateException("Palette is not set");
        }
        BufferedImage result = new BufferedImage(graphic.width(), graphic.height(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < graphic.height(); y++) {
            for (int x = 0; x < graphic.width(); x++) {
                Byte colorIndex = graphic.pixels()[y][x];
                if (colorIndex == null) {
                    continue;
                }
                result.setRGB(x, y, palette[colorIndex & 0xFF].getRGB());
            }
        }
        return result;
    }

    public BufferedImage render(byte[][] pixels) {
        if (palette == null) {
            throw new IllegalStateException("Palette is not set");
        }
        int width = pixels.length;
        int height = pixels[0].length;
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte colorIndex = pixels[y][x];
                result.setRGB(x, y, palette[colorIndex & 0xFF].getRGB());
            }
        }
        return result;
    }

    public BufferedImage render(Texture texture, WadFile wadFile, Map<String, BufferedImage> graphics) {
        BufferedImage result = new BufferedImage(texture.width(), texture.height(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        for (TexturePatch p : texture.patches()) {
            String name = wadFile.pnames[p.patchNumber()].toUpperCase();
            BufferedImage patch = graphics.get(name);
            g.drawImage(patch, p.xOffset(), p.yOffset(), null);
        }
        g.dispose();
        return result;
    }
}
