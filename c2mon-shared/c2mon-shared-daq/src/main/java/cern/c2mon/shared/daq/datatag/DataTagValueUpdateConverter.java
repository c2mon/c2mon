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
package cern.c2mon.shared.daq.datatag;

import cern.c2mon.shared.common.datatag.DataTagValueUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.IOException;

/**
 * Converter class used to convert between JSON messages and
 * {@link DataTagValueUpdate} instances.
 *
 * @author Mark Brightwell
 */
@Slf4j
public class DataTagValueUpdateConverter implements MessageConverter {

  private ObjectMapper mapper;

  @PostConstruct
  public void init() {
    this.mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
  }

  /**
   * Converts an incoming tag update into a {@link DataTagValueUpdate} object.
   *
   * @param message the incoming tag update
   *
   * @throws JMSException if an error occurs converting the message
   */
  @Override
  public Object fromMessage(final Message message) throws JMSException {

    if (message == null) {
      throw new MessageConversionException("Message must not be null!");
    }

    if (!(message instanceof TextMessage)) {
      throw new MessageConversionException("Message must be an instance of TextMessage!");
    }

    try {
      String json = ((TextMessage) message).getText();
      log.trace("Update received from DAQ:\n" + json);

      return mapper.readValue(json, DataTagValueUpdate.class);
    } catch (IOException | RuntimeException e) {
      log.error("Exception caught while parsing incoming update", e);
      throw new MessageConversionException("Exception caught while parsing incoming update: " + ((TextMessage) message).getText(), e);
    }
  }

  /**
   * Converts a {@link DataTagValueUpdate} to a JMS {@link Message}
   *
   * @param tag     the tag to convert
   * @param session the session in which the message must be created
   *
   * @return the resulting message
   * @throws JMSException if an error occurs in creating the message
   */
  @Override
  public Message toMessage(final Object tag, final Session session) throws JMSException {
    try {
      String json = mapper.writeValueAsString(tag);
      return session.createTextMessage(json);

    } catch (JsonProcessingException e) {
      log.error("Exception caught on update reception", e.getMessage());
      throw new MessageConversionException("Exception caught in converting dataTagValueUpdate to a json String:"
          + e.getMessage());
    }
  }
}

