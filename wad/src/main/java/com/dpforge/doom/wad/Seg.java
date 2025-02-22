package com.dpforge.doom.wad;

/**
 *
 * @param offset distance along linedef to start of seg
 */
public record Seg(
        int startVertex,
        int endVertex,
        int angle,
        int lineDef,
        Direction direction,
        int offset
) {
    enum Direction {
        // same as linedef
        SAME,
        // opposite of linedef
        OPPOSITE
    }
}
