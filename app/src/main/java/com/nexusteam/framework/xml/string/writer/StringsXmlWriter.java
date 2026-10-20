package com.nexusteam.framework.xml.string.writer;

import com.nexusteam.framework.xml.string.exception.ExtractionException;
import com.nexusteam.framework.xml.string.model.ExtractedString;
import com.nexusteam.framework.xml.string.util.FileUtils;
import com.nexusteam.framework.xml.string.util.Logger;
import com.nexusteam.framework.xml.string.util.XmlEscaper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class StringsXmlWriter {

    private static final String RESOURCES_TAG = "resources";

    public Set<String> loadExistingNames(Path stringsXml) throws ExtractionException {
        Set<String> names = new HashSet<>();
        if (!Files.isRegularFile(stringsXml)) return names;
        try {
            Document doc = parseXml(stringsXml);
            NodeList stringNodes = doc.getElementsByTagName("string");
            for (int i = 0; i < stringNodes.getLength(); i++) {
                Node node = stringNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String name = ((Element) node).getAttribute("name");
                    if (name != null && !name.isEmpty()) names.add(name);
                }
            }
            Logger.fine("Loaded " + names.size() + " existing string names from " + stringsXml);
        } catch (Exception e) {
            throw new ExtractionException("Failed to read existing strings.xml: " + stringsXml, e);
        }
        return names;
    }

    public int appendStrings(Path stringsXml, List<ExtractedString> extracted,
                             boolean dryRun, boolean backup) throws ExtractionException {
        if (extracted == null || extracted.isEmpty()) return 0;
        try {
            Document doc;
            boolean created = false;
            if (Files.isRegularFile(stringsXml)) {
                doc = parseXml(stringsXml);
            } else {
                doc = createEmptyResourcesDocument();
                created = true;
            }
            Element resources = doc.getDocumentElement();
            if (resources == null || !RESOURCES_TAG.equals(resources.getTagName())) {
                throw new ExtractionException("Root element of strings.xml is not <resources>");
            }
            for (ExtractedString es : extracted) {
                Element stringEl = doc.createElement("string");
                stringEl.setAttribute("name", es.getGeneratedName());
                stringEl.setTextContent(XmlEscaper.escape(es.getValue()));
                resources.appendChild(doc.createTextNode("\n    "));
                resources.appendChild(stringEl);
            }
            resources.appendChild(doc.createTextNode("\n"));
            if (dryRun) {
                Logger.info("[dry-run] Would write " + extracted.size() + " string(s) to " + stringsXml
                        + (created ? " (new file)" : ""));
                return extracted.size();
            }
            if (backup && Files.isRegularFile(stringsXml)) {
                FileUtils.createBackup(stringsXml);
                Logger.fine("Created backup: " + stringsXml + ".bak");
            }
            FileUtils.ensureParentDirectories(stringsXml);
            writeDocument(doc, stringsXml);
            Logger.info("Wrote " + extracted.size() + " string(s) to " + stringsXml
                    + (created ? " (created)" : ""));
            return extracted.size();
        } catch (ExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new ExtractionException("Failed to update strings.xml: " + stringsXml, e);
        }
    }

    private Document parseXml(Path path) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(false);
        trySetFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        trySetFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
        trySetFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        try { factory.setExpandEntityReferences(false); } catch (Exception ignored) {}
        return factory.newDocumentBuilder().parse(path.toFile());
    }

    private static void trySetFeature(DocumentBuilderFactory factory, String name, boolean value) {
        try { factory.setFeature(name, value); } catch (Exception ignored) {}
    }

    private Document createEmptyResourcesDocument() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element resources = doc.createElement(RESOURCES_TAG);
        doc.appendChild(resources);
        doc.setXmlStandalone(true);
        return doc;
    }

    private void writeDocument(Document doc, Path path) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        String content = sw.toString().replace("\r\n", "\n");
        if (!content.endsWith("\n")) content = content + "\n";
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }
}
