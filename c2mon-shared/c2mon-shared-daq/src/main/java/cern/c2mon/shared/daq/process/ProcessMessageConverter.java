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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;

/**
 * Helper class that specifies a converter between Java objects and JMS messages.
 * 
 * @author Martin Flamm
 * 
 */
public final class ProcessMessageConverter {
  /** Log4j instance */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMessageConverter.class);
  
  /**
   * XML Converter helper class
   */
  private XMLConverter xmlConverter = new XMLConverter();

  /**
   * Converts from a XML based JMS Message to a Java object.
   * 
   * @param message the xml based JMS message
   * @return the converted Java object
   * @throws JMSException if thrown by JMS API methods
   */
  @Deprecated
  public Object fromXML(final Message message) throws JMSException {
    try {
      LOGGER.debug("fromXML() - Message properly received");
      return this.xmlConverter.fromXml(((TextMessage) message).getText());
    }
    catch (Exception ex) {
      LOGGER.error("fromXML() - Error occurred while converting XML to object. Message was: " + ((TextMessage) message).getText());
      throw new JMSException(ex.getMessage());
    }
  }


  /**
   * Convert a Java object to a XML based JMS Message using the supplied session
   * to create the message object.
   *
   * @param object The object to convert to XML
   * @return the JMS Message
   * @throws MessageConversionException if the type of the object is not supported
   */
  @Deprecated
  public String toXML(Object object) {
    return this.xmlConverter.toXml(object);
  }

  }

}
