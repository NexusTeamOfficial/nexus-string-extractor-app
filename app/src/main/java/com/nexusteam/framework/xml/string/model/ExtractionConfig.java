package com.nexusteam.framework.xml.string.model;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class ExtractionConfig {
    private final Path resDir;
    private final Path stringsXml;
    private final boolean dryRun;
    private final boolean backup;
    private final boolean verbose;
    private final String prefix;

    private ExtractionConfig(Builder b) {
        this.resDir = b.resDir;
        this.stringsXml = b.stringsXml;
        this.dryRun = b.dryRun;
        this.backup = b.backup;
        this.verbose = b.verbose;
        this.prefix = b.prefix;
    }
    public Path getResDir() { return resDir; }
    public Path getStringsXml() { return stringsXml; }
    public boolean isDryRun() { return dryRun; }
    public boolean isBackup() { return backup; }
    public boolean isVerbose() { return verbose; }
    public Optional<String> getPrefix() { return Optional.ofNullable(prefix); }
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Path resDir;
        private Path stringsXml;
        private boolean dryRun;
        private boolean backup;
        private boolean verbose;
        private String prefix;
        public Builder resDir(Path v) { this.resDir = v; return this; }
        public Builder stringsXml(Path v) { this.stringsXml = v; return this; }
        public Builder dryRun(boolean v) { this.dryRun = v; return this; }
        public Builder backup(boolean v) { this.backup = v; return this; }
        public Builder verbose(boolean v) { this.verbose = v; return this; }
        public Builder prefix(String v) { this.prefix = v; return this; }
        public ExtractionConfig build() {
            Objects.requireNonNull(resDir, "resDir");
            Objects.requireNonNull(stringsXml, "stringsXml");
            return new ExtractionConfig(this);
        }
    }
}
