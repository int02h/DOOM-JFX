package com.dpforge.doom.wad;

public record Texture(
        String name,
        int masked,
        int width,
        int height,
        TexturePatch[] patches
) {
}
