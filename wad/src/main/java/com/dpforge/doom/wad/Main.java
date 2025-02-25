package com.dpforge.doom.wad;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final GraphicRenderer graphicsRenderer = new GraphicRenderer();
    private static final SoundRenderer soundRenderer = new SoundRenderer();
    private static final Map<String, BufferedImage> graphics = new HashMap<>();

    private static final boolean WRITE_RESOURCE_TO_FILE = false;

    public static void main(String[] args) throws IOException, WadException {
        File wadFile = new File("../doom-jfx/doom2.wad");
        WadFile wad = new WadFileReader().read(wadFile);

        graphicsRenderer.setPalette(wad.palettes[0]);

        var output = new File("output");
        FileUtil.deleteDirectory(output);

        outputDirectory(output, wad.directory);
        // textures rely on graphics from the directory
        outputTextures(output, wad);

        WadMap map = wad.maps.get("MAP01");

        var renderer = new GameRenderer(map, graphics);
        for (Thing t : map.things) {
            if (t.type() == ThingType.PLAYER_1_START) {
                renderer.setCamera(t.x(), t.y(), 90f);
                break;
            }
        }
        renderer.render();
        File frameFile = new File(output, "frame.png");
        FileUtil.ensureParentExist(frameFile);
        ImageIO.write(renderer.image, "PNG", frameFile);

        //renderMap(output, map);
    }

    private static void outputDirectory(File output, WadDirectory directory) throws IOException {
        File dir = new File(output, directory.name.isEmpty() ? "lumps" : directory.name);

        for (var entry : directory.graphics.entrySet()) {
            var file = new File(dir, String.format("graphics/%s.png", entry.getKey()));
            BufferedImage render = graphicsRenderer.render(entry.getValue());
            writeImage(render, file);
            graphics.put(entry.getKey(), render);
        }

        for (var entry : directory.flats.entrySet()) {
            var file = new File(dir, String.format("flat/%s.png", entry.getKey()));
            BufferedImage render = graphicsRenderer.render(entry.getValue());
            writeImage(render, file);
            graphics.put(entry.getKey(), render);
        }

        if (WRITE_RESOURCE_TO_FILE) {
            for (var entry : directory.sounds.entrySet()) {
                var file = new File(dir, String.format("sound/%s.wav", entry.getKey()));
                FileUtil.ensureParentExist(file);
                soundRenderer.renderWav(file, entry.getValue());
            }
        }

        for (var d : directory.directories) {
            outputDirectory(dir, d);
        }
    }

    private static void outputTextures(File output, WadFile wad) throws IOException {
        File dir = new File(output, "textures");

        for (var entry : wad.textures.entrySet()) {
            var file = new File(dir, String.format("%s.png", entry.getKey()));
            BufferedImage render = graphicsRenderer.render(entry.getValue(), wad, graphics);
            writeImage(render, file);
            graphics.put(entry.getKey(), render);
        }
    }

    private static void renderMap(File output, WadMap map) throws IOException {
        File file = new File(output, String.format("%s.png", map.name));
        FileUtil.ensureParentExist(file);
        try (MapRenderer mapRenderer = new MapRenderer(map)) {
            mapRenderer.renderFlats(graphics);
            mapRenderer.renderThings();
            mapRenderer.renderBSP();
            writeImage(mapRenderer.getImage(), file);
        }
    }

    private static void writeImage(BufferedImage image, File file) throws IOException {
        if (WRITE_RESOURCE_TO_FILE) {
            FileUtil.ensureParentExist(file);
            ImageIO.write(image, "PNG", file);
        }
    }
}