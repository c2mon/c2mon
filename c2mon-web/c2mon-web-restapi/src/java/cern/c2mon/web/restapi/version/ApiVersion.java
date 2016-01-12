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
package cern.c2mon.web.restapi.version;

/**
 * Simple bean used to keep track of progressive versions of the C2MON REST API.
 *
 * The version numbers are used in conjunction with the Spring MVC request
 * mappings to determine which response to send to the client. By the exchange
 * of HTTP "Content-Type" and "Accepts" headers, the client can request a
 * specific API version and the server can respond with the corresponding
 * version. A version number is a plain integer with no decimal part.
 *
 * @author Justin Lewis Salmon
 */
public class ApiVersion {

  /**
   * HTTP header string for version 1.
   */
  public static final String API_V1 = "application/json; version=c2mon.web.restapi.v1";
}
