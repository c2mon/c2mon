package cern.c2mon.util.parser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Simple wrapper class for a DOM Parser.
 * The SimpleXMLParser internally uses Xerces.
 * @author J. Stowisek
 * @version $Revision: 1.2 $ ($Date: 2005/07/05 16:15:27 $ - $State: Exp $)
 */

public final class SimpleXMLParser implements XmlParser {
  /**
   * Local JAXP Document builder for parsing XML content.
   */
  private DocumentBuilder builder;

  public static final String ENCODING = "UTF-8";

  /**
   * Create an instance of the SimpleXMLParser.
   * @throws ParserConfigurationException if the object cannot create an
   * instance of the Xerces parser it uses internally.
   */
  public SimpleXMLParser() throws ParserConfigurationException {
    // throws ParserConfigurationException if creation fails
    builder = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder();
  }

  /**
   * Parse an XML document contained in a String.
   * @param xml String representation of the XML document to be parsed
   * @return the parsed XML Document - never NULL
   */
  @Override
  public synchronized Document parse(final String xml) throws ParserException {
    Document doc;
    ByteArrayInputStream in;
    try {
      in = new ByteArrayInputStream(xml.getBytes(ENCODING));
      doc = builder.parse(new InputSource(in));
      in.close();
    } catch (UnsupportedEncodingException e1) {
      /* Should NEVER happen as the encoding is hard-coded above, but OK */
      throw new ParserException("Unsupported encoding: "+ ENCODING, e1);
    } catch (IOException e2) {
      throw new ParserException("Error creating InputStream for xml string", e2);
    } catch (SAXException saxe) {
      throw new ParserException("Error parsing XML document", saxe);
    } finally {
      builder.reset();
    }
    if (doc == null) {
      throw new ParserException("Null document obtained during document parsing.");
    }   
    return doc;
  }

}
