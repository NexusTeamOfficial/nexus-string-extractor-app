package com.nexusteam.framework.xml.string.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FileUtils {
    private FileUtils() {}
    public static void createBackup(Path file) throws IOException {
        if (file == null) throw new IllegalArgumentException("file must not be null");
        if (!Files.isRegularFile(file)) throw new IOException("Cannot backup non-existent file: " + file);
        Path backup = file.resolveSibling(file.getFileName().toString() + ".bak");
        Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
    }
    public static void ensureParentDirectories(Path path) throws IOException {
        if (path == null) throw new IllegalArgumentException("path must not be null");
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);
    }
}
