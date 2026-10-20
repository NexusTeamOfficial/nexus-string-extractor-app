package com.nexusteam.framework.xml.string.generator;

public final class NameGenerator {
    private static final char START = 'a';
    private static final int BASE = 26;
    private int counter;
    private final String prefix;

    public NameGenerator() { this(0, null); }
    public NameGenerator(int startIndex) { this(startIndex, null); }
    public NameGenerator(int startIndex, String prefix) {
        if (startIndex < 0) throw new IllegalArgumentException("startIndex must be >= 0");
        this.counter = startIndex;
        this.prefix = (prefix == null || prefix.isEmpty()) ? "" : prefix;
    }
    public String next() {
        String name = toName(counter);
        counter++;
        return prefix + name;
    }
    public String peek() { return prefix + toName(counter); }
    public int getCounter() { return counter; }
    public static String toName(int index) {
        if (index < 0) throw new IllegalArgumentException("index must be >= 0");
        StringBuilder sb = new StringBuilder();
        int n = index + 1;
        while (n > 0) {
            n--;
            sb.append((char) (START + (n % BASE)));
            n /= BASE;
        }
        return sb.reverse().toString();
    }
}
