package com.dpforge.doom.wad;

public record Sector(
        int floorHeight,
        int ceilingHeight,
        String floorTexture,
        String ceilingTexture,
        int lightLevel,
        int type,
        int tag
) {
}
