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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;

/**
 * Helper class that specifies a converter between Java objects and strings.
 * 
 * @author vilches
 * 
 */
@Slf4j
public final class ProcessMessageConverter {

  private ObjectMapper mapper = new ObjectMapper();
  private XMLConverter xmlConverter = new XMLConverter();

  /**
   * Converts a XML string to a Java object.
   * 
   * @param xml the request or response as a XML string
   * @return the converted Java object
   */
  @Deprecated
  public Object fromXML(final String xml) {
    try {
      log.debug("Message properly received");
      return this.xmlConverter.fromXml(xml);
    }
    catch (Exception ex) {
      log.error("Error occurred while converting XML to object " + xml);
      return null;
    }
  }

  /**
   * Convert a Java object to a XML based JMS message using the supplied session
   * to create the message object.
   *
   * @param object the object to convert to XML
   * @return the JMS message
   * @throws MessageConversionException if the type of the object is not supported
   */
  @Deprecated
  public String toXML(Object object) {
    return this.xmlConverter.toXml(object);
  }

  /**
   * Converts JSON String to a Java object.
   *
   * @param json the request or response as a JSON string
   * @return the mapped Java object
   */
  public Object fromJSON(final String json) {
    try {
      return mapper.readValue(json, ProcessCommunication.class);
    }
    catch (IOException e) {
      log.error("Error occurred while parsing JSON " + json);
      return null;
    }
  }

  /**
   * Converts a Java object to JSON.
   *
   * @param object the object to convert to JSON
   * @return json representation of the given object
   */
  public String toJSON(ProcessCommunication object) {
    try {
      return mapper.writeValueAsString(object);
    }
    catch (JsonProcessingException e) {
      log.error("Error occurred while generating JSON " + object.getClass());
      return null;
    }
  }
}
