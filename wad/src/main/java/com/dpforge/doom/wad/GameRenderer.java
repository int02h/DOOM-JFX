package com.dpforge.doom.wad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Map;

public class GameRenderer {

    private static final int SSECTOR_MASK = 0x7FFF;
    // FOV (Field of View) is the angular width of the player's vision in the game world.
    // It determines how much of the environment is visible on the screen at a given moment.
    // Doom's FOV is fixed at 90° (by default). This means the player can see a 90-degree cone in front of them.
    // Doom's default FOV is 90 degrees, which in BAM is (90/360) * 2³² = 0x40000000.
    private static final float FOV = 90f;

    private static final int SCREEN_WIDTH = 320;
    private static final int SCREEN_HEIGHT = 240;
    private static final int PLAYER_HEIGHT = 56;
    private static final boolean NO_TEXTURING = false;

    private final WadMap map;
    private final Map<String, BufferedImage> graphics;

    private final Path2D polygon = new Path2D.Double();

    private int cameraX;
    private int cameraY;
    private int cameraZ;
    /**
     * In Doom's coordinate system, angles are measured in binary angle format, where:
     * 0° (0 in binary angles) → East (+X direction)
     * 90° (¼ of full circle) → North (+Y direction)
     * 180° (½ of full circle) → West (-X direction)
     * 270° (¾ of full circle) → South (-Y direction)
     */
    private float cameraAngle;

    private final int[] xy = new int[2];

    final BufferedImage image;
    private final Graphics2D g;

    public GameRenderer(WadMap map, Map<String, BufferedImage> graphics) {
        this.map = map;
        this.graphics = graphics;

        image = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public void setCamera(int x, int y, float angle) {
        this.cameraX = x;
        this.cameraY = y;
        this.cameraZ = 56;
        this.cameraAngle = angle;
    }

    public void render() {
        long start = System.currentTimeMillis();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        Node root = map.nodes[map.nodes.length - 1];
        walk(root);
        long elapsed = System.currentTimeMillis() - start;
        System.out.format("Frame rendering took %d ms", elapsed);
    }

    private void walk(Node node) {
        int side = (cameraX - node.partitionLineX()) * node.partitionLineDY()
                - (cameraY - node.partitionLineY()) * node.partitionLineDX();
        // To determine which side is front and which is back, DOOM uses the partition line as a reference:
        // - The front side consists of all map points to the "right" of the partition line.
        // - The back side consists of all map points to the "left" of the partition line.
        if (side > 0) { // the camera is on the front side
            if (isBoundingBoxInFrustum(node.leftBBox())) {
                if (isLeaf(node.leftChild())) {
                    drawSubSector(node.leftChild() & SSECTOR_MASK);
                } else {
                    walk(map.nodes[node.leftChild()]);
                }
            }
            if (isBoundingBoxInFrustum(node.rightBBox())) {
                if (isLeaf(node.rightChild())) {
                    drawSubSector(node.rightChild() & SSECTOR_MASK);
                } else {
                    walk(map.nodes[node.rightChild()]);
                }
            }
        } else if (side < 0) { // the camera is on the back side
            if (isBoundingBoxInFrustum(node.rightBBox())) {
                if (isLeaf(node.rightChild())) {
                    drawSubSector(node.rightChild() & SSECTOR_MASK);
                } else {
                    walk(map.nodes[node.rightChild()]);
                }
            }
            if (isBoundingBoxInFrustum(node.leftBBox())) {
                if (isLeaf(node.leftChild())) {
                    drawSubSector(node.leftChild() & SSECTOR_MASK);
                } else {
                    walk(map.nodes[node.leftChild()]);
                }
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void drawSubSector(int ssectorNumber) {
        SSector ssector = map.ssectors[ssectorNumber];
        for (int segNum = ssector.firstSeg(); segNum < ssector.firstSeg() + ssector.segCount(); segNum++) {
            Seg seg = map.segs[segNum];
            LineDef line = map.lineDefs[seg.lineDef()];

            Vertex start = map.vertexes[line.startVertex()];
            Vertex end = map.vertexes[line.endVertex()];
            // SUGGESTION: probably the texture should be mirrored. But maybe the order of vertexes is enough.
            boolean isFrontSide = seg.direction() == Seg.Direction.SAME;
            int sideDefNum = isFrontSide ? line.rightSideDef() : line.leftSideDef();
            if (sideDefNum == SideDef.NO_SIDE_DEF) {
                continue;
            }
            int backSideDefNum = !isFrontSide ? line.rightSideDef() : line.leftSideDef();
            SideDef backSide = backSideDefNum == SideDef.NO_SIDE_DEF ? null : map.sideDefs[backSideDefNum];
            SideDef side = map.sideDefs[sideDefNum];
            drawSide(side, start, end, backSide, seg.offset());
        }
    }

    private void drawSide(SideDef side, Vertex v1, Vertex v2, SideDef backSide, int offset) {
        Sector sector = map.sectors[side.facingSectorNumber()];
        Sector backSector = backSide != null ? map.sectors[backSide.facingSectorNumber()] : null;

        int width = Math.round(lineLength(v1.x(), v1.y(), v2.x(), v2.y()));

        if (!side.lowerTexture().equals(Texture.NO_TEXTURE) && backSector != null) {
            boolean p1 = projectPoint(v1.x(), v1.y(), sector.floorHeight(), xy);
            int fx1 = xy[0], fy1 = xy[1];
            boolean p2 = projectPoint(v2.x(), v2.y(), sector.floorHeight(), xy);
            int fx2 = xy[0], fy2 = xy[1];

            boolean p3 = projectPoint(v1.x(), v1.y(), backSector.floorHeight(), xy);
            int cy1 = xy[1];
            boolean p4 = projectPoint(v2.x(), v2.y(), backSector.floorHeight(), xy);
            int cy2 = xy[1];

            if (p1 && p2 && p3 && p4) {
                int height = Math.abs(backSector.floorHeight() - sector.floorHeight());
                drawTexture(fx1, fy1, cy1, fx2, fy2, cy2, side.lowerTexture(), width, height, side.xOffset() + offset, side.yOffset());
            }
        }

        if (!side.middleTexture().equals(Texture.NO_TEXTURE)) {
            boolean p1 = projectPoint(v1.x(), v1.y(), sector.floorHeight(), xy);
            int fx1 = xy[0], fy1 = xy[1];
            boolean p2 = projectPoint(v2.x(), v2.y(), sector.floorHeight(), xy);
            int fx2 = xy[0], fy2 = xy[1];

            boolean p3 = projectPoint(v1.x(), v1.y(), sector.ceilingHeight(), xy);
            int cy1 = xy[1];
            boolean p4 = projectPoint(v2.x(), v2.y(), sector.ceilingHeight(), xy);
            int cy2 = xy[1];

            if (p1 && p2 && p3 && p4) {
                int height = Math.abs(sector.ceilingHeight() - sector.floorHeight());
                drawTexture(fx1, fy1, cy1, fx2, fy2, cy2, side.middleTexture(), width, height, side.xOffset() + offset, side.yOffset());
            }
        }

        if (!side.upperTexture().equals(Texture.NO_TEXTURE) & backSector != null) {
            boolean p1 = projectPoint(v1.x(), v1.y(), backSector.ceilingHeight(), xy);
            int fx1 = xy[0], fy1 = xy[1];
            boolean p2 = projectPoint(v2.x(), v2.y(), backSector.ceilingHeight(), xy);
            int fx2 = xy[0], fy2 = xy[1];

            boolean p3 = projectPoint(v1.x(), v1.y(), sector.ceilingHeight(), xy);
            int cy1 = xy[1];
            boolean p4 = projectPoint(v2.x(), v2.y(), sector.ceilingHeight(), xy);
            int cy2 = xy[1];

            if (p1 && p2 && p3 && p4) {
                int height = Math.abs(sector.ceilingHeight() - backSector.ceilingHeight());
                drawTexture(fx1, fy1, cy1, fx2, fy2, cy2, side.upperTexture(), width, height, side.xOffset() + offset, side.yOffset());
            }
        }
    }

    private void drawTexture(
            int x1, int fy1, int cy1,
            int x2, int fy2, int cy2,
            String name,
            int width,
            int height,
            int tOffsetX,
            int tOffsetY
    ) {
        if (x1 == x2 || fy1 >= cy1 || fy2 >= cy2) {
            return;
        }

        if (NO_TEXTURING) {
            polygon.reset();
            polygon.moveTo(x1, fy1);
            polygon.lineTo(x2, fy2);
            polygon.lineTo(x2, cy2);
            polygon.lineTo(x1, cy1);
            polygon.closePath();
            g.setColor(Color.LIGHT_GRAY);
            g.fill(polygon);
            g.setColor(Color.BLACK);
            g.draw(polygon);
            return;
        }

        BufferedImage texture = graphics.get(name.toUpperCase());

        float length = lineLength(x1, cy1, x2, cy2);
        float dx = (x2 - x1) / length;
        float dcy = (cy2 - cy1) / length;
        float dfy = (fy2 - fy1) / length;

        float x = x1;
        float cy = cy1;
        float fy = fy1;

        float widthScale = 1f * width / length;
        float tx = tOffsetX;

        for (int i = 0; i <= length; i++) {
            drawTextureColumn(texture, height, Math.round(tx), tOffsetY, Math.round(x), fy, cy);
            cy += dcy;
            fy += dfy;
            x += dx;
            tx += widthScale;
        }
    }

    private void drawTextureColumn(BufferedImage texture, int height, int tx, int tyOffset, int x, float y1, float y2) {
        if (tx < 0) return;

        float scale = 1f * height / Math.abs(y2 - y1);

        float y = y1;
        float length = Math.abs(y2 - y1);
        float dy = (y2 - y1) / length;

        // DOOM renders wall textures from top to bottom
        float ty = tyOffset + height;

        for (int i = 0; i < length; i++) {
            int pixel = texture.getRGB(tx % texture.getWidth(), Math.round(ty) % texture.getHeight());
            if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) continue;
            image.setRGB(x, SCREEN_HEIGHT - Math.round(y), pixel);
            y += dy;
            ty -= scale;
        }
    }

    private static boolean isLeaf(int nodeNumber) {
        return (nodeNumber & (1 << 15)) != 0;
    }

    private boolean isBoundingBoxInFrustum(BBox bbox) {
        // Compute frustum boundary angles
        double leftAngle = Math.toRadians(cameraAngle) - Math.toRadians(FOV) / 2;
        double rightAngle = Math.toRadians(cameraAngle) + Math.toRadians(FOV) / 2;

        // Check if any corner is inside the frustum
        if (isPointInFrustum(bbox.left(), bbox.top(), leftAngle, rightAngle)) {
            return true;
        }
        if (isPointInFrustum(bbox.right(), bbox.top(), leftAngle, rightAngle)) {
            return true;
        }
        if (isPointInFrustum(bbox.left(), bbox.bottom(), leftAngle, rightAngle)) {
            return true;
        }
        if (isPointInFrustum(bbox.right(), bbox.bottom(), leftAngle, rightAngle)) {
            return true;
        }

        // Compute frustum boundary lines
        double frustumFar = 10000; // Arbitrary large distance for frustum lines
        double leftEndX = cameraX + Math.cos(leftAngle) * frustumFar;
        double leftEndY = cameraY + Math.sin(leftAngle) * frustumFar;
        double rightEndX = cameraX + Math.cos(rightAngle) * frustumFar;
        double rightEndY = cameraY + Math.sin(rightAngle) * frustumFar;

        // Check if any AABB edge intersects the frustum
        if (linesIntersect(cameraX, cameraY, leftEndX, leftEndY, bbox.left(), bbox.top(), bbox.right(), bbox.top()) ||
                linesIntersect(cameraX, cameraY, rightEndX, rightEndY, bbox.left(), bbox.top(), bbox.right(), bbox.top())) {
            return true;
        }
        if (linesIntersect(cameraX, cameraY, leftEndX, leftEndY, bbox.right(), bbox.top(), bbox.right(), bbox.bottom()) ||
                linesIntersect(cameraX, cameraY, rightEndX, rightEndY, bbox.right(), bbox.top(), bbox.right(), bbox.bottom())) {
            return true;
        }
        if (linesIntersect(cameraX, cameraY, leftEndX, leftEndY, bbox.left(), bbox.bottom(), bbox.right(), bbox.bottom()) ||
                linesIntersect(cameraX, cameraY, rightEndX, rightEndY, bbox.left(), bbox.bottom(), bbox.right(), bbox.bottom())) {
            return true;
        }
        if (linesIntersect(cameraX, cameraY, leftEndX, leftEndY, bbox.left(), bbox.top(), bbox.left(), bbox.bottom()) ||
                linesIntersect(cameraX, cameraY, rightEndX, rightEndY, bbox.left(), bbox.top(), bbox.left(), bbox.bottom())) {
            return true;
        }

        return false;
    }

    // Check if a point is inside the frustum
    private boolean isPointInFrustum(int px, int py, double leftAngle, double rightAngle) {
        // Compute angle from camera to point
        double angleToPoint = Math.atan2(py - cameraY, px - cameraX);

        // Normalize angles to [0, 2π] range
        leftAngle = normalizeAngle(leftAngle);
        rightAngle = normalizeAngle(rightAngle);
        angleToPoint = normalizeAngle(angleToPoint);

        if (leftAngle <= rightAngle) {
            return angleToPoint >= leftAngle && angleToPoint <= rightAngle;
        } else {
            // Handles cases where the frustum crosses the -π/π boundary
            return angleToPoint >= leftAngle || angleToPoint <= rightAngle;
        }
    }

    private static double normalizeAngle(double angle) {
        while (angle < 0) angle += 2 * Math.PI;
        while (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
        return angle;
    }

    // Check if two line segments intersect
    private static boolean linesIntersect(double x1, double y1, double x2, double y2,
                                          double x3, double y3, double x4, double y4) {
        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d == 0) return false; // Parallel lines

        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / d;
        double u = ((x1 - x3) * (y1 - y2) - (y1 - y3) * (x1 - x2)) / d;

        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }

    private boolean projectPoint(int x, int y, int z, int[] output) {
        // Convert angles to radians
        float cameraRad = (float) Math.toRadians(cameraAngle);
        float fovRad = (float) Math.toRadians(FOV);

        // Step 1: Translate world coordinates (move camera to the origin)
        float Xp = x - cameraX;
        float Yp = y - cameraY;
        float Zp = z - (cameraZ + PLAYER_HEIGHT);

        // Step 2: Rotate around Z-axis (align camera view)
        float Xr = (float) (Xp * Math.cos(-cameraRad) - Yp * Math.sin(-cameraRad));
        float Yr = (float) (-Xp * Math.sin(-cameraRad) + Yp * Math.cos(-cameraRad));
        float Zr = Zp;  // No change in depth

        // Step 3: Perspective Projection
        if (Xr <= 0) return false;  // If behind the camera, don't render

        float S = (float) (SCREEN_WIDTH / (2 * Math.tan(fovRad / 2)));

        int screenX = Math.round(SCREEN_WIDTH / 2f + (Yr / Xr) * S);
        int screenY = Math.round(SCREEN_HEIGHT / 2f + (Zr / Xr) * S);
        output[0] = screenX;
        output[1] = screenY;

        return true;
    }

    private static float lineLength(int x1, int y1, int x2, int y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

}

