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
package cern.c2mon.shared.daq.process.backward;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.parsers.ParserConfigurationException;
 
import org.apache.log4j.Logger;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.w3c.dom.Document;
 
import cern.c2mon.util.parser.ParserException;
import cern.c2mon.util.parser.SimpleXMLParser;
 
/**
 * Spring message converter converting between objects and messages along the
 * tim.sys.ProcessRequest queue.
 * No Spring used within the class.
 * @author mbrightw
 *
 */
public class ProcessRequestConverterBC implements MessageConverter {
 
 /**
   * Private Logger.
   */
 private static final Logger LOGGER = Logger.getLogger(ProcessRequestConverterBC.class);
 
 /** 
   * Simple DOM parser for parsing XML message content 
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
 
 /**
   * Converts a process request message into a ProcessRequest object (ProcessDisconnection
   * or {@link ProcessConnectionRequest}.
   * 
   * @throws MessageConversionException if unable to decode the XML text message
   * @throws NullPointerException if passed message is null
   */
 @Override
 public Object fromMessage(Message message) throws JMSException, MessageConversionException {    
   if (TextMessage.class.isAssignableFrom(message.getClass())) {
     try {      
       Document doc = parser.parse(((TextMessage) message).getText());
       String docName = doc.getDocumentElement().getNodeName();   
       ProcessRequestBC processRequest;
       if (docName.equals(ProcessDisconnectionBC.XML_ROOT_ELEMENT)) {
         if (LOGGER.isDebugEnabled()) {
           LOGGER.debug("fromMessage - Backward Compatibility - ProcessDisconnection received.");
         }
         processRequest = ProcessDisconnectionBC.fromXML(doc.getDocumentElement());
       } else if (docName.equals(ProcessConnectionRequestBC.XML_ROOT_ELEMENT)) {
         if (LOGGER.isDebugEnabled()) {
           LOGGER.debug("fromMessage - Backward Compatibility - ProcessConnectionRequest received.");
         }
         // Extract reply topic
         Destination replyQueue = null;
         try {
           replyQueue = message.getJMSReplyTo();
         } catch (JMSException jmse) {
           LOGGER.warn("fromMessage - Backward Compatibility - Cannot extract ReplyTo from message.", jmse);
           throw jmse;
         }
         if (replyQueue != null ) {//&& replyTopic instanceof Topic
           processRequest = ProcessConnectionRequestBC.fromXML(doc.getDocumentElement()); //never returns null
           ((ProcessConnectionRequestBC) processRequest).setReplyQueue(replyQueue);
         } else {          
           LOGGER.warn("fromMessage - Backward Compatibility - JMSReplyTo destination is not a valid queue and could not be extracted from ProcessConfigurationRequest");
           throw new MessageConversionException("JMS reply queue could not be extracted (Backward Compatibility).");
         }
       } else {
        LOGGER.warn("fromMessage - Backward Compatibility - Unrecognized XML message received on the process request topic - Message from old Disconnection procedure");
        LOGGER.warn("  (unrecognized document root element is " + docName +")");
        throw new MessageConversionException("Unrecognized XML message received (Backward Compatibility).");
       }  
       
       return processRequest;
       
     } catch (ParserException ex) {        
       LOGGER.warn("fromMessage - Backward Compatibility - Exception caught in DOM parsing of incoming message: ", ex);
       //TODO may need to adjust encoding in next line (UTF-8 or whatever used by ActiveMQ)?
       LOGGER.warn("Message was: " + ((TextMessage) message).getText());          
       throw new MessageConversionException("Exception caught in DOM parsing on process request message (Backward Compatibility)");
     }   
   } else {
     StringBuffer str = new StringBuffer("fromMessage() - Backward Compatibility - Unsupported message type(");
     str.append(message.getClass().getName());
     str.append(") : Message discarded (Backward Compatibility).");
     LOGGER.warn(str);
     throw new MessageConversionException("Unsupported JMS message type (Backward Compatibility).");
   }      
 }
 
 /**
   * Used in DAQ to convert the connection and disconnection requests to JMS messages.
   */
 @Override
 public Message toMessage(Object processRequest, Session session) throws JMSException, MessageConversionException {
   Message textMessage;
   if (processRequest instanceof ProcessConnectionRequestBC) {
     String xmlString = ((ProcessConnectionRequestBC) processRequest).toXML();
     textMessage = session.createTextMessage(xmlString);
   } else if (processRequest instanceof ProcessDisconnectionBC) {
     String xmlString = ((ProcessDisconnectionBC) processRequest).toXML();
     textMessage = session.createTextMessage(xmlString);
   } else {
     LOGGER.warn("toMessage - Backward Compatibility - ProcessRequestConverter is being called on the following type that is not supported: " + processRequest.getClass());      
     throw new MessageConversionException("Backward Compatibility - ProcessRequestConverter is being called on an unsupported type");
   }
   return textMessage;       
 }
 
 
}
