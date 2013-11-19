/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2013 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.daq.process;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.jms.MessageFormatException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.springframework.jms.support.converter.MessageConversionException;
import org.w3c.dom.Document;

import cern.c2mon.util.parser.ParserException;
import cern.c2mon.util.parser.SimpleXMLParser;


/**
 * Helper class to transform a class using simpleframework annotations into XML
 * 
 * @author vilches
 * 
 */
public class XMLConverter {
  /** Log4j instance */
  private static final Logger LOGGER = Logger.getLogger(ProcessMessageConverter.class);
  
  /**
   * Simple DOM parser for parsing XML message content
   */
  private SimpleXMLParser parser;
  
  /**
   * Constructor
   */
  public XMLConverter() { 
    try {
      this.parser = new SimpleXMLParser();
    }
    catch (ParserConfigurationException e) {
      //should not happen: throw unchecked fatal error
      throw new RuntimeException("Error creating instance of SimpleXMLParser:", e);
    }
  }
  
  /**
   * Creates a XML representation of this class by making use of
   * the simpleframework XML library.
   * @param obj The object to convert to XML
   * @return The XML representation of this class
   */
  public final String toXml(final Object obj) throws MessageConversionException {
      Serializer serializer = new Persister(new AnnotationStrategy());
      StringWriter fw = null;
      String result = null;

      try {
        fw = new StringWriter();
        if(obj instanceof ProcessConnectionRequest) {
          LOGGER.trace("toXml() : converting from ProcessConnectionRequest to XML.");
          serializer.write(((ProcessConnectionRequest) obj), fw);
        } else if(obj instanceof ProcessConnectionResponse) {
          LOGGER.trace("toXml() : converting from ProcessConnectionResponse to XML.");
          serializer.write(((ProcessConnectionResponse) obj), fw);
        } else if(obj instanceof ProcessConfigurationRequest) {
          LOGGER.trace("toXml() : converting from ProcessConfigurationRequest to XML.");
          serializer.write(((ProcessConfigurationRequest) obj), fw);
        } else if(obj instanceof ProcessConfigurationResponse) {
          LOGGER.trace("toXml() : converting from ProcessConfigurationResponse to XML.");
          serializer.write(((ProcessConfigurationResponse) obj), fw);
        } else if(obj instanceof ProcessDisconnectionRequest) {
          LOGGER.trace("toXml() : converting from ProcessDisconnectionRequest to XML.");
          serializer.write(((ProcessDisconnectionRequest) obj), fw);
        } else {
          LOGGER.error("toXml() : Object type no found: " + obj.getClass());    
          throw new MessageConversionException("toXml() : unsupported type");
        }
        result = fw.toString();
      } catch (Exception e) {
        LOGGER.error("toXml(): Error coverting object " + obj + " to XML: " + e);
      } finally {
        if (fw != null) {
          try {
            fw.close();
          } catch (IOException e) {
            LOGGER.error("toXml(): Error closing file. " + e);
            e.printStackTrace();
          }
        }
      }

      return result;
    }
  
  /**
   * Static method for creating a Process object
   * from a XML String by making use of the simpleframework XML library.
   * @param xml The XML to parse
   * @return Document Document created from the given XML String
   * @throws Exception In case of a parsing error or a wrong XML definition
   */
  public final Object fromXml(final String xml) throws Exception {
    try {
      Document doc = this.parser.parse(xml);
      // Getting the XML doc name to make call the proper class
      String docName = doc.getDocumentElement().getNodeName();
      
      LOGGER.trace("fromXml() - Message received from " + docName + ": " + xml);
      
      if(docName.equals(ProcessMessageType.CONNECT_REQUEST.getName())) {
        return fromXml(xml, ProcessMessageType.CONNECT_REQUEST);
      } else if(docName.equals(ProcessMessageType.CONNECT_RESPONSE.getName())) {
        return fromXml(xml, ProcessMessageType.CONNECT_RESPONSE); 
      } else if(docName.equals(ProcessMessageType.CONFIG_REQUEST.getName())) {
        return fromXml(xml, ProcessMessageType.CONFIG_REQUEST); 
      } else if(docName.equals(ProcessMessageType.CONFIG_RESPONSE.getName())) {
        return fromXml(xml, ProcessMessageType.CONFIG_RESPONSE); 
      } else if(docName.equals(ProcessMessageType.DISCONNETION_REQUEST.getName())) {
        return fromXml(xml, ProcessMessageType.DISCONNETION_REQUEST); 
      } else {
        LOGGER.error("fromXml() : Cannot deserialize XML message since the message type could not be determined" + xml);
        throw new MessageFormatException("XML TAG Node Name not found: " + xml);  
      }
    } catch (ParserException ex) {        
      LOGGER.error("Exception caught in DOM parsing of incoming message: ", ex);
      LOGGER.error("Message was: " + xml);          
      throw new MessageConversionException("Exception caught in DOM parsing on process request message");
    }
  }
  
  /**
   * Static method for creating a Process object
   * from a XML String by making use of the simpleframework XML library.
   * @param xml The XML representation of a <code>processConnectionResponse</code> object
   * @param processType The process type to call the proper object cast
   * @return Object Object created from the given XML String
   * @throws Exception In case of a parsing error or a wrong XML definition
   */
  private final Object fromXml(final String xml, final ProcessMessageType processType) throws Exception {
    StringReader sr = null;
    Serializer serializer = new Persister(new AnnotationStrategy());

    try {
      sr = new StringReader(xml);
      switch(processType) {
        case CONNECT_REQUEST:
          LOGGER.trace("fromXml() : converting from XML to ProcessConnectionRequest.");
          return serializer.read(ProcessConnectionRequest.class, sr, false);
        case CONNECT_RESPONSE:
          LOGGER.trace("fromXml() : converting from XML to ProcessConnectionResponse.");
          return serializer.read(ProcessConnectionResponse.class, sr, false);
        case CONFIG_REQUEST:
          LOGGER.trace("fromXml() : converting from XML to ProcessConfigurationRequest.");
          return serializer.read(ProcessConfigurationRequest.class, sr, false);
        case CONFIG_RESPONSE:
          LOGGER.trace("fromXml() : converting from XML to ProcessConfigurationResponse.");
          return serializer.read(ProcessConfigurationResponse.class, sr, false);
        case DISCONNETION_REQUEST:
          LOGGER.trace("fromXml() : converting from XML to ProcessDisconnectionRequest.");
          return serializer.read(ProcessDisconnectionRequest.class, sr, false);
        default:
          LOGGER.error("fromXml() : Process type not found: " + processType);
          throw new MessageFormatException("fromXml(): Process type not found: " + processType);  
      }
    } finally {
      if (sr != null) {
        sr.close();
      }
    }
  }

}
