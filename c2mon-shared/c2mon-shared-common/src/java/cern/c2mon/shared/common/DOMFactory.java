/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2010 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.common;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * DOMFactory to serialize simple POJOs to an XML format. All fields of the pojos
 * are subelements in the generated Node. The names are converted like described 
 * at the javaNameToXMLName method. Types supported for the fields (if it
 * is a primitive java type also the wrapper class is supported):
 * <ul>
 * <li>short</li>
 * <li>int</li>
 * <li>long</li>
 * <li>float</li>
 * <li>double</li>
 * <li>byte</li>
 * <li>char</li>
 * <li>boolean</li>
 * <li>String</li>
 * </ul>
 * 
 * Null values, not supported types and transient fields will be ignored in 
 * the resulting xml.
 * <br/>
 * Example:
 * <pre>public class TestPojo {
 *   private int numberTest = 0;
 *   private transient String transientString = "notInDocument"; // Will be ignored
 *   private String normalString = "sdjhsd";
 *   private String nullString = null; // Will be ignored
 *   private int notSetInt; // Will be assumed as 0 (standard java behavior)
 *   private Integer nullInt; // Will be ignored like null
 *   private List&lt;String&gt; list = new ArrayList&lt;String&gt;(); // ignored as not supported
 * }</pre>
 * Will result in:
 * <pre>&lt;TestPojo&gt;
 *  &lt;number-test&gt;0&lt;/number-test&gt;
 *  &lt;test-number&gt;238438&lt;/test-number&gt;
 *  &lt;normal-string&gt;sdjhsd&lt;/normal-string&gt;
 *  &lt;not-set-int&gt;0&lt;/not-set-int&gt;
 * &lt;/TestPojo&gt;</pre>
 * @author alang
 *
 */
public class DOMFactory extends SimpleTypeReflectionHandler {
    /**
     * The DocumentBuilderFactory
     */
    private DocumentBuilderFactory documentBuilderFactory;
    
    /**
     * The DocumentBuilder to create new DOM Documents
     */
    private DocumentBuilder documentBuilder;

    /**
     * The default namespace applied to all nodes.
     */
    private String defaultNamespace;
    
    /**
     * The transformer object to transform the DOM to a String
     */
    private Transformer transformer;
    
    /**
     * Creates a new DOMFactory with an empty default namespace.
     */
    public DOMFactory() {
        this("");
    }
    
    /**
     * Creates a new DOMFactory with the provided default namespace.
     * @param defaultNamespace The default namespace for this factory.
     */
    public DOMFactory(final String defaultNamespace) {
        this.setDefaultNamespace(defaultNamespace);
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
    }
    /**
     * Creates a new empty document.
     * @return The new document.
     * @throws ParserConfigurationException Might throw a ParserConfigurationException.
     */
    public Document createDocument() throws ParserConfigurationException {
        if (documentBuilder == null) {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }
        return documentBuilder.newDocument();
    }
    
    /**
     * Generates an xml element from this pojo. Translating the fields like described
     * in the class description.
     * @param document The document in which the nodes should be.
     * @param pojo The pojo to take the fields from.
     * @param attributes The fields which should be used as attributes and not
     * as elements.
     * @return The create element representing the provided pojo.
     * @throws ParserConfigurationException Might throw a ParserConfigurationException.
     * @throws IllegalAccessException Might throw a IllegalAccessException.
     * @throws InstantiationException Might throw a InstantiationException.
     */
    public Element generateSimpleElement(final Document document, final Object pojo, 
            final String ... attributes) throws ParserConfigurationException, 
            IllegalAccessException, InstantiationException {
        return generateSimpleElement(document, pojo, Arrays.asList(attributes));
    }
    
    /**
     * Generates an xml element from this pojo. Translating the fields like described
     * in the class description.
     * @param document The document in which the nodes should be.
     * @param pojo The pojo to take the fields from.
     * @param attributes The fields which should be used as attributes and not
     * as elements.
     * @return The create element representing the provided pojo.
     * @throws ParserConfigurationException Might throw a ParserConfigurationException.
     * @throws IllegalAccessException Might throw a IllegalAccessException.
     * @throws InstantiationException Might throw a InstantiationException.
     */
    public Element generateSimpleElement(final Document document,
            final Object pojo, final List<String> attributes) 
            throws ParserConfigurationException,
            IllegalAccessException, InstantiationException {
        return generateSimpleElement(document, pojo.getClass().getSimpleName(), pojo, attributes);
    }
    
    /**
     * Generates an xml element from this pojo. Translating the fields like described
     * in the class description.
     * @param document The document in which the nodes should be.
     * @param rootName This is to use another name for the root element than the
     * simple class name.
     * @param pojo The pojo to take the fields from.
     * @param attributes The fields which should be used as attributes and not
     * as elements.
     * @return The create element representing the provided pojo.
     * @throws ParserConfigurationException Might throw a ParserConfigurationException.
     * @throws IllegalAccessException Might throw a IllegalAccessException.
     * @throws InstantiationException Might throw a InstantiationException.
     */
    public Element generateSimpleElement(final Document document, final String rootName, 
            final Object pojo, final String ... attributes) throws ParserConfigurationException, 
            IllegalAccessException, InstantiationException {
        return generateSimpleElement(document, rootName, pojo, Arrays.asList(attributes));
    }
    
    /**
     * Generates an xml element from this pojo. Translating the fields like described
     * in the class description.
     * @param document The document in which the nodes should be.
     * @param rootName This is to use another name for the root element than the
     * simple class name.
     * @param pojo The pojo to take the fields from.
     * @param attributes The fields which should be used as attributes and not
     * as elements.
     * @return The create element representing the provided pojo.
     * @throws ParserConfigurationException Might throw a ParserConfigurationException.
     * @throws IllegalAccessException Might throw a IllegalAccessException.
     * @throws InstantiationException Might throw a InstantiationException.
     */
    public Element generateSimpleElement(final Document document, final String rootName,
            final Object pojo, final List<String> attributes) 
            throws ParserConfigurationException,
            IllegalAccessException, InstantiationException {
        Element rootNode = document.createElementNS(getDefaultNamespace(), rootName);
        List<Field> fields = getNonTransientSimpleFields(pojo.getClass());
        for (Field field : fields) {            
            field.setAccessible(true);
            String fieldName = field.getName();
                                    
            if (field.get(pojo) != null) {
                
                if (!attributes.contains(fieldName)) {
                    
                    Element element = document.createElementNS(getDefaultNamespace(), getElementName(field));
                    
                    // handle CDATAs
                    if (field.isAnnotationPresent(XmlValue.class)) {
                        CDATASection cdata = document.createCDATASection(field.get(pojo).toString());
                        element.appendChild(cdata);
                    }
                    else {
                      element.setTextContent(field.get(pojo).toString());                    
                    }
                    
                    rootNode.appendChild(element);                    
                }
                else {
                    rootNode.setAttribute(getAttributeName(field), field.get(pojo).toString());
                }
            }
        }
        return rootNode;
    }
    
    /**
     * Gets the element name to a field.
     * @param field The field to get the element name.
     * @return The element name of this field.
     */
    private String getElementName(final Field field) {
        String elementName;
        XmlElement xmlElement = field.getAnnotation(XmlElement.class);
        if (xmlElement != null) {
            elementName = xmlElement.name();
        }
        else {
            elementName = javaNameToXMLName(field.getName());
        }
        return elementName;
    }
    
    /**
     * Gets the attribute name to a field.
     * @param field The field to get the atribute name.
     * @return The attribute name of this field.
     */
    private String getAttributeName(final Field field) {
        String attributeName;
        XmlAttribute xmlAttribute = field.getAnnotation(XmlAttribute.class);
        if (xmlAttribute != null) {
            attributeName = xmlAttribute.name();
        }
        else {
            attributeName = javaNameToXMLName(field.getName());
        }
        return attributeName;
    }

    /**
     * Encodes a field name in Java notation (e.g. myFieldName) to an XML
     * field name (e.g. my-field-name).
     * @param fieldName The java field name to convert.
     * @return The converted xml name.
     */
    public final String javaNameToXMLName(final String fieldName) {
      // StringBuilder for constructing the resulting XML-encoded field name
      StringBuilder str = new StringBuilder();
      // Number of characters in the field name
      int fieldNameLength = fieldName.length();
      char currentChar;
      for (int i = 0; i != fieldNameLength; i++) {
        currentChar =  fieldName.charAt(i);
        if (Character.isUpperCase(currentChar)) {
          str.append('-');
          str.append(Character.toLowerCase(currentChar));
        } else {
          str.append(currentChar);
        }
      }
      return str.toString();
    }

    /**
     * @return the defaultNamespace
     */
    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    /**
     * @param defaultNamespace the defaultNamespace to set
     */
    public void setDefaultNamespace(final String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }
    
    /**
     * Transforms a DOM document to a XML String.
     * @param document The document to transform.
     * @return A String XML representation of this document.
     * @throws TransformerException May throw a TransformerException.
     */
    public String getDocumentString(final Document document) 
            throws TransformerException {
        Transformer transformer = getTranformer();
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(document);
        transformer.transform(source, result);
        String xmlString = result.getWriter().toString();
        return xmlString;
    }
    
    /**
     * Returns a Transformer object. It is a singleton.
     * @return A transformer object.
     * @throws TransformerConfigurationException May throw a TransformerConfigurationException.
     */
    private Transformer getTranformer() throws TransformerConfigurationException {
        if (transformer == null) {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        return transformer;
    }
    
}
