/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
package cern.c2mon.shared.client.configuration;

import java.io.StringWriter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cern.c2mon.shared.util.parser.ParserException;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

/**
 * JMSMessage to ConfigurationRequest conversion class. Also checks that
 * a replyTo is set on the message to send back a report.
 * 
 * <p>Also contains the XML conversion methods.
 * 
 * @author Mark Brightwell
 *
 */
public class ConfigurationRequestConverter implements MessageConverter {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ConfigurationRequestConverter.class);
  
  /**
   * XML root element
   */
  public static final String CONFIGURATION_XML_ROOT = "ConfigurationRequest";
  
  /**
   * XML id attribute
   */
  public static final String CONFIGURATION_ID_ATTRIBUTE = "config-id";
  
  /**
   * Could remove this parser and use Java standard one.
   */
  private SimpleXMLParser parser;
  
  /**
   * Init method run on bean instantiation.
   * Initializes XML parser.
   */
  public void init() {
    try {
      this.parser = new SimpleXMLParser();
    }
    catch (ParserConfigurationException e) {
      //should not happen: throw unchecked fatal error
      throw new RuntimeException("Error creating instance of SimpleXMLParser:", e);
    }
  }

  @Override
  public Object fromMessage(final Message message) throws JMSException, MessageConversionException {
    if (TextMessage.class.isAssignableFrom(message.getClass())) {
      try {      
        Document doc = parser.parse(((TextMessage) message).getText());
        String docName = doc.getDocumentElement().getNodeName();           
        if (docName.equals(CONFIGURATION_XML_ROOT)) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("fromMessage() : Reconfiguration request received.");
          }
          return fromXML(doc.getDocumentElement());          
        } else {
         LOGGER.error("Unrecognized XML message received on the reconfiguration request topic - is being ignored");
         LOGGER.error("  (unrecognized document root element is " + docName + ")");
         throw new MessageConversionException("Unrecognized XML message received.");
        }          
      } catch (ParserException ex) {        
        LOGGER.error("Exception caught in DOM parsing of incoming message: ", ex);
        //TODO may need to adjust encoding in next line (UTF-8 or whatever used by ActiveMQ)?
        LOGGER.error("Message was: " + ((TextMessage) message).getText());          
        throw new MessageConversionException("Exception caught in DOM parsing on reconfiguration request message");
      }   
    } else {
      StringBuffer str = new StringBuffer("fromMessage() : Unsupported message type(");
      str.append(message.getClass().getName());
      str.append(") : Message discarded.");
      LOGGER.error(str); 
      throw new MessageConversionException("Unsupported JMS message type received on configuration request connection.");
    }     
  }

  @Override
  public Message toMessage(final Object configRequest, final Session session) throws JMSException, MessageConversionException {
    Message textMessage;
    if (configRequest instanceof ConfigurationRequest) {
      String xmlString;
      try {
        xmlString = toXML(((ConfigurationRequest) configRequest));
        textMessage = session.createTextMessage(xmlString);
        return textMessage;
      } catch (ParserConfigurationException ex) {
        LOGGER.error("Parser exception caught whilst encoding the ConfigurationRequest object; aborting request. ", ex);
        ex.printStackTrace();
        throw new MessageConversionException("ConfigurationRequest conversion failed.",  ex);
      } catch (TransformerException ex) {       
        LOGGER.error("Transformer exception caught whilst encoding the ConfigurationRequest object; aborting request. ", ex);        
        ex.printStackTrace();
        throw new MessageConversionException("ConfigurationRequest conversion failed.",  ex);
      }      
    } else {
      LOGGER.error("ConfigurationRequestConverter is being called on the following type that is not supported: " + configRequest.getClass());      
      throw new MessageConversionException("ConfigurationRequestConverter is being called on an unsupported type");
    }    
  }
  
  /**
   * Create an XML string representation of the ProcessConnectionRequest object.
   * @throws ParserConfigurationException if exception occurs in XML parser 
   * @throws TransformerException if exception occurs in XML parser
   * @param configurationRequest the request object to convert to XML
   * @return XML as String
   */
  public synchronized String toXML(final ConfigurationRequest configurationRequest) throws ParserConfigurationException, TransformerException {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();    
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    Document dom = builder.newDocument();
    Element rootElt = dom.createElement(CONFIGURATION_XML_ROOT);
    rootElt.setAttribute(CONFIGURATION_ID_ATTRIBUTE, Integer.toString(configurationRequest.getConfigId()));
    dom.appendChild(rootElt);
    
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    StringWriter writer = new StringWriter();
    Result result = new StreamResult(writer);
    Source source = new DOMSource(dom);
    transformer.transform(source, result);
    return writer.getBuffer().toString(); 
  
  }
  
  /**
   * Converts the top level DOM element of an XML document to
   * a ConfigurationRequest object.
   * @param domElement the top-level XML element ("ConfigurationRequest")
   * @return the request object
   */
  private static ConfigurationRequest fromXML(final Element domElement) {
    ConfigurationRequest result = null;

    if (domElement.getNodeName().equals(CONFIGURATION_XML_ROOT)) {
      String idStr = domElement.getAttribute(CONFIGURATION_ID_ATTRIBUTE);
      if (idStr == null || idStr.length() == 0) {
        throw new RuntimeException("Unable to read configuration request id: " + idStr);
      } else {
        result = new ConfigurationRequest(new Integer(idStr));
      }
      return result;
    } else {
      throw new MessageConversionException("Called the fromXML message on the wrong XML doc Element! - check your code as this should be checked first"); 
    }
  }
}
