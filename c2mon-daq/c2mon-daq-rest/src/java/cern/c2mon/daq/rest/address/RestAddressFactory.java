/*
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
 */
package cern.c2mon.daq.rest.address;

import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;

import java.util.Map;

/**
 * This factory class is used to create HardwareAddress instances out of a Map<String, String>.
 * There are two kind of HardwareAddresses which can be created in this module:
 * <p/>
 * {@link RestGetAddress}: Pojo class which holds all information providing a GET functionality for a DataTag
 * <p/>
 * {@link RestPostAddress}: Pojo class which holds all information providing a POST functionality for a DataTag.
 * <p/>
 * To determine which HardwareAddress should be created the map needs to set the entry 'mode' to 'POST' or 'GET'.
 *
 * @author Franz Ritter
 */
public abstract class RestAddressFactory {

  private final static int DEFAULT_GET_FREQUENCY = 30 * 1000;

  private final static int DEFAULT_POST_FREQUENCY = 30 * 1000;

  private final static double POST_FREQUENCY_TOLERANCE = 1.5;

  public enum RestMode {
    GET, POST;
  }

  /**
   * Factory method for creating a HardwareAddress out of a map.
   * The map holds all information for creating the object.
   * There are two kind od HardwareAddresses which can be created out of the information of the map:
   * <p/>
   * {@link RestGetAddress}: Pojo class which holds all infromation providing a GET functionality for a DataTag.
   * Fields to set: (mode, GET), (url, "the url for the get request"), (getFrequency, "the request frequency"), (jsonPathExpression, "jsonPath expression for a json message")
   * <p/>
   * {@link RestPostAddress}: Pojo class which holds all infromation providing a POST functionality for a DataTag.
   * Fields to set: (mode, POST), (postFrequency, "the expected post frequency")
   * </p>
   *
   * @param addressParameters A map which all infroation for creating the HardwareAddress
   * @return The created HardwareAddress.
   */
  public static HardwareAddress createHardwareAddress(Map<String, String> addressParameters) {

    RestMode mode = extractMode(addressParameters);

    if (mode == RestMode.GET) {
      // extract fields for the request mode

      String url = extractUrl(addressParameters);
      Integer frequency = extractGetFrequency(addressParameters);
      String jsonPathExpression = extractJsonPath(addressParameters);

      return new RestGetAddress(url, frequency, jsonPathExpression);

    } else if (mode == RestMode.POST) {
      // extract fields for the receive mode

      Integer frequency = extractPostFrequency(addressParameters);

      return new RestPostAddress(frequency);

    } else {
      throw new IllegalArgumentException("Parsing of the HardwareAddress failed. Mode not specified.");
    }
  }

  /**
   * Parses the mode of the address from the properties map.
   *
   * @param properties the map which contains the mode information.
   * @return the rest url.
   */
  private static RestMode extractMode(Map<String, String> properties) {

    RestMode value;

    switch (properties.get("mode").toUpperCase()) {
      case "GET":
        value = RestMode.GET;
        break;
      case "POST":
        value = RestMode.POST;
        break;
      default:
        throw new IllegalArgumentException("HardwareAddress invalid. \"mode\" - " + properties.get("mode") + " unknown.");
    }

    return value;

  }

  /**
   * Parses the url from the properties map.
   *
   * @param properties the map which contains the url information.
   * @return the rest url.
   */
  private static String extractUrl(Map<String, String> properties) {

    String value;

    value = properties.get("url");

    if (value == null) {
      throw new IllegalArgumentException("HardwareAddress invalid. \"url\" not specified.");
    }

    return value;

  }

  /**
   * Parses the request interval from the properties map.
   *
   * @param properties the map which contains the url information.
   * @return the rest url.
   */
  private static Integer extractGetFrequency(Map<String, String> properties) {

    String value;

    value = properties.get("getFrequency");

    if (value == null) {
      return DEFAULT_GET_FREQUENCY;
    }

    // the interval is given in seconds so it must be changed to millisecondss
    return Integer.parseInt(value) * 1000;

  }

  /**
   * Parses the request interval from the properties map.
   *
   * @param properties the map which contains the url information.
   * @return the rest url.
   */
  private static Integer extractPostFrequency(Map<String, String> properties) {

    String value;

    value = properties.get("postFrequency");

    if (value != null) {
      return (int) (SourceDataTag.getAddressParameter(properties, "postFrequency", Integer.class) * POST_FREQUENCY_TOLERANCE) * 1000;
    } else {
      return null;
    }
  }

  /**
   * Parses the pattern from the properties map.
   * The pattern don't have to be specified and the method can return null without throwing an exception.
   *
   * @param properties the map which contains the url information.
   * @return the rest url.
   */
  private static String extractJsonPath(Map<String, String> properties) {

    String value;

    value = properties.get("jsonPathExpression");

    return value;

  }


}
