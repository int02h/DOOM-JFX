package com.dpforge.doom.wad;

public record LineDef(
        int startVertex,
        int endVertex,
        int flags,
        int specialType,
        int sectorTag,
        int rightSideDef,
        int leftSideDef
) {
}
