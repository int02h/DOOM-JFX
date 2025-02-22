package com.dpforge.doom.wad;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtil {
    private FileUtil() {
    }

    public static void deleteDirectory(File output) throws IOException {
        if (!output.exists()) {
            return;
        }
        if (!output.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + output);
        }
        Files.walkFileTree(output.toPath(), new SimpleFileVisitor<>() {
            @NotNull
            @Override
            public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @NotNull
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void ensureParentExist(File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot create parent directories: " + parent);
        }
    }
}
