package com.emothep.model;

import com.emothep.serviceMock.MockObject;
import com.emothep.utils.EmothepManageFileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class TestData {
    private String name;
    private String serviceIn;
    private ArrayList<MockObject> mockObjectArrayListIn;
    private String serviceOut;
    private ArrayList<MockObject> mockObjectArrayListOut;

    public TestData(String filename) throws IOException, ParserConfigurationException, SAXException {
        Element element = new EmothepManageFileUtils().getDocumentFromFile(filename).getDocumentElement();
        setName(element.getAttribute("name"));
        setServiceIn(element.getElementsByTagName("EmothepTestStart"));

    }


    private void setName(String name){
        this.name = name;
    }
    private void setServiceIn(NodeList nodeList){
        for(int i=0; i != nodeList.getLength(); i++) {
            serviceIn = nodeList.item(i).getAttributes().getNamedItem("serviceName").getNodeValue();
            break;
        }
    }

    public String getName(){
        return name;
    }

    public String getServiceIn(){
        return serviceIn;
    }
}
