package com.nexusteam.framework.xml.string;

import com.nexusteam.framework.xml.string.core.ExtractionEngine;
import com.nexusteam.framework.xml.string.core.TransformationResult;
import com.nexusteam.framework.xml.string.exception.ExtractionException;
import com.nexusteam.framework.xml.string.model.ExtractionConfig;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Public library API for NexusStringExtractor.
 */
public final class StringExtractor {

    private final ExtractionConfig config;

    private StringExtractor(ExtractionConfig config) {
        this.config = config;
    }

    public TransformationResult extract() throws ExtractionException {
        return new ExtractionEngine().run(config);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Path resDir;
        private Path stringsXml;
        private boolean dryRun;
        private boolean backup;
        private boolean verbose;
        private String prefix;

        public Builder resDir(Path resDir) {
            this.resDir = resDir;
            return this;
        }

        public Builder resDir(File resDir) {
            this.resDir = resDir != null ? resDir.toPath() : null;
            return this;
        }

        public Builder resDir(String resDir) {
            this.resDir = resDir != null ? Paths.get(resDir) : null;
            return this;
        }

        public Builder stringsXml(Path stringsXml) {
            this.stringsXml = stringsXml;
            return this;
        }

        public Builder stringsXml(File stringsXml) {
            this.stringsXml = stringsXml != null ? stringsXml.toPath() : null;
            return this;
        }

        public Builder stringsXml(String stringsXml) {
            this.stringsXml = stringsXml != null ? Paths.get(stringsXml) : null;
            return this;
        }

        public Builder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public Builder backup(boolean backup) {
            this.backup = backup;
            return this;
        }

        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public StringExtractor build() {
            Objects.requireNonNull(resDir, "resDir is required");
            Path resolvedStrings = stringsXml != null
                    ? stringsXml
                    : resDir.resolve("values").resolve("strings.xml");

            ExtractionConfig config = ExtractionConfig.builder()
                    .resDir(resDir.toAbsolutePath().normalize())
                    .stringsXml(resolvedStrings.toAbsolutePath().normalize())
                    .dryRun(dryRun)
                    .backup(backup)
                    .verbose(verbose)
                    .prefix(prefix)
                    .build();

            return new StringExtractor(config);
        }
    }
}
