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
import cern.c2mon.shared.util.parser.SimpleXMLParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.IOException;

/**
 * Converter class used to convert between JMS messages and
 * {@link DataTagValueUpdate} instances.
 *
 * @author Mark Brightwell
 */
@Slf4j
public class DataTagValueUpdateConverter implements MessageConverter {

  private ObjectMapper mapper;

  private SimpleXMLParser parser;

  public DataTagValueUpdateConverter() {
    this.mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);

    try {
      this.parser = new SimpleXMLParser();
    } catch (Exception e) {
      throw new RuntimeException("Error creating instance of SimpleXMLParser", e);
    }
  }

  /**
   * Converts an incoming message into a {@link DataTagValueUpdate}. Never
   * returns null.
   *
   * @param message the incoming JMS message
   * @throws JMSException               if the content of the message cannot
   *                                    be extracted due to some JMS problem
   * @throws MessageConversionException if error occurs during parsing of
   *                                    message content (including non-text
   *                                    message reception)
   */
  @Override
  public Object fromMessage(final Message message) throws JMSException {
    if (message == null) {
      throw new MessageConversionException("Listener called with null JMS message argument");
    }

    if (!(message instanceof TextMessage)) {
      throw new MessageConversionException("Received a non text message from JMS: unable to process it");
    }

    try {
      String text = ((TextMessage) message).getText();
      log.trace("Update received from DAQ: \n{}", text);

      if (isJsonString(text)) {
        return readJsonString(text);
      } else {
        // If the message isn't JSON, then it must be a legacy XML update
        return readXmlString(text);
      }

    } catch (IOException | RuntimeException ex) {
      log.error("Exception caught on update reception", ex);
      throw new MessageConversionException("Error parsing incoming tag update. "
          + "Message content was: " + ((TextMessage) message).getText(), ex);
    }
  }

  /**
   * Converts a {@link DataTagValueUpdate} to a JMS Message
   *
   * @param dataTagValueUpdate the update object to convert
   * @param session            the JMS session in which the message must be created
   * @return the resulting JMS message
   * @throws JMSException if an error occurs in creating the JMS message
   */
  @Override
  public Message toMessage(final Object dataTagValueUpdate, final Session session) throws JMSException {
    try {
      String jsonString = mapper.writeValueAsString(dataTagValueUpdate);
      return session.createTextMessage(jsonString);

    } catch (JsonProcessingException e) {
      log.error("Exception caught on update reception", e.getMessage());
      throw new MessageConversionException("Exception caught in converting dataTagValueUpdate to a json String:" + e.getMessage());
    }
  }

  private DataTagValueUpdate readJsonString(String jsonString) throws IOException {
    return mapper.readValue(jsonString, DataTagValueUpdate.class);
  }

  private DataTagValueUpdate readXmlString(String xmlString) {
    Document xmlDocument = parser.parse(xmlString);
    Element rootElement = xmlDocument.getDocumentElement();
    return DataTagValueUpdate.fromXML(rootElement);
  }

  /**
   * @return true if the given string is a valid JSON string, false otherwise
   */
  private boolean isJsonString(final String string) {
    try {
      mapper.readTree(string);
      return true;
    } catch (IOException ioe) {
      return false;
    }
  }
}


