package cern.c2mon.shared.common;


import static org.junit.Assert.assertTrue;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TestXMLFactory extends AbstractXMLTst {

    @Test
    public void testXMLFactory() throws ParserConfigurationException, DOMException, IllegalArgumentException, IllegalAccessException, InstantiationException, TransformerException {
        DOMFactory factory = new DOMFactory();
        Document document = factory.createDocument();
        TestPojo pojo = new TestPojo();
        Element element = factory.generateSimpleElement(document, pojo, "id", "name");        
        document.appendChild(element);
        assertTrue(element.getChildNodes().getLength() > 0);
        printDocument(document);
    }
}
