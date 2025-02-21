package com.dpforge.doom.wad;

import java.awt.Color;

public class WadFile {

    WadType type;
    int lumpNumber;
    int infoTableOffset;

    public Color[][] palettes;
    public byte[][] colorMaps;
    public Thing[] things;
    public LineDef[] lineDefs;

    public EndDoom endDoom;

    @Override
    public String toString() {
        return new StringBuilder()
                .append(formatProperty("type", type))
                .append(formatProperty("lumpNumber", lumpNumber))
                .append(formatProperty("infoTableOffset", infoTableOffset))
                .append(formatProperty("palette number", palettes.length))
                .append(formatProperty("color map number", colorMaps.length))
                .append(formatProperty("thing number", things.length))
                .append(formatProperty("end doom", endDoom.asText()))
                .toString();
    }

    private String formatProperty(String name, Object value) {
        return String.format("%-20s%s\n", name, value);
    }
}
