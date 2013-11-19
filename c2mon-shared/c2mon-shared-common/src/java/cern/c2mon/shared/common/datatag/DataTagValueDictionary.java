package cern.c2mon.shared.common.datatag;


import java.io.Serializable;

import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.util.parser.SimpleXMLParser;

import org.apache.log4j.Logger;

import javax.xml.parsers.ParserConfigurationException;


/**
 * DataTagValueDictionary
 *
 * This is an add-on object to the DataTag entity bean and the DataTagCacheObject.
 * The dictionary manages a set of {(value, description)} pairs, which is usually
 * configured from the TDRefDb. Attached to a DataTag, it can be used to obtain
 * a textual description of a tag's current value.
 *
 * @author Jan Stowisek
 * @version $Revision: 1.8 $ ($Date: 2007/07/04 12:38:55 $ - $State: Exp $)
 */
public class DataTagValueDictionary implements Serializable, Cloneable {
  /**
   * Simple XML parser for reading dictionary objects from XML.
   */
  private static SimpleXMLParser parser = null;
  
  /**
   * Log4j Logger for this class.
   */
  private static final Logger LOG = Logger.getLogger(DataTagValueDictionary.class);

  private static final long serialVersionUID = -4306034447960538021L;

  /**
   * Constant that determines the initial capacity of the HashMap used to
   * store the (value, description) pairs. It should be approximately
   * (4/3) * the expected number of values or, ideally, the next higher prime
   * number.
   */
  private static final int INITIAL_CAPACITY = 4;

  /**
   * HashMap used to internally manage the (value, description) pairs.
   */
  private HashMap descriptions;
    
  /**
   * Default constructor.
   * Initialisation of an empty value dictionary
   */
  public DataTagValueDictionary() {
    descriptions = new HashMap(DataTagValueDictionary.INITIAL_CAPACITY);
  }

  /**
   * Copy constructor.
   * Creates a new DataTagValueDictionary holding the same descriptions
   * as the original. 
   */
  public DataTagValueDictionary(DataTagValueDictionary old) {
    if (old != null) {
      this.descriptions = (HashMap) old.descriptions.clone();
    } else {
      this.descriptions = new HashMap(DataTagValueDictionary.INITIAL_CAPACITY);
    }
  }
  
  /**
   * New clone implementation - should remove above copy constructor in due course...
   */
  public Object clone() {    
    try {
      DataTagValueDictionary  dataTagValueDictionary = (DataTagValueDictionary) super.clone();
      dataTagValueDictionary.descriptions = (HashMap) this.descriptions.clone();
      return dataTagValueDictionary;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace(); //should never get here...
      throw new RuntimeException("Exception caught when cloning a DataTagValueDictionary - this should not be happening!");
    }
    
  }

  /**
   * Add a textual description for a certain value.
   * If the dictionary already holds a description for the specified
   * value, the new description will REPLACE the old one. Null parameters
   * will be ignored.
   * @param value         the value for which the dictionary shall return the description
   * @param description   the free-text description attached to the value
   */
  public final void addDescription(final Object pValue, final String pDescription) {
	if (pValue != null) {
      this.descriptions.put(pValue, pDescription);
	}
  }

  /**
   * Remove the preconfigured description for a certain value.
   * If the dictionary doesn't hold a description for the specified
   * value, the call to this method will have no effect. Null parameters
   * are also ignored.
   * @param value         the value for which the description is to be removed from the dictionary
   */
  public final void removeDescription(final Object pValue) {
	if (pValue != null) {
      this.descriptions.remove(pValue);
	}
  }

  /**
   * Retrieve the preconfigured description for a certain value, if any.
   * @return the preconfigured description for a certain value, null if no description is configured
   */
  public final String getDescription(final Object pValue) {
	
    return
      pValue == null ? null : (String) descriptions.get(pValue);
  }

  /**
   * Returns a string representation of this map. 
   * The string representation consists of a list of key-value mappings, 
   * enclosed in braces.
   */
  public String toString() {
    return this.descriptions.toString();
  }
  
  public String toXML() {
    StringBuilder str = new StringBuilder(200);
    str.append("<DataTagValueDictionary>\n");

    Iterator it = this.descriptions.keySet().iterator();
    Object key = null;
    while (it.hasNext()) {
      key = it.next();
      str.append("<entry type=\"");
      str.append(key.getClass().getName().substring(10));
      str.append("\" key=\"");
      str.append(key);
      str.append("\">");
      str.append(this.descriptions.get(key));
      str.append("</entry>");
    }
    str.append("</DataTagValueDictionary>\n");
    return str.toString();
  }
  
  public static DataTagValueDictionary fromXML(final String pXML) {
    if (parser != null) {
      try {
        return fromXML(parser.parse(pXML).getDocumentElement());
      }
      catch (Exception e) {
        LOG.error("fromXML() : Error creating DataTagValueDictionary from XML string:", e);
        return null;
      }
    }
    else {
      return null;
    }
  }
  
  public static DataTagValueDictionary fromXML(final Element pDocumentElement) {
    DataTagValueDictionary result = new DataTagValueDictionary();
  
    NodeList fields = pDocumentElement.getChildNodes();

    String fieldName;
    String fieldValueString;
    Node fieldNode;
    int fieldsCount = fields.getLength();
    
    String dataType  = null;
    String keyString = null;
    String description = null;

    for (int i = 0; i < fieldsCount; i++) {
      fieldNode = fields.item(i);
      if (fieldNode.getNodeType() == 1) {
        fieldName = fieldNode.getNodeName();
        fieldValueString = fieldNode.getFirstChild().getNodeValue();

        if (fieldName.equals("entry")) {
          fieldNode.getAttributes().getNamedItem("key").getNodeValue();
          dataType = fieldNode.getAttributes().getNamedItem("type").getNodeValue();
          keyString = fieldNode.getAttributes().getNamedItem("key").getNodeValue();
          description = fieldValueString;
          
          if (dataType.equals("Integer")) {
            result.addDescription(Integer.valueOf(keyString), description);
          } else if (dataType.equals("Float")) {
            result.addDescription(Float.valueOf(keyString), description);
          } else if (dataType.equals("Double")) {
            result.addDescription(Double.valueOf(keyString), description);
          } else if (dataType.equals("Long")) {
            result.addDescription(Long.valueOf(keyString), description);
          } else if (dataType.equals("Boolean")) {
            result.addDescription(Boolean.valueOf(keyString), description);
          } else if (dataType.equals("String")) {
            result.addDescription(keyString, description);
          }
        }
      }
    }// for
    return result;
  }
  
  static {
    try {
      parser = new SimpleXMLParser();
    }
    catch (ParserConfigurationException e) {
      LOG.error("<static init> Unable to create SimpleXMLParser for this class. This may cause problems creating DataTagValueDictionary objects from XML", e);
    }
  }
}
