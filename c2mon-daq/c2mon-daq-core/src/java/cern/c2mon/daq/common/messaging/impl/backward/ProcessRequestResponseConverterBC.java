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
package cern.c2mon.daq.common.messaging.impl.backward;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.parsers.ParserConfigurationException;
 
import org.apache.log4j.Logger;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
 
import cern.tim.util.parser.ParserException;
import cern.tim.util.parser.SimpleXMLParser;
 
/**
 * JMS converter class that should be imported into tim-shared as a more general converter
 * XML <--> message
 * (currently only used in DAQ) 
 * @author mbrightw
 *
 */
public class ProcessRequestResponseConverterBC implements MessageConverter {
 
 private Logger LOGGER = Logger.getLogger(ProcessRequestResponseConverterBC.class);
 
 private SimpleXMLParser parser;
 
 @PostConstruct
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
 public Object fromMessage(Message message) throws JMSException, MessageConversionException {
   if (!(message instanceof TextMessage)) {
     throw new MessageFormatException("Expected TextMessage as response but received " + message.getClass());
   } else {           
     try {
       return parser.parse(((TextMessage) message).getText());        
     }
     catch (ParserException ex) {  
       LOGGER.error("Exception caught in DOM parsing of incoming message: " + ex.getMessage());
       //may need to adjust encoding in next line (UTF-8 or whatever used by ActiveMQ)?
       LOGGER.error("Message was: " + ((TextMessage) message).getText());  
       throw new JMSException(ex.getMessage()); //TODO use more specific exceptions here?
     } catch (Exception e) {
       LOGGER.error("Unidentified error caught in conversion of JMS message to XML Document");
       throw new JMSException(e.getMessage());
     }     
   }
 }
 
 /**
   * Not implemented as not used. TODO: could use in server for sending message.
   */
 @Override
 public Message toMessage(Object arg0, Session arg1) throws JMSException, MessageConversionException {
   //not used
   return null;
 }
 
}
