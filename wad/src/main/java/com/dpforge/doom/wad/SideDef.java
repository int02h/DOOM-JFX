package com.dpforge.doom.wad;

public record SideDef(
        int xOffset,
        int yOffset,
        String upperTexture,
        String lowerTexture,
        String middleTexture,
        int facingSectorNumber
) {
    public static final int NO_SIDE_DEF = 0xFFFF;
}
