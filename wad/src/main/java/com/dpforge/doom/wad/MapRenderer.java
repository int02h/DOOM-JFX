package com.dpforge.doom.wad;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class MapRenderer {

    private final int padding = 32;
    private final int scale = 2;
    // 1 - do not flip vertically, -1 - do flip
    private final int verticalFlip = -1;

    public BufferedImage render(WadMap map) {
        int minX = Short.MAX_VALUE;
        int minY = Short.MAX_VALUE;
        int maxX = Short.MIN_VALUE;
        int maxY = Short.MIN_VALUE;
        for (Vertex v : map.vertexes) {
            minX = Math.min(minX, v.x() / scale);
            minY = Math.min(minY, verticalFlip * v.y() / scale);
            maxX = Math.max(maxX, v.x() / scale);
            maxY = Math.max(maxY, verticalFlip * v.y() / scale);
        }
        int width = maxX - minX + 2 * padding;
        int height = maxY - minY + 2 * padding;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Get the Graphics2D object
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(8f / scale));
        for (LineDef line : map.lineDefs) {
            Vertex start = map.vertexes[line.startVertex()];
            Vertex end = map.vertexes[line.endVertex()];
            g.drawLine(
                    start.x() / scale + padding - minX,
                    verticalFlip * start.y() / scale + padding - minY,
                    end.x() / scale + padding - minX,
                    verticalFlip * end.y() / scale + padding - minY
            );
        }

        g.dispose();
        return image;
    }
}
