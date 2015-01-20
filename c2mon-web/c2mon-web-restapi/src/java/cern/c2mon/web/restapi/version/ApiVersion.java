/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
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
