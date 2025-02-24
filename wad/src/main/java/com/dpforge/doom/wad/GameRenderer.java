package com.dpforge.doom.wad;

import java.util.ArrayList;
import java.util.List;

public class GameRenderer {

    private static final int SSECTOR_MASK = 0x7FFF;
    // FOV (Field of View) is the angular width of the player's vision in the game world.
    // It determines how much of the environment is visible on the screen at a given moment.
    // Doom's FOV is fixed at 90° (by default). This means the player can see a 90-degree cone in front of them.
    // Doom's default FOV is 90 degrees, which in BAM is (90/360) * 2³² = 0x40000000.
    private static final float FOV = 90f;

    private final WadMap map;

    private int cameraX;
    private int cameraY;
    /**
     * In Doom's coordinate system, angles are measured in binary angle format, where:
     * 0° (0 in binary angles) → East (+X direction)
     * 90° (¼ of full circle) → North (+Y direction)
     * 180° (½ of full circle) → West (-X direction)
     * 270° (¾ of full circle) → South (-Y direction)
     */
    private float cameraAngle;

    public GameRenderer(WadMap map) {
        this.map = map;
    }

    public void setCamera(int x, int y, float angle) {
        this.cameraX = x;
        this.cameraY = y;
        this.cameraAngle = angle;
    }

    public void render() {
        Node root = map.nodes[map.nodes.length - 1];
        walk(root);
    }

    private void walk(Node node) {
        int side = (cameraX - node.partitionLineX()) * node.partitionLineDY()
                - (cameraY - node.partitionLineY()) * node.partitionLineDX();
        // To determine which side is front and which is back, DOOM uses the partition line as a reference:
        // - The front side consists of all map points to the "right" of the partition line.
        // - The back side consists of all map points to the "left" of the partition line.
        if (side > 0) { // the camera is on the front side
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
        } else if (side < 0) { // the camera is on the back side
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
            int lineSide = (cameraX - start.x()) * (end.y() - start.y())
                    - (cameraY - start.y()) * (end.x() - start.x());
            boolean isFrontSide = lineSide > 0;
            int sideDefNum = isFrontSide ? line.rightSideDef() : line.leftSideDef();
            if (sideDefNum == SideDef.NO_SIDE_DEF) {
                continue;
            }
            SideDef side = map.sideDefs[sideDefNum];
            drawSide(side);
        }
    }

    private void drawSide(SideDef side) {

    }

    private static boolean isLeaf(int nodeNumber) {
        return (nodeNumber & (1 << 15)) != 0;
    }

    private boolean isBoundingBoxInFrustum(BBox bbox) {
        // Compute frustum boundary angles
        double leftAngle = Math.toRadians(cameraAngle) - Math.toRadians(FOV) / 2;
        double rightAngle = Math.toRadians(cameraAngle) + Math.toRadians(FOV) / 2;

        // AABB corners
        int[][] corners = {
                {bbox.left(), bbox.top()}, // Top-left
                {bbox.right(), bbox.top()}, // Top-right
                {bbox.left(), bbox.bottom()}, // Bottom-left
                {bbox.right(), bbox.bottom()}  // Bottom-right
        };

        // Check if any corner is inside the frustum
        for (int[] corner : corners) {
            if (isPointInFrustum(corner[0], corner[1], leftAngle, rightAngle)) {
                return true;
            }
        }

        double[][] edges = {
                {bbox.left(), bbox.top(), bbox.right(), bbox.top()}, // Top edge
                {bbox.right(), bbox.top(), bbox.right(), bbox.bottom()}, // Right edge
                {bbox.left(), bbox.bottom(), bbox.right(), bbox.bottom()}, // Bottom edge
                {bbox.left(), bbox.top(), bbox.left(), bbox.bottom()}  // Left edge
        };

        // Compute frustum boundary lines
        double frustumFar = 10000; // Arbitrary large distance for frustum lines
        double leftEndX = cameraX + Math.cos(leftAngle) * frustumFar;
        double leftEndY = cameraY + Math.sin(leftAngle) * frustumFar;
        double rightEndX = cameraX + Math.cos(rightAngle) * frustumFar;
        double rightEndY = cameraY + Math.sin(rightAngle) * frustumFar;

        // Check if any AABB edge intersects the frustum
        for (double[] edge : edges) {
            if (linesIntersect(cameraX, cameraY, leftEndX, leftEndY, edge[0], edge[1], edge[2], edge[3]) ||
                    linesIntersect(cameraX, cameraY, rightEndX, rightEndY, edge[0], edge[1], edge[2], edge[3])) {
                return true;
            }
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
}

