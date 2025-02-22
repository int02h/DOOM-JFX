package com.dpforge.doom.wad;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Main {
    private static final GraphicRenderer graphicsRenderer = new GraphicRenderer();
    private static final SoundRenderer soundRenderer = new SoundRenderer();

    public static void main(String[] args) throws IOException, WadException {
        File wadFile = new File("../doom-jfx/doom.wad");
        WadFile wad = new WadFileReader().read(wadFile);

        graphicsRenderer.setPalette(wad.palettes[0]);

        var output = new File("output");
        FileUtil.deleteDirectory(output);

        outputDirectory(output, wad.directory);
    }

    private static void outputDirectory(File output, WadDirectory directory) throws IOException {
        File dir = directory.name.isEmpty() ? output : new File(output, directory.name);

        for (var entry : directory.graphics.entrySet()) {
            var file = new File(dir, String.format("graphics/%s.png", entry.getKey()));
            FileUtil.ensureParentExist(file);
            ImageIO.write(graphicsRenderer.render(entry.getValue()), "PNG", file);
        }

        for (var entry : directory.flats.entrySet()) {
            var file = new File(dir, String.format("flat/%s.png", entry.getKey()));
            FileUtil.ensureParentExist(file);
            ImageIO.write(graphicsRenderer.render(entry.getValue()), "PNG", file);
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
}