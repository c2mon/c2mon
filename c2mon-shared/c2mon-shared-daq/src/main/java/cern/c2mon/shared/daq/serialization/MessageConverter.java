/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.shared.daq.serialization;

import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.serialisation.HardwareAddressDeserializer;
import cern.c2mon.shared.common.serialisation.HardwareAddressSerializer;
import cern.c2mon.shared.daq.messaging.DAQResponse;
import cern.c2mon.shared.daq.messaging.ServerRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * This class handles the serialization of the server requests to the daq as well the serialization of the response form the daq to the server.
 * All objects between server and daq are send as json strings.
 * The serialization is done with the {@link ObjectMapper} from the Jackson library.
 *
 * @author Franz Ritter
 */
@Slf4j
public class MessageConverter {

  public static ObjectMapper mapper = new ObjectMapper();

  static {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(HardwareAddress.class, new HardwareAddressDeserializer());
    module.addSerializer(HardwareAddress.class, new HardwareAddressSerializer());
    mapper.registerModule(module);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
  }

  /**
   * Takes a {@link ServerRequest} object and parses it into a JSON string.
   *
   * @param serverRequest The {@link ServerRequest} object.
   * @param <T>
   * @return The {@link ServerRequest} object in json representation.
   */
  public static <T extends ServerRequest> String requestToJson(T serverRequest) {
    String result = null;
    try {

      result = mapper.writeValueAsString(serverRequest);

    } catch (JsonProcessingException e) {
      log.error("Serializing of " + serverRequest.getClass() + " failed: " + e.getMessage());
    }

    return result;
  }

  /**
   * Takes a {@link DAQResponse} object and parses it into a JSON string.
   *
   * @param daqResponse The {@link DAQResponse} object.
   * @param <T>
   * @return The {@link DAQResponse} object in json representation.
   */
  public static <T extends DAQResponse> String responseToJson(T daqResponse) {
    String result = null;
    try {

      result = mapper.writeValueAsString(daqResponse);

    } catch (JsonProcessingException e) {
      log.error("Serializing of " + daqResponse.getClass() + " failed: " + e.getMessage());
    }

    return result;
  }

  /**
   * Takes a json string and parses it into to a {@link ServerRequest} object.
   *
   * @param tagUpdateJson The json string of the {@link ServerRequest}.
   * @return The parsed java object.
   */
  public static ServerRequest requestFromJson(String tagUpdateJson) {
    ServerRequest result = null;
    try {

      result = mapper.readValue(tagUpdateJson, ServerRequest.class);

    } catch (IOException e) {
      log.warn("Error occurred while converting the json string back to an object: " + e.getMessage());
    }
    return result;
  }

  /**
   * Takes a json string and parses it into to a {@link DAQResponse} object.
   *
   * @param daqResponse The json string of the {@link DAQResponse}.
   * @param clazz       The implementation of the {@link DAQResponse}.
   * @param <T>
   * @return The java object.
   */
  public static <T extends DAQResponse> T responseFromJson(String daqResponse, Class<T> clazz) {
    T result;
    try {

      result = mapper.readValue(daqResponse, clazz);

    } catch (IOException e) {
      log.warn("Error occurred while converting the json string back to an object: " + e.getMessage());
      throw new RuntimeException();
    }
    return result;
  }
}
