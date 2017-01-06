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

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;

/**
 * Helper class that specifies a converter between Java objects and strings.
 * 
 * @author Martin Flamm
 * 
 */
public final class ProcessMessageConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMessageConverter.class);
  private ObjectMapper mapper = new ObjectMapper();
  private XMLConverter xmlConverter = new XMLConverter();

  /**
   * Converts a XML string to a Java object.
   * 
   * @param xml
   * @return the converted Java object
   */
  @Deprecated
  public Object fromXML(final String xml) {
    try {
      LOGGER.debug("fromXML() - Message properly received");
      return this.xmlConverter.fromXml(xml);
    }
    catch (Exception ex) {
      LOGGER.error("fromXML() - Error occurred while converting XML to object. " + xml);
    }
    return null;
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

  /**
   * Converts JSON to a Java object.
   *
   * @param json
   * @return the converted Java object
   */
  public Object fromJSON(final String json) {
    try {
      return mapper.readValue(json, ProcessCommunication.class);
    }
    catch (IOException e) {
      LOGGER.error("fromJSON() - Error occurred while parsing JSON. " + json);
      return null;
    }
  }

  /**
   * Converts a Java object JSON.
   *
   * @param object The object to convert to JSON
   * @return the converted JSON String
   */
  public String toJSON(ProcessCommunication object) {
    try {
      return mapper.writeValueAsString(object);
    }
    catch (JsonProcessingException e) {
      LOGGER.error("toJSON() - Error occurred while generating JSON. " + object.getClass());
      return null;
    }
  }
}
