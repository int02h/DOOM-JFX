package com.dpforge.doom.wad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WadDirectory {

    public final String name;
    public final List<WadDirectory> directories = new ArrayList<>();
    public final Map<String, Graphic> graphics = new HashMap<>();
    public final Map<String, byte[][]> flats = new HashMap<>();
    public final Map<String, byte[]> sounds = new HashMap<>();

    public WadDirectory(String name) {
        this.name = name;
    }

    public void addDirectory(WadDirectory dir) {
        directories.add(dir);
    }
}
