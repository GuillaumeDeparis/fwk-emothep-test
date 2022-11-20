package com.emothep.model;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TestDataTest {
    @Test
    void getServiceName() throws IOException, ParserConfigurationException, SAXException {
        TestData td = new TestData("C:\\Users\\guillaume.deparis.E-MOTHEP\\IdeaProjects\\fwk-emothep-test\\src\\test\\resources\\emothepE2ETest.xml");
        assertEquals("inputservice:name", td.getServiceIn());
    }
}