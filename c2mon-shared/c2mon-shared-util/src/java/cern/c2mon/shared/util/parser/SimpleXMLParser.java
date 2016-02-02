/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.shared.util.parser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
      try {
        builder.reset();
      } catch (UnsupportedOperationException e) {
        try {
          builder = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e2) {
        }
      }
    }
    if (doc == null) {
      throw new ParserException("Null document obtained during document parsing.");
    }
    return doc;
  }

  /**
   * Takes a Node and parses it to a HashMap.
   * This method serves as helper method deserialize a map from a xml file.
   * </p>
   * Because of the representation of the map in the xml file type safety cant be ensured and the value type is always String.
   * The user of this map has to transform the type of the value by himself to the required type.
   * </p>
   * The need of this method is because the server uses a old self made xml parser for DAQ configurations.
   *
   * @param node the Node which is created because of the receiving of the xml node.
   * @return the map object represented by this node.
   */
  public static Map<String, String> domNodeToMap(Node node){
    Map<String, String> result = new HashMap<>();

    // check if the node is a map node.

    if(!node.getNodeName().equals("properties")){
      throw new IllegalArgumentException("Node does not hold the Information for creating a map");
    }

    // parse node and put the information in the map
    NodeList entryNodes = node.getChildNodes();

    for(int i = 0; i< entryNodes.getLength(); i++){
      Node entryNode = entryNodes.item(i);
      String key = entryNode.getAttributes().item(0).getNodeValue().toString();
      String value = entryNode.getFirstChild().getNodeValue();
      result.put(key, value);
    }

    return result;
  }


  /**
   * Takes a Map and parse it to an slf made xml string.
   * This method serves as helper method serialize a map to a xml file.
   * The need of this method is because the server uses a old self made xml parser for DAQ configurations.
   *
   * @param map the java object which needs to be parsed to an xml string.
   * @return The xml string representation of the given map
   */
  public static String mapToXMLString(Map<String, String> map){
    String spaces = "            ";
    String result = spaces + "<properties class=\""+ map.getClass() +"\">" ;

    for(Map.Entry<String, String> entry : map.entrySet()){
      result += "<entry key=\"" + entry.getKey() + "\">" + entry.getValue() + "</entry>";
    }

    result += "</properties>\n";

    return  result;

  }

}
