package com.nexusteam.framework.xml.string.writer;

import com.nexusteam.framework.xml.string.exception.ExtractionException;
import com.nexusteam.framework.xml.string.model.LayoutFile;
import com.nexusteam.framework.xml.string.util.FileUtils;
import com.nexusteam.framework.xml.string.util.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LayoutXmlWriter {

    public boolean write(LayoutFile layoutFile, boolean dryRun, boolean backup) throws ExtractionException {
        if (!layoutFile.hasExtractions()) return false;
        Path path = layoutFile.getPath();
        Document doc = layoutFile.getDocument();
        try {
            if (dryRun) {
                Logger.info("[dry-run] Would update layout: " + path
                        + " (" + layoutFile.getExtractedStrings().size() + " replacement(s))");
                return true;
            }
            if (backup && Files.isRegularFile(path)) {
                FileUtils.createBackup(path);
                Logger.fine("Created backup: " + path + ".bak");
            }
            Files.write(path, serialize(doc).getBytes(StandardCharsets.UTF_8));
            Logger.info("Updated layout: " + path
                    + " (" + layoutFile.getExtractedStrings().size() + " replacement(s))");
            return true;
        } catch (Exception e) {
            throw new ExtractionException("Failed to write layout: " + path, e);
        }
    }

    private String serialize(Document doc) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        if (doc.getXmlStandalone()) {
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        }
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        String content = sw.toString().replace("\r\n", "\n");
        if (!content.endsWith("\n")) content = content + "\n";
        return content;
    }
}
