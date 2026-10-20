package com.nexusteam.framework.xml.string.core;

import com.nexusteam.framework.xml.string.exception.ExtractionException;
import com.nexusteam.framework.xml.string.generator.NameCollisionResolver;
import com.nexusteam.framework.xml.string.generator.NameGenerator;
import com.nexusteam.framework.xml.string.model.ExtractedString;
import com.nexusteam.framework.xml.string.model.ExtractionConfig;
import com.nexusteam.framework.xml.string.model.LayoutFile;
import com.nexusteam.framework.xml.string.parser.LayoutXmlParser;
import com.nexusteam.framework.xml.string.util.Logger;
import com.nexusteam.framework.xml.string.writer.LayoutXmlWriter;
import com.nexusteam.framework.xml.string.writer.StringsXmlWriter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ExtractionEngine {

    private final LayoutScanner scanner = new LayoutScanner();
    private final StringsXmlWriter stringsWriter = new StringsXmlWriter();
    private final LayoutXmlWriter layoutWriter = new LayoutXmlWriter();

    public TransformationResult run(ExtractionConfig config) throws ExtractionException {
        Logger.setVerbose(config.isVerbose());
        Logger.info("Starting NexusStringExtractor");
        Logger.info("  resDir     : " + config.getResDir());
        Logger.info("  stringsXml : " + config.getStringsXml());
        Logger.info("  dryRun     : " + config.isDryRun());
        Logger.info("  backup     : " + config.isBackup());
        config.getPrefix().ifPresent(p -> Logger.info("  prefix     : " + p));

        List<Path> layoutPaths;
        try {
            layoutPaths = scanner.scan(config.getResDir());
        } catch (Exception e) {
            throw new ExtractionException("Failed to scan layout directory", e);
        }

        if (layoutPaths.isEmpty()) {
            Logger.info("No layout XML files found. Nothing to do.");
            return new TransformationResult(0, 0, 0, List.of(), config.isDryRun());
        }

        Set<String> existingNames = stringsWriter.loadExistingNames(config.getStringsXml());
        String prefix = config.getPrefix().orElse(null);
        NameGenerator generator = new NameGenerator(0, prefix);
        NameCollisionResolver resolver = new NameCollisionResolver(existingNames, generator);
        LayoutXmlParser parser = new LayoutXmlParser(resolver);

        List<LayoutFile> modifiedLayouts = new ArrayList<>();
        List<ExtractedString> allExtracted = new ArrayList<>();

        for (Path layoutPath : layoutPaths) {
            try {
                LayoutFile lf = parser.parse(layoutPath);
                if (lf.hasExtractions()) {
                    modifiedLayouts.add(lf);
                    allExtracted.addAll(lf.getExtractedStrings());
                }
            } catch (Exception e) {
                Logger.warning("Skipping layout (parse failed): " + layoutPath
                        + " — " + e.getMessage());
            }
        }

        Logger.info("Extracted " + allExtracted.size() + " hardcoded string(s) from "
                + modifiedLayouts.size() + " layout(s)");

        if (!allExtracted.isEmpty()) {
            stringsWriter.appendStrings(
                    config.getStringsXml(), allExtracted, config.isDryRun(), config.isBackup());
        }

        int layoutsWritten = 0;
        for (LayoutFile lf : modifiedLayouts) {
            if (layoutWriter.write(lf, config.isDryRun(), config.isBackup())) {
                layoutsWritten++;
            }
        }

        TransformationResult result = new TransformationResult(
                layoutPaths.size(), layoutsWritten, allExtracted.size(), allExtracted, config.isDryRun());
        Logger.info("Done. " + result);
        return result;
    }
}
