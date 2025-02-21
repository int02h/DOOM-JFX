package com.dpforge.doom.wad;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// https://doom.fandom.com/wiki/WAD
public class WadFileReader {

    private static final int PALETTE_COLOR_NUMBER = 256;
    private static final int PALETTE_COLOR_SIZE = 3;
    private static final int COLOR_MAP_SIZE = 256;

    private WadFile wad;
    private BinaryReader reader;
    private String currentMap;

    public WadFile read(File wadFile) throws IOException, WadException {
        wad = new WadFile();
        try (FileInputStream fis = new FileInputStream(wadFile)) {
            reader = new BinaryReader(fis.readAllBytes());
        }
        readHeader();
        readDirectory();
        return wad;
    }

    private void readHeader() throws WadException {
        // Header
        String wadType = reader.readAsciiString(4);
        try {
            wad.type = WadType.valueOf(wadType);
        } catch (IllegalArgumentException e) {
            throw new WadException("Unknown type of WAD file: " + wadType);
        }

        wad.lumpNumber = reader.readInt32();
        wad.infoTableOffset = reader.readInt32();
    }

    private void readDirectory() throws WadException {
        reader.setOffset(wad.infoTableOffset);
        for (int i = 0; i < wad.lumpNumber; i++) {
            int lumpPosition = reader.readInt32();
            int lumpSize = reader.readInt32();
            String name = reader.readNullPaddedAsciiString(8);

            int tmpOffset = reader.setOffset(lumpPosition);
            switch (name) {
                case "PLAYPAL" -> readPlayPal(lumpSize);
                case "COLORMAP" -> readColorMap(lumpSize);
                case "ENDOOM" -> readEndDoom(lumpSize);
                case "DEMO1", "DEMO2", "DEMO3" -> readDemo(name, lumpSize);
                case "THINGS" -> readThings(lumpSize);
                case "LINEDEFS" -> readLineDefs(lumpSize);
                default -> {
                    if (name.length() == 4
                            && name.charAt(0) == 'E'
                            && name.charAt(2) == 'M'
                            && Character.isDigit(name.charAt(1))
                            && Character.isDigit(name.charAt(3))
                    ) {
                        currentMap = name;
                    } else {
                        throw new WadException("Unknown lump %s of size %d", name, lumpSize);
                    }
                }
            }
            reader.setOffset(tmpOffset);
        }
    }

    private void readPlayPal(int size) {
        int paletteCount = size / (PALETTE_COLOR_NUMBER * PALETTE_COLOR_SIZE);
        wad.palettes = new Color[paletteCount][];
        for (int paletteIndex = 0; paletteIndex < paletteCount; paletteIndex++) {
            Color[] palette = wad.palettes[paletteIndex] = new Color[PALETTE_COLOR_NUMBER];
            for (int colorIndex = 0; colorIndex < PALETTE_COLOR_NUMBER; colorIndex++) {
                int r = reader.readInt8();
                int g = reader.readInt8();
                int b = reader.readInt8();
                palette[colorIndex] = new Color(r, g, b);
            }
        }
    }

    private void readColorMap(int size) {
        int mapCount = size / COLOR_MAP_SIZE;
        wad.colorMaps = new byte[mapCount][];
        for (int i = 0; i < mapCount; i++) {
            wad.colorMaps[i] = reader.readBytes(COLOR_MAP_SIZE);
        }
    }

    private void readEndDoom(int size) throws WadException {
        if (size != 80 * 25 * 2) throw new WadException("Wrong size of ENDOOM: " + size);
        wad.endDoom = new EndDoom(reader.readBytes(size));
    }

    private void readDemo(String name, int lumpSize) {
        System.out.format("Skipping demo %s of size %d\n", name, lumpSize);
    }

    private void readThings(int size) throws WadException {
        int thingCount = size / 10;
        System.out.format("Reading %d THINGS for map %s\n", thingCount, currentMap);

        wad.things = new Thing[thingCount];

        for (int i = 0; i < thingCount; i++) {
            int x = reader.readInt16();
            int y = reader.readInt16();
            int angle = reader.readInt16();
            int type = reader.readInt16();
            int flags = reader.readInt16();
            wad.things[i] = new Thing(x, y, angle, ThingType.find(type), flags);
        }
    }

    private void readLineDefs(int lumpSize) {
        int lineDefCount = lumpSize / 14;
        System.out.format("Reading %d LINEDEFS for map %s\n", lineDefCount, currentMap);

        wad.lineDefs = new LineDef[lineDefCount];

        for (int i = 0; i < lineDefCount; i++) {
            int startVertex = reader.readInt16();
            int endVertex = reader.readInt16();
            int flags = reader.readInt16();
            int specialType = reader.readInt16();
            int sectorTag = reader.readInt16();
            int rightSideDef = reader.readInt16();
            int leftSideDef = reader.readInt16();
            wad.lineDefs[i] = new LineDef(
                    startVertex, endVertex, flags, specialType, sectorTag, rightSideDef, leftSideDef
            );
        }
    }
}
