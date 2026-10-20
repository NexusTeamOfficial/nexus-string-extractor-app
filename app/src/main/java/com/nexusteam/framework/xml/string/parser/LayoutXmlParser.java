package com.nexusteam.framework.xml.string.parser;

import com.nexusteam.framework.xml.string.exception.ParseException;
import com.nexusteam.framework.xml.string.generator.NameCollisionResolver;
import com.nexusteam.framework.xml.string.model.ExtractedString;
import com.nexusteam.framework.xml.string.model.LayoutFile;
import com.nexusteam.framework.xml.string.util.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class LayoutXmlParser {

    private final NameCollisionResolver nameResolver;

    public LayoutXmlParser(NameCollisionResolver nameResolver) {
        this.nameResolver = nameResolver;
    }

    public LayoutFile parse(Path layoutPath) throws ParseException {
        Document document = loadDocument(layoutPath);
        List<ExtractedString> extracted = new ArrayList<>();
        Element root = document.getDocumentElement();
        if (root != null) {
            walk(root, layoutPath.toString(), extracted);
        }
        return new LayoutFile(layoutPath, document, extracted);
    }

    private Document loadDocument(Path path) throws ParseException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setIgnoringComments(false);
            factory.setIgnoringElementContentWhitespace(false);
            trySetFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
            trySetFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
            trySetFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
            try {
                factory.setExpandEntityReferences(false);
            } catch (Exception ignored) {
            }

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(path.toFile());
            if (doc.getDocumentElement() != null) {
                doc.getDocumentElement().normalize();
            }
            return doc;
        } catch (Exception e) {
            throw new ParseException("Failed to parse layout: " + path, e);
        }
    }

    private static void trySetFeature(DocumentBuilderFactory factory, String name, boolean value) {
        try {
            factory.setFeature(name, value);
        } catch (Exception ignored) {
        }
    }

    private void walk(Element element, String layoutPath, List<ExtractedString> out) {
        NamedNodeMap attrs = element.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Node node = attrs.item(i);
                if (!(node instanceof Attr)) continue;
                Attr attr = (Attr) node;
                String ns = attr.getNamespaceURI();
                String local = attr.getLocalName();
                if (local == null) {
                    local = attr.getName();
                    if (local.contains(":")) {
                        local = local.substring(local.indexOf(':') + 1);
                    }
                }
                if (!StringAttributeFilter.isTargetAttribute(ns, local)) continue;
                String value = attr.getValue();
                if (!StringAttributeFilter.isHardcodedLiteral(value)) continue;

                String generatedName = nameResolver.nextUnique();
                out.add(new ExtractedString(value, local, generatedName, layoutPath));
                attr.setValue("@string/" + generatedName);
                Logger.fine("Extracted [" + local + "]=\"" + value + "\" → @string/" + generatedName
                        + " in " + layoutPath);
            }
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                walk((Element) child, layoutPath, out);
            }
        }
    }
}
