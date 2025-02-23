package com.dpforge.doom.wad;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final GraphicRenderer graphicsRenderer = new GraphicRenderer();
    private static final SoundRenderer soundRenderer = new SoundRenderer();
    private static final Map<String, File> flats = new HashMap<>();

    public static void main(String[] args) throws IOException, WadException {
        File wadFile = new File("../doom-jfx/__doom2.wad");
        WadFile wad = new WadFileReader().read(wadFile);

        graphicsRenderer.setPalette(wad.palettes[0]);

        var output = new File("output");
        FileUtil.deleteDirectory(output);

        outputDirectory(output, wad.directory);
        renderMap(output, wad.maps.get("MAP01"));
    }

    private static void outputDirectory(File output, WadDirectory directory) throws IOException {
        File dir = new File(output, directory.name.isEmpty() ? "lumps" : directory.name);

        for (var entry : directory.graphics.entrySet()) {
            var file = new File(dir, String.format("graphics/%s.png", entry.getKey()));
            FileUtil.ensureParentExist(file);
            ImageIO.write(graphicsRenderer.render(entry.getValue()), "PNG", file);
        }

        for (var entry : directory.flats.entrySet()) {
            var file = new File(dir, String.format("flat/%s.png", entry.getKey()));
            FileUtil.ensureParentExist(file);
            ImageIO.write(graphicsRenderer.render(entry.getValue()), "PNG", file);
            flats.put(entry.getKey(), file);
        }

        for (var entry : directory.sounds.entrySet()) {
            var file = new File(dir, String.format("sound/%s.wav", entry.getKey()));
            FileUtil.ensureParentExist(file);
            soundRenderer.renderWav(file, entry.getValue());
        }

        for (var d : directory.directories) {
            outputDirectory(dir, d);
        }
    }

    private static void renderMap(File output, WadMap map) throws IOException {
        File file = new File(output, String.format("%s.png", map.name));
        MapRenderer mapRenderer = new MapRenderer(map, flats);
        ImageIO.write(mapRenderer.render(), "PNG", file);
    }
}