package com.nexusteam.framework.xml.string.core;

import com.nexusteam.framework.xml.string.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LayoutScanner {

    public List<Path> scan(Path resDir) throws IOException {
        Path layoutDir = resDir.resolve("layout");
        if (!Files.isDirectory(layoutDir)) {
            Logger.warning("Layout directory does not exist: " + layoutDir);
            return List.of();
        }
        List<Path> result;
        try (Stream<Path> walk = Files.walk(layoutDir)) {
            result = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xml"))
                    .sorted()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        Logger.fine("Found " + result.size() + " layout XML file(s) under " + layoutDir);
        return result;
    }
}
