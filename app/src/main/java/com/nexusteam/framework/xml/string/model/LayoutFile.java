package com.nexusteam.framework.xml.string.model;

import org.w3c.dom.Document;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LayoutFile {
    private final Path path;
    private final Document document;
    private final List<ExtractedString> extractedStrings;

    public LayoutFile(Path path, Document document, List<ExtractedString> extractedStrings) {
        this.path = Objects.requireNonNull(path);
        this.document = Objects.requireNonNull(document);
        this.extractedStrings = Collections.unmodifiableList(Objects.requireNonNull(extractedStrings));
    }
    public Path getPath() { return path; }
    public Document getDocument() { return document; }
    public List<ExtractedString> getExtractedStrings() { return extractedStrings; }
    public boolean hasExtractions() { return !extractedStrings.isEmpty(); }
}
