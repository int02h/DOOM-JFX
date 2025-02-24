package com.dpforge.doom.wad;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class WadFile {

    WadType type;
    int lumpNumber;
    int infoTableOffset;

    public Color[][] palettes;
    public byte[][] colorMaps;
    public EndDoom endDoom;
    public String[] pnames;

    public final WadDirectory directory = new WadDirectory("");
    public final Map<String, WadMap> maps = new HashMap<>();
    public final Map<String, Texture> textures = new HashMap<>();

}
