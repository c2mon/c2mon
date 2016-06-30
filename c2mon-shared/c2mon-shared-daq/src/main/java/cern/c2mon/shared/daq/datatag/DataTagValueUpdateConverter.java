/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.shared.daq.datatag;

import cern.c2mon.shared.common.datatag.DataTagValueUpdate;
import cern.c2mon.shared.util.parser.SimpleXMLParser;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.IOException;

/**
 * Converter class used to convert between JMS XML messages and DataTagValueUpdate objects.
 *
 * <p>Is used for conversion in the DAQ and server code.
 *
 * @author Mark Brightwell
 */
public class DataTagValueUpdateConverter implements MessageConverter {

  /**
   * Private Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DataTagValueUpdateConverter.class);

  /**
   * Parser for parsing JSON message content
   */
  private ObjectMapper mapper;

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
      this.mapper = new ObjectMapper();
      mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);

      this.parser = new SimpleXMLParser();

    } catch (Exception e) {
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
   * @throws JMSException               if the content of the message cannot be extracted due to some JMS problem
   * @throws MessageConversionException if error occurs during parsing of message content (including non-text message reception)
   */
  //TODO make the fromMessage backwards compatible in order to also receive the old xml format!
  @Override
  public Object fromMessage(final Message message) throws JMSException {
    if (message == null) {
      throw new MessageConversionException("Listener called with null JMS message argument.");
    }
    if (!(message instanceof TextMessage)) {
      throw new MessageConversionException("Received a non-text message from JMS - unable to process it.");
    }
    try {
      String incomingJSON = ((TextMessage) message).getText();
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Update received from DAQ:\n" + incomingJSON);
      }

      if (isJsonString(incomingJSON)) {

        return readJsonString(incomingJSON);

        // If the Sting is not json it must be a update from an 'old' daq. In that case the ols parsing have to be done.
      } else {

        return readXmlString(incomingJSON);
      }

    } catch (IOException | RuntimeException ex) {
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
      LOGGER.error("Exception caught on update reception", e.getMessage());
      throw new MessageConversionException("Exception caught in converting dataTagValueUpdate to a json String:" + e.getMessage());
    }
  }

  private DataTagValueUpdate readJsonString(String jsonString) throws IOException {

    return mapper.readValue(jsonString, DataTagValueUpdate.class);

  }

  private DataTagValueUpdate readXmlString(String xmlString) throws IOException {

    Document xmlDocument = parser.parse(xmlString);
    Element rootElement = xmlDocument.getDocumentElement();
    return DataTagValueUpdate.fromXML(rootElement);

  }

  /**
   * Determine if the string is a valid json String
   *
   * @param json String value which needs to be checked if its a json String.
   * @return True if the string is in the json format
   */
  private boolean isJsonString(final Object json) {
    boolean result = false;
    try {
      if (json instanceof String) {
        final JsonParser parser = mapper.getFactory().createParser((String) json);
        while (parser.nextToken() != null) {
        }
        return true;
      }
    } catch (IOException ioe) {
    }
    return result;
  }
}


