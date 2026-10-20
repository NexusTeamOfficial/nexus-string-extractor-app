package com.nexusteam.framework.xml.string.core;

import com.nexusteam.framework.xml.string.model.ExtractedString;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class TransformationResult {

    private final int layoutsScanned;
    private final int layoutsModified;
    private final int stringsExtracted;
    private final List<ExtractedString> allExtracted;
    private final boolean dryRun;

    public TransformationResult(int layoutsScanned, int layoutsModified, int stringsExtracted,
                                List<ExtractedString> allExtracted, boolean dryRun) {
        this.layoutsScanned = layoutsScanned;
        this.layoutsModified = layoutsModified;
        this.stringsExtracted = stringsExtracted;
        this.allExtracted = Collections.unmodifiableList(Objects.requireNonNull(allExtracted));
        this.dryRun = dryRun;
    }

    public int getLayoutsScanned() { return layoutsScanned; }
    public int getLayoutsModified() { return layoutsModified; }
    public int getStringsExtracted() { return stringsExtracted; }
    public List<ExtractedString> getAllExtracted() { return allExtracted; }
    public boolean isDryRun() { return dryRun; }

    @Override
    public String toString() {
        return "TransformationResult{scanned=" + layoutsScanned + ", modified=" + layoutsModified
                + ", extracted=" + stringsExtracted + ", dryRun=" + dryRun + "}";
    }
}
