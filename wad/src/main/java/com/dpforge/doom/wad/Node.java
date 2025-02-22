package com.dpforge.doom.wad;

public record Node(
        int partitionLineX,
        int partitionLineY,
        int partitionLineDX,
        int partitionLineDY,
        BBox rightBBox,
        BBox leftBBox
) {
}
