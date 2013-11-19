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
package cern.c2mon.shared.daq.datatag;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cern.c2mon.shared.util.parser.SimpleXMLParser;

/**
 * Converter class used to convert between JMS XML messages and DataTagValueUpdate objects.
 * 
 * <p>Is used for conversion in the DAQ and server code.
 * 
 * @author Mark Brightwell 
 *
 */
public class DataTagValueUpdateConverter implements MessageConverter {
  
  /**
   * Private Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(DataTagValueUpdateConverter.class);
  
  /** 
   * Simple DOM parser for parsing XML message content 
   */
  private SimpleXMLParser parser;
  
  /**
   * Init method run on bean instantiation.
   * Initializes XML parser.
   */
  @PostConstruct
  public void init() {
    try {
      this.parser = new SimpleXMLParser();
    } catch (ParserConfigurationException e) {
      //should not happen: throw unchecked fatal error
      throw new RuntimeException("Error creating instance of SimpleXMLParser:", e);
    }
  }
  
  /**
   * Converts an incoming XML message into a DataTagValueUpdate object.
   * 
   * <p>Never returns null.
   * 
   * @param message the incoming JMS message
   * @throws JMSException if the content of the message cannot be extracted due to some JMS problem
   * @throws MessageConversionException if error occurs during parsing of message content (including non-text message reception)
   */
  @Override
  public Object fromMessage(final Message message) throws JMSException {    
    if (message == null) {
      throw new MessageConversionException("Listener called with null JMS message argument.");
    }
    if (!(message instanceof TextMessage)) {            
      throw new MessageConversionException("Received a non-text message from JMS - unable to process it.");
    }
    try {
      String incomingXml = ((TextMessage) message).getText();
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Update received from DAQ:\n" + incomingXml);
      }
      Document xmlDocument = parser.parse(incomingXml);      
      Element rootElement = xmlDocument.getDocumentElement();
      return DataTagValueUpdate.fromXML(rootElement); //may throw parsing exceptions              
    } catch (RuntimeException ex) {
      LOGGER.error("Exception caught on update reception", ex);
      throw new MessageConversionException("Exception caught in DOM parsing of incoming Tag updates from DAQ layer. "
          + "Message content was: " + ((TextMessage) message).getText(), ex);
    }   
  }

  /**
   * Converts a DataTagValueUpdate to a JMS Message
   * (to be used in DAQ code once TIM2 operational).
   * 
   * @param dataTagValueUpdate the update object to convert
   * @param session the JMS session in which the message must be created
   * @return the resulting JMS message
   * @throws JMSException if an error occurs in creating the JMS message
   */
  @Override
  public Message toMessage(final Object dataTagValueUpdate, final Session session) throws JMSException {
    String xmlString = ((DataTagValueUpdate) dataTagValueUpdate).toXML();
    return session.createTextMessage(xmlString);        
  }
}

  
