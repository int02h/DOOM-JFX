package com.dpforge.doom.wad;

public record Graphic(
        int width,
        int height,
        int xOffset,
        int yOffset,
        Byte[][] pixels
) {
}
