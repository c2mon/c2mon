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
package cern.c2mon.server.client.request;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;

import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestImpl;

import com.google.gson.JsonSyntaxException;

/**
 * This abstract class provides a static method for converting a JMS tag request
 * message into a <code>TransferTagRequest</code> object.
 *
 * @author Matthias Braeger
 * @see ClientRequest
 */
abstract class ClientRequestMessageConverter {

  /** Class logger */
  private static final Logger LOG = LoggerFactory.getLogger(ClientRequestMessageConverter.class);

  /**
   * Hidden default constructor
   */
  private ClientRequestMessageConverter() {
    // Do nothing
  }

  /**
   * Converts the received JMS message to a <code>TransferTagRequest</code> object.
   * @param message The received JMS message
   * @return The deserialized <code>TransferTagRequest</code>
   * @throws JMSException In case of problems when getting the text from the JMS text message
   * @throws MessageConversionException In case of problems while deserializing the JMS message
   */
  public static final ClientRequest fromMessage(final Message message) throws JMSException, MessageConversionException {

    if (message instanceof TextMessage) {
      String json = ((TextMessage) message).getText();
      try {
        return ClientRequestImpl.fromJson(json);

      }
      catch (JsonSyntaxException jse) {
        StringBuffer str = new StringBuffer("fromMessage() : Unsupported JSON message (");
        str.append(json);
        str.append(") : Message discarded.");
        LOG.error(str.toString());
        throw new MessageConversionException("Unsupported JSON message received on tag request connection.");
      }
    }

    else if (message instanceof ObjectMessage) {

      ObjectMessage oMessage = (ObjectMessage) message;

      Object object = oMessage.getObject();

      return ClientRequestImpl.fromObject(object);
    }

    else {
      StringBuffer str = new StringBuffer("fromMessage() : Unsupported message type(");
      str.append(message.getClass().getName());
      str.append(") : Message discarded.");
      LOG.error(str.toString());
      throw new MessageConversionException("Unsupported JMS message type received on tag request connection.");
    }
  }
}
