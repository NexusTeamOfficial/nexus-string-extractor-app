package com.nexusteam.framework.xml.string.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class StringAttributeFilter {
    public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    private static final Set<String> TARGET_LOCAL_NAMES;
    static {
        Set<String> names = new HashSet<>();
        names.add("text"); names.add("hint");
        names.add("contentDescription"); names.add("tooltipText");
        TARGET_LOCAL_NAMES = Collections.unmodifiableSet(names);
    }
    private StringAttributeFilter() {}
    public static Set<String> targetLocalNames() { return TARGET_LOCAL_NAMES; }
    public static boolean isTargetAttribute(String namespaceUri, String localName) {
        if (localName == null) return false;
        boolean nsOk = namespaceUri == null || namespaceUri.isEmpty() || ANDROID_NS.equals(namespaceUri);
        return nsOk && TARGET_LOCAL_NAMES.contains(localName);
    }
    public static boolean isHardcodedLiteral(String value) {
        if (value == null) return false;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return false;
        char first = trimmed.charAt(0);
        return first != '@' && first != '?';
    }
}
