package com.nexusteam.framework.xml.string.model;

import java.util.Objects;

public final class ExtractedString {
    private final String value;
    private final String attributeName;
    private final String generatedName;
    private final String layoutPath;

    public ExtractedString(String value, String attributeName, String generatedName, String layoutPath) {
        this.value = Objects.requireNonNull(value);
        this.attributeName = Objects.requireNonNull(attributeName);
        this.generatedName = Objects.requireNonNull(generatedName);
        this.layoutPath = Objects.requireNonNull(layoutPath);
    }
    public String getValue() { return value; }
    public String getAttributeName() { return attributeName; }
    public String getGeneratedName() { return generatedName; }
    public String getLayoutPath() { return layoutPath; }
}
