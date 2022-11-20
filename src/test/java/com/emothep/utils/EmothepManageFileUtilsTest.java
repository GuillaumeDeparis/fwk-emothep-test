package com.emothep.utils;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class EmothepManageFileUtilsTest {

    @Test
    void getDocumentFromFile() throws IOException, ParserConfigurationException, SAXException {
        Document doc = new EmothepManageFileUtils().getDocumentFromFile("C:\\Users\\guillaume.deparis.E-MOTHEP\\IdeaProjects\\fwk-emothep-test\\src\\test\\resources\\emothepE2ETest.xml");
        assertEquals( "test", doc.getDocumentElement().getAttribute("name"));
    }
}