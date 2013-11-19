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

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Factory to fill POJOs with values corresponding to a name convention.
 * The convertion between xml name an java field name to fill. Is done like
 * described in the Javadoc of the xmlNametoJavaName method. The following
 * types of fields may be filled (also the wrapper classes matching the 
 * primitive java types):
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
 * Example:
 * <pre>&lt;TestPojo&gt;
 *  &lt;number-test&gt;0&lt;/number-test&gt;
 *  &lt;test-number&gt;238438&lt;/test-number&gt;
 *  &lt;normal-string&gt;sdjhsd&lt;/normal-string&gt;
 *  &lt;not-set-int&gt;0&lt;/not-set-int&gt;
 * &lt;/TestPojo&gt;</pre>
 * May fill the object:
 * <pre>public class TestPojo {
 *   private int numberTest = 0;
 *   private String normalString = "sdjhsd";
 *   private int notSetInt;
 * }</pre>
 * 
 * @author alang
 *
 */
public class ObjectFactory extends SimpleTypeReflectionHandler {

    /**
     * Initializes the simple fields of an POJO corresponding to matching names
     * in the xml.
     * @param pojo The POJO to fill.
     * @param element The DOM Element to use.
     * @return A List of more complex child elements of the provided element which
     * couldn't be handled automatically.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the field
     * you provide via the field name is not a simple type field. 
     */
    public List<Element> initSimpleFields(final Object pojo, 
            final Element element) throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        List<Element> complexElements = new ArrayList<Element>();
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attribute = (Attr) attributes.item(i);
            String attributeName = attribute.getNodeName();
            if (!attributeName.startsWith(XMLConstants.XMLNS_ATTRIBUTE)
                    && !attributeName.startsWith("xsi")) {
                setSimpleFieldForTag(pojo, attributeName, 
                        attribute.getNodeValue());
            }
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {                
                
                Element child = (Element) node;
                
                if (child.hasAttributes() 
                        || (child.hasChildNodes() 
                                && child.getChildNodes().getLength() > 1 
                                || child.getChildNodes().item(0).getNodeType() != Node.TEXT_NODE)) {
                    complexElements.add(child);
                }
                else {
                    
                    setSimpleFieldForTag(pojo, child.getTagName(), child.getTextContent());
                }
            }
        }
        return complexElements;
    }

    /**
     * Sets a field of a pojo corresponding to the provided name with to the
     * provided value.
     * @param pojo Plain old Java object.
     * @param xmlName The xml name of the field to set.
     * @param value The value to set the field to (unconverted).
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the field
     * you provide via the field name is not a simple type field.
     */
    public void setSimpleFieldForTag(final Object pojo, 
            final String xmlName, final String value) 
            throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        setSimpleFieldByString(pojo, xmlNameToJavaName(xmlName), value);
    }
    
    /**
     * Returns the java name to a xml tag/attribute name.
     * @param xmlName The xml name to get the java name for.
     * @return The java name to the provided xml name.
     */
    public final String xmlNameToJavaName(final String xmlName) {
        StringBuilder str = new StringBuilder();
        int fieldNameLength = xmlName.length();
        char currentChar;
        for (int i = 0; i < fieldNameLength; i++) {
          currentChar = xmlName.charAt(i);
          if (currentChar == '-') {
            str.append(Character.toUpperCase(xmlName.charAt(++i)));
          } else {
            str.append(currentChar);
          }
        }
        return str.toString();
    }
}
