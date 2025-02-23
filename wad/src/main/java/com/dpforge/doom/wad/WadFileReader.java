package com.dpforge.doom.wad;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

// https://doom.fandom.com/wiki/WAD
public class WadFileReader {

    private static final int PALETTE_COLOR_NUMBER = 256;
    private static final int PALETTE_COLOR_SIZE = 3;
    private static final int COLOR_MAP_SIZE = 256;

    private WadFile wad;
    private BinaryReader reader;
    private WadDirectory currentDir;
    private Stack<WadDirectory> dirStack = new Stack<>();
    private WadMap currentMap;

    public WadFile read(File wadFile) throws IOException, WadException {
        wad = new WadFile();
        currentDir = wad.directory;
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
            String lumpName = reader.readNullPaddedAsciiString(8);

            if (lumpSize == 0) {
                if (isMapName(lumpName)) {
                    currentMap = new WadMap(lumpName);
                    wad.maps.put(lumpName, currentMap);
                } else if (lumpName.endsWith("_START")) {
                    var parentDir = currentDir;
                    currentDir = new WadDirectory(lumpName.replace("_START", ""));
                    dirStack.push(parentDir);
                    parentDir.addDirectory(currentDir);
                } else if (lumpName.endsWith("_END")) {
                    currentDir = dirStack.pop();
                } else {
                    throw new WadException("Unknown marker " + lumpName);
                }
                continue;
            }

            int tmpOffset = reader.setOffset(lumpPosition);
            switch (lumpName) {
                // map lumps
                case "THINGS" -> readThings(lumpSize);
                case "LINEDEFS" -> readLineDefs(lumpSize);
                case "SIDEDEFS" -> readSideDefs(lumpSize);
                case "VERTEXES" -> readVertexes(lumpSize);
                case "SEGS" -> readSegs(lumpSize);
                case "SSECTORS" -> readSSector(lumpSize);
                case "NODES" -> readNodes(lumpSize);
                case "SECTORS" -> readSectors(lumpSize);
                case "REJECT" -> readReject(lumpSize);
                case "BLOCKMAP" -> readBlockmap(lumpSize);
                // common lumps
                case "TEXTURE1", "TEXTURE2" -> System.out.format("Skip %s of size %d\n", lumpName, lumpSize);
                case "PNAMES" -> readPNames(lumpSize);
                case "GENMIDI" -> readGenMidi(lumpSize);
                case "DMXGUS", "DMXGUSC" -> readDmxGus(lumpSize);
                case "PLAYPAL" -> readPlayPal(lumpSize);
                case "COLORMAP" -> readColorMap(lumpSize);
                case "ENDOOM" -> readEndDoom(lumpSize);
                case "DEMO1", "DEMO2", "DEMO3" -> readDemo(lumpName, lumpSize);
                default -> {
                    if (lumpName.startsWith("DP")) {
                        System.out.println("Ignore sound " + lumpName);
                    } else if (lumpName.startsWith("DS")) {
                        readSound(lumpName, lumpSize);
                    } else if (lumpName.startsWith("D_")) {
                        System.out.println("Ignore music " + lumpName);
                    } else if (currentDir.name.startsWith("S")) {
                        readPicture(lumpName, lumpSize);
                    } else if (currentDir.name.startsWith("F")) {
                        readFlat(lumpName, lumpSize);
                    } else {
                        try {
                            readPicture(lumpName, lumpSize);
                        } catch (WadException e) {
                            throw new WadException("Lump %s is not a sprite", lumpName);
                        }
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
        System.out.format("Reading %d THINGS for map %s\n", thingCount, currentMap.name);

        currentMap.things = new Thing[thingCount];

        for (int i = 0; i < thingCount; i++) {
            int x = reader.readSignedInt16();
            int y = reader.readSignedInt16();
            int angle = reader.readInt16();
            int type = reader.readInt16();
            int flags = reader.readInt16();
            currentMap.things[i] = new Thing(x, y, angle, ThingType.find(type), flags);
        }
    }

    private void readLineDefs(int lumpSize) {
        int lineDefCount = lumpSize / 14;
        System.out.format("Reading %d LINEDEFS for map %s\n", lineDefCount, currentMap.name);

        currentMap.lineDefs = new LineDef[lineDefCount];

        for (int i = 0; i < lineDefCount; i++) {
            int startVertex = reader.readInt16();
            int endVertex = reader.readInt16();
            int flags = reader.readInt16();
            int specialType = reader.readInt16();
            int sectorTag = reader.readInt16();
            int rightSideDef = reader.readInt16();
            int leftSideDef = reader.readInt16();
            currentMap.lineDefs[i] = new LineDef(
                    startVertex, endVertex, flags, specialType, sectorTag, rightSideDef, leftSideDef
            );
        }
    }

    private void readSideDefs(int lumpSize) {
        int sideDefCount = lumpSize / 30;
        System.out.format("Reading %d SIDEDEFS for map %s\n", sideDefCount, currentMap.name);

        currentMap.sideDefs = new SideDef[sideDefCount];

        for (int i = 0; i < sideDefCount; i++) {
            int xOffset = reader.readInt16();
            int yOffset = reader.readInt16();
            String upperTexture = reader.readNullPaddedAsciiString(8);
            String lowerTexture = reader.readNullPaddedAsciiString(8);
            String middleTexture = reader.readNullPaddedAsciiString(8);
            int facingSectorNumber = reader.readInt16();
            currentMap.sideDefs[i] = new SideDef(
                    xOffset, yOffset, upperTexture, lowerTexture, middleTexture, facingSectorNumber
            );
        }
    }

    private void readVertexes(int lumpSize) {
        int vertexCount = lumpSize / 4;
        System.out.format("Reading %d VERTEXES for map %s\n", vertexCount, currentMap.name);

        currentMap.vertexes = new Vertex[vertexCount];

        for (int i = 0; i < vertexCount; i++) {
            int x = reader.readSignedInt16();
            int y = reader.readSignedInt16();
            currentMap.vertexes[i] = new Vertex(x, y);
        }
    }

    private void readSegs(int lumpSize) {
        int segCount = lumpSize / 12;
        System.out.format("Reading %d SEGS for map %s\n", segCount, currentMap.name);

        currentMap.segs = new Seg[segCount];

        for (int i = 0; i < segCount; i++) {
            int startVertex = reader.readInt16();
            int endVertex = reader.readInt16();
            int angle = reader.readInt16();
            int lineDef = reader.readInt16();
            Seg.Direction direction = Seg.Direction.values()[reader.readInt16()];
            int offset = reader.readInt16();
            currentMap.segs[i] = new Seg(startVertex, endVertex, angle, lineDef, direction, offset);
        }
    }

    private void readSSector(int lumpSize) {
        int ssectorCount = lumpSize / 4;
        System.out.format("Reading %d SSECTORS for map %s\n", ssectorCount, currentMap.name);

        currentMap.ssectors = new SSector[ssectorCount];

        for (int i = 0; i < ssectorCount; i++) {
            int segCount = reader.readInt16();
            int firstSeg = reader.readInt16();
            currentMap.ssectors[i] = new SSector(segCount, firstSeg);
        }
    }

    private void readNodes(int lumpSize) {
        int nodeCount = lumpSize / 28;
        System.out.format("Reading %d NODES for map %s\n", nodeCount, currentMap.name);

        currentMap.nodes = new Node[nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            int partitionLineX = reader.readInt16();
            int partitionLineY = reader.readInt16();
            int partitionLineDX = reader.readInt16();
            int partitionLineDY = reader.readInt16();
            BBox rightBBox = readBBox();
            BBox leftBBox = readBBox();
            currentMap.nodes[i] = new Node(
                    partitionLineX, partitionLineY, partitionLineDX, partitionLineDY, rightBBox, leftBBox
            );
        }
    }

    private BBox readBBox() {
        int top = reader.readInt16();
        int bottom = reader.readInt16();
        int left = reader.readInt16();
        int right = reader.readInt16();
        return new BBox(top, bottom, left, right);
    }

    private void readSectors(int lumpSize) {
        int sectorCount = lumpSize / 26;
        System.out.format("Reading %d SECTORS for map %s\n", sectorCount, currentMap.name);

        currentMap.sectors = new Sector[sectorCount];

        for (int i = 0; i < sectorCount; i++) {
            int floorHeight = reader.readInt16();
            int ceilingHeight = reader.readInt16();
            String floorTexture = reader.readNullPaddedAsciiString(8);
            String ceilingTexture = reader.readNullPaddedAsciiString(8);
            int lightLevel = reader.readInt16();
            int type = reader.readInt16();
            int tag = reader.readInt16();
            currentMap.sectors[i] = new Sector(
                    floorHeight, ceilingHeight, floorTexture, ceilingTexture, lightLevel, type, tag
            );
        }
    }

    private void readReject(int lumpSize) {
        System.out.format("Reading REJECT for map %s of size %d\n", currentMap.name, lumpSize);
    }

    private void readBlockmap(int lumpSize) {
        System.out.format("Reading BLOCKMAP for map %s of size %d\n", currentMap.name, lumpSize);
    }

    private void readPNames(int lumpSize) {
        System.out.format("Reading PNAMES of size %d\n", lumpSize);
    }

    private void readGenMidi(int lumpSize) {
        System.out.format("Reading GENMIDI of size %d\n", lumpSize);
    }

    private void readDmxGus(int lumpSize) {
        System.out.format("Reading DMXGUS of size %d\n", lumpSize);
    }

    private void readSound(String lumpName, int lumpSize) {
        currentDir.sounds.put(lumpName, reader.readBytes(lumpSize));
    }

    private void readPicture(String lumpName, int lumpSize) throws WadException {
        System.out.format("Reading picture %s of size %d\n", lumpName, lumpSize);

        int lumpStart = reader.getOffset();
        int width = reader.readInt16();
        int height = reader.readInt16();
        int xOffset = reader.readSignedInt16();
        int yOffset = reader.readSignedInt16();
        int headerEnd = reader.getOffset();

        var columnOffsets = new int[width];
        for (int i = 0; i < width; i++) {
            columnOffsets[i] = reader.readInt32();
        }

        var pixels = new Byte[height][width];
        int totalCount = 0;

        for (int i = 0; i < width; i++) {
            int columnStart = lumpStart + columnOffsets[i];
            if (columnStart < headerEnd || columnStart >= lumpStart + lumpSize) {
                throw new WadException("Picture column is outside of lump");
            }

            reader.setOffset(columnStart);
            readPictureColumn(i, pixels);
            int columnEnd = reader.getOffset();
            totalCount += columnEnd - columnStart;
        }

        if (totalCount > lumpSize) {
            throw new WadException("Read more picture data (%d) than expected (%d)", totalCount, lumpSize);
        }

        Graphic graphic = new Graphic(width, height, xOffset, yOffset, pixels);
        Graphic existing = currentDir.graphics.get(lumpName);
        if (existing == null) {
            currentDir.graphics.put(lumpName, graphic);
            return;
        }
        if (!Arrays.deepEquals(graphic.pixels(), existing.pixels())) {
            throw new WadException("Duplicate graphic %s", lumpName);
        }
        System.out.format("Skipping graphic %s\n", lumpName);
    }

    private void readPictureColumn(int col, Byte[][] data) {
        while (true) {
            int rowStart = reader.readInt8();
            if (rowStart == 0xFF) {
                break;
            }
            int length = reader.readInt8();
            reader.readInt8(); // padding
            for (int i = 0; i < length; i++) {
                data[rowStart + i][col] = (byte) reader.readInt8();
            }
            reader.readInt8(); // padding
        }
    }

    private void readFlat(String lumpName, int lumpSize) throws WadException {
        if (lumpSize != 64 * 64) {
            throw new WadException("Unexpected floor size: %d", lumpSize);
        }
        var pixels = new byte[64][];
        for (int i = 0; i < 64; i++) {
            pixels[i] = reader.readBytes(64);
        }

        byte[][] existing = currentDir.flats.get(lumpName);
        if (existing == null) {
            currentDir.flats.put(lumpName, pixels);
            return;
        }
        if (!Arrays.deepEquals(pixels, existing)) {
            throw new WadException("Duplicate flat %s", lumpName);
        }
        System.out.format("Skipping flat %s\n", lumpName);
    }

    private static boolean isMapName(String lumpName) {
        if (lumpName.length() == 4
                && lumpName.charAt(0) == 'E'
                && lumpName.charAt(2) == 'M'
                && Character.isDigit(lumpName.charAt(1))
                && Character.isDigit(lumpName.charAt(3))
        ) {
            return true;
        }
        if (lumpName.startsWith("MAP")) {
            return true;
        }
        return false;
    }
}
