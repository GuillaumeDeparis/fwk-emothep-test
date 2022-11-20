package com.emothep.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

public class EmothepManageFileUtils {


    public Document getDocumentFromFile(String filename) throws IOException, ParserConfigurationException, SAXException {
        filename = filename.replace('\\', '/');
        return getDocumentFromInputStream(new FileInputStream(filename));
    }

    public Document getDocumentFromInputStream(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        document.normalize();

        return document;
    }
}
