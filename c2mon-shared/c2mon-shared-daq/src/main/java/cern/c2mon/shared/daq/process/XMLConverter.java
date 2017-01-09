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
package cern.c2mon.shared.daq.process;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.jms.MessageFormatException;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.springframework.jms.support.converter.MessageConversionException;
import org.w3c.dom.Document;

import cern.c2mon.shared.util.parser.ParserException;
import cern.c2mon.shared.util.parser.SimpleXMLParser;


/**
 * Helper class to transform a class using simpleframework annotations into XML
 * 
 * @author vilches
 *
 *
 */
@Slf4j
@Deprecated
public class XMLConverter {

  private SimpleXMLParser parser;

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
          log.trace("Converting from ProcessConnectionRequest to XML");
          serializer.write(((ProcessConnectionRequest) obj), fw);
        } else if(obj instanceof ProcessConnectionResponse) {
          log.trace("Converting from ProcessConnectionResponse to XML");
          serializer.write(((ProcessConnectionResponse) obj), fw);
        } else if(obj instanceof ProcessConfigurationRequest) {
          log.trace("Converting from ProcessConfigurationRequest to XML");
          serializer.write(((ProcessConfigurationRequest) obj), fw);
        } else if(obj instanceof ProcessConfigurationResponse) {
          log.trace("converting from ProcessConfigurationResponse to XML");
          serializer.write(((ProcessConfigurationResponse) obj), fw);
        } else if(obj instanceof ProcessDisconnectionRequest) {
          log.trace("Converting from ProcessDisconnectionRequest to XML");
          serializer.write(((ProcessDisconnectionRequest) obj), fw);
        } else {
          log.error("Object type not found: " + obj.getClass());
          throw new MessageConversionException("toXml() : unsupported type");
        }
        result = fw.toString();
      } catch (Exception e) {
        log.error("Error converting object " + obj + " to XML: " + e);
      } finally {
        if (fw != null) {
          try {
            fw.close();
          } catch (IOException e) {
            log.error("Error closing file while XML generation " + e);
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

      log.trace("Message received from " + docName + ": " + xml);
      
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
        log.error("Cannot deserialize XML message since the message type could not be determined" + xml);
        throw new MessageFormatException("XML TAG Node Name not found: " + xml);  
      }
    } catch (ParserException ex) {
      log.error("Exception caught in DOM parsing of incoming message: ", ex);
      log.error("Message was: " + xml);
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
          log.trace("Converting from XML to ProcessConnectionRequest");
          return serializer.read(ProcessConnectionRequest.class, sr, false);
        case CONNECT_RESPONSE:
          log.trace("Converting from XML to ProcessConnectionResponse");
          return serializer.read(ProcessConnectionResponse.class, sr, false);
        case CONFIG_REQUEST:
          log.trace("Converting from XML to ProcessConfigurationRequest");
          return serializer.read(ProcessConfigurationRequest.class, sr, false);
        case CONFIG_RESPONSE:
          log.trace("Converting from XML to ProcessConfigurationResponse");
          return serializer.read(ProcessConfigurationResponse.class, sr, false);
        case DISCONNETION_REQUEST:
          log.trace("Converting from XML to ProcessDisconnectionRequest");
          return serializer.read(ProcessDisconnectionRequest.class, sr, false);
        default:
          log.error("Process type not found: " + processType);
          throw new MessageFormatException("Process type not found while parsing XML: " + processType);
      }
    } finally {
      if (sr != null) {
        sr.close();
      }
    }
  }

}
