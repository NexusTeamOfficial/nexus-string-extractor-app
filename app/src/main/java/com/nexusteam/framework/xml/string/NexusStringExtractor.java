package com.nexusteam.framework.xml.string;

import com.nexusteam.framework.xml.string.core.TransformationResult;
import com.nexusteam.framework.xml.string.exception.ExtractionException;

/**
 * Convenience entry points that mirror simple usage.
 * Prefer {@link StringExtractor} for new code.
 */
public final class NexusStringExtractor {

    private NexusStringExtractor() {
    }

    public static TransformationResult extract(String resDir) throws ExtractionException {
        return StringExtractor.builder()
                .resDir(resDir)
                .build()
                .extract();
    }

    public static TransformationResult extractWithBackup(String resDir) throws ExtractionException {
        return StringExtractor.builder()
                .resDir(resDir)
                .backup(true)
                .build()
                .extract();
    }

    public static TransformationResult dryRun(String resDir) throws ExtractionException {
        return StringExtractor.builder()
                .resDir(resDir)
                .dryRun(true)
                .verbose(true)
                .build()
                .extract();
    }
}
