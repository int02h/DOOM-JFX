package com.dpforge.doom.wad;

public record Thing(
        int x,
        int y,
        int angle,
        ThingType type,
        int flags
) {
}
