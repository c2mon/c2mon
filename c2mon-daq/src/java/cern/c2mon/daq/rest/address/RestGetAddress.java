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

import cern.c2mon.shared.common.datatag.address.impl.HardwareAddressImpl;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Franz Ritter
 */

/**
 * This class holds all information of the GetHardwareAddress.
 * The information of the GetHardwareAddress is needed for sending get queries to web services.
 * To do that the DAQ needs for each dataTag the url for the get request as well the frequency
 * in which time the request is done.
 * An additional parameter is jsonPath expression which tells the module which part of the
 * received get message should be send to the Sever.
 * </p>
 * To create an instance of this class the use factory method of {@link RestAddressFactory}.
 */
@Getter
@Setter
public class RestGetAddress extends HardwareAddressImpl {

  /**
   * The url of the webservice to make the get request.
   */
  private String url;

  /**
   * The frequency in which time interval the request should be done.
   */
  private Integer frequency;

  /**
   * An optional parameter which defines the jsonPath expression for the received json message.
   * JsonPath helps to extract data from a json message.
   */
  private String jsonPathExpression;

  /**
   * Constructor which is used by the {@link RestAddressFactory} to create an instance of this class.
   *
   * @param url The url for the get request.
   * @param frequency The frequency for the get request.
   * @param jsonPathExpression The JsonPath expression for extracting data of the get request.
   */
  protected RestGetAddress(String url, Integer frequency, String jsonPathExpression){

    super();
    this.url = url;
    this.frequency = frequency;
    this.jsonPathExpression = jsonPathExpression;
  }

}
