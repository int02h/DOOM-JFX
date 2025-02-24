package com.dpforge.doom.wad;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
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

    public RenderedImage render(Texture texture, WadFile wadFile, Map<String, File> graphics) throws IOException {
        BufferedImage result = new BufferedImage(texture.width(), texture.height(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        for (TexturePatch p : texture.patches()) {
            String name = wadFile.pnames[p.patchNumber()];
            File file = graphics.get(name);
            if (file == null) {
                file = graphics.get(name.toUpperCase());
                if (file == null) {
                    System.err.format("Could not find patch %s\n", name);
                    continue;
                } else {
                    System.out.format("Bad patch name. Expected: (%s). Actual: (%s)\n", name.toUpperCase(), name);
                }
            }
            BufferedImage patch = ImageIO.read(file);
            g.drawImage(patch, p.xOffset(), p.yOffset(), null);
        }
        g.dispose();
        return result;
    }
}
