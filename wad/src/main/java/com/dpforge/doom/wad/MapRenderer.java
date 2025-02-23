package com.dpforge.doom.wad;

import kotlin.Pair;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapRenderer {

    private final WadMap map;
    private final Map<String, File> flats;

    private final BufferedImage image;
    private final Graphics2D g;
    private final Map<Integer, List<Integer>> sectorToSides;
    private final Map<Integer, Integer> sideToLine;

    private final int minX;
    private final int minY;

    private final int padding = 32;
    private final int scale = 2;
    // 1 - do not flip vertically, -1 - do flip
    private final int verticalFlip = -1;

    public MapRenderer(WadMap map, Map<String, File> flats) {
        this.map = map;
        this.flats = flats;

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
        this.minX = minX;
        this.minY = minY;

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Get the Graphics2D object
        g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Init mapping for a quick access
        sectorToSides = new HashMap<>();
        for (int i = 0; i < map.sideDefs.length; i++) {
            SideDef side = map.sideDefs[i];
            sectorToSides.computeIfAbsent(side.facingSectorNumber(), k -> new ArrayList<>()).add(i);
        }

        sideToLine = new HashMap<>();
        for (int i = 0; i < map.lineDefs.length; i++) {
            final LineDef line = map.lineDefs[i];
            if (line.leftSideDef() != SideDef.NO_SIDE_DEF) {
                sideToLine.put(line.leftSideDef(), i);
            }
            if (line.rightSideDef() != SideDef.NO_SIDE_DEF) {
                sideToLine.put(line.rightSideDef(), i);
            }
        }
    }

    public BufferedImage render() throws IOException {
        for (int i = 0; i < map.sectors.length; i++) {
            Sector sector = map.sectors[i];
            List<Path2D> polygons = extractPolygons(i);
            for (Path2D polygon : polygons) {
                BufferedImage texture = ImageIO.read(flats.get(sector.floorTexture()));
                // Define a TexturePaint with the loaded texture
                Rectangle rect = new Rectangle(0, 0, texture.getWidth(), texture.getHeight());
                TexturePaint texturePaint = new TexturePaint(texture, rect);
                // Set the paint to the texture
                g.setPaint(texturePaint);

                g.fill(polygon);
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(8f / scale));
                g.draw(polygon);
            }
        }

        renderThings();

        g.dispose();
        return image;
    }

    private void renderThings() {
        g.setColor(Color.BLUE);
        int dotSize = 8 * scale;
        for (Thing t : map.things) {
            g.fillOval(
                    t.x() / scale + padding - minX - dotSize / 2,
                    verticalFlip * t.y() / scale + padding - minY - dotSize / 2,
                    dotSize,
                    dotSize
            );
        }
    }

    private List<Path2D> extractPolygons(int sectorNumber) {
        List<Integer> sides = sectorToSides.get(sectorNumber);
        List<Pair<Vertex, Vertex>> vertexes = new ArrayList<>();

        for (int sideNumber : sides) {
            int lineNumber = sideToLine.get(sideNumber);
            LineDef line = map.lineDefs[lineNumber];
            Vertex start = map.vertexes[line.startVertex()];
            Vertex end = map.vertexes[line.endVertex()];
            vertexes.add(new Pair<>(start, end));
        }

        List<Path2D> result = new ArrayList<>();
        while (!vertexes.isEmpty()) {
            Path2D polygon = new Path2D.Double();
            result.add(polygon);
            final Vertex startVertex = vertexes.get(0).getFirst();
            polygon.moveTo(
                    (double) startVertex.x() / scale + padding - minX,
                    (double) verticalFlip * startVertex.y() / scale + padding - minY
            );
            var start = startVertex;
            while (true) {
                Vertex end = null;
                for (var pair : vertexes) {
                    if (pair.getFirst() == start) {
                        end = pair.getSecond();
                        vertexes.remove(pair);
                        break;
                    } else if (pair.getSecond() == start) {
                        end = pair.getFirst();
                        vertexes.remove(pair);
                        break;
                    }
                }
                if (end == null) {
                    polygon.closePath();
                    break;
                }
                polygon.lineTo(
                        (double) end.x() / scale + padding - minX,
                        (double) verticalFlip * end.y() / scale + padding - minY
                );
                if (end == startVertex) {
                    break;
                }
                start = end;
            }
        }
        return result;
    }
}
