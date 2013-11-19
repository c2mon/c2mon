package cern.c2mon.shared.daq.config;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class AbstractXMLTst {

    public AbstractXMLTst() {
        super();
    }

    public void printDocument(Document document) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        String xmlString = getDocumentString(document);
        System.out.println(xmlString);
    }

    public String getDocumentString(Document document) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    
        //initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(document);
        transformer.transform(source, result);
    
        String xmlString = result.getWriter().toString();
        return xmlString;
    }
    
    public void validateDocument(Document document, String xsdURL) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
//        Element element = (Element) document.getElementsByTagName("ConfigurationChangeEvent").item(0);
//        element.setAttribute("xmlns", "http://timweb.cern.ch/schemas/c2mon-daq/Configuration");
        // create a SchemaFactory capable of understanding WXS schemas
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//        factory.
        URL location = new URL(xsdURL);
        // load a WXS schema, represented by a Schema instance
        Schema schema = factory.newSchema(location);
        // create a Validator instance, which can be used to validate an instance document
        Validator validator = schema.newValidator();
//        validator.setErrorHandler(new ErrorHandler() {
//            
//            @Override
//            public void warning(final SAXParseException exception) throws SAXException {
//                exception.printStackTrace();
//            }
//            
//            @Override
//            public void fatalError(final SAXParseException exception) throws SAXException {
//                exception.printStackTrace();
//            }
//            
//            @Override
//            public void error(final SAXParseException exception) throws SAXException {
//                exception.printStackTrace();
//            }
//        });
        // validate the xml
        String xmlString = getDocumentString(document);
        validator.validate(new StreamSource(new StringReader(xmlString)));
    }
}
