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
package cern.c2mon.server.video;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;

import cern.c2mon.shared.video.VideoRequest;
import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * This abstract class provides a static method for converting a VideoRequest.
 *
 * @author ekoufaki
 */
public abstract class VideoRequestMessageConverter {

  /** Class logger */
  private static final Logger LOG = LoggerFactory.getLogger(VideoRequestMessageConverter.class);

  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  /**
   * Hidden default constructor
   */
  private VideoRequestMessageConverter() {
    // Do nothing
  }

  /**
   * Converts the received JMS message to a <code>VideoRequest</code> object.
   * @param message The received JMS message
   * @return The deserialized <code>VideoRequest</code>
   * @throws JMSException In case of problems when getting the text from the JMS text message
   * @throws MessageConversionException In case of problems while deserializing the JMS message
   */
  public static final VideoRequest fromMessage(final Message message) throws JMSException, MessageConversionException {

    if (message instanceof TextMessage) {
      String json = ((TextMessage) message).getText();

      try {
        return GSON.fromJson(json, VideoRequest.class);
      }
      catch (JsonSyntaxException jse) {
        StringBuffer str = new StringBuffer("fromMessage() : Unsupported JSON message (");
        str.append(json);
        str.append(") : Message discarded.");
        LOG.error(str.toString());
        throw new MessageConversionException("Unsupported JSON message received on tag request connection.");
      }
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
