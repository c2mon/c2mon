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
package cern.c2mon.client.common.util;

import java.io.IOException;


import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class.
 * Provides a method that parses a String
 * and returns an RbacAuthorizationDetails object.
 * @author ekoufaki
 */
public final class RbacAuthorizationDetailsParser {

  /** Private Constructor! */
  private RbacAuthorizationDetailsParser() {
  }

  /**
   * RbacAuthorizationDetailsParser logger
   * */
  private static Logger logger = LoggerFactory.getLogger(RbacAuthorizationDetailsParser.class);

  /**
   * RbacAuthorizationDetails are provided as one string for convenience (instead of 3).
   * Given that string this method returns an RbacAuthorizationDetails Object.
   * @param encodedDetails a String that contains the RbacAuthorizationDetails.
   * The details should be provided as 3 comma seperated strings in the following order: "Class,Device,Property"
   * Example: "TIM_APPLICATIONS,TIM_WEBCONFIG,RUN"
   * @return an RbacAuthorizationDetails Object
   * @throws IOException In case the encodedDetails are null or encoded in a non-supported format.
   */
  public static RbacAuthorizationDetails parseRbacDetails(final String encodedDetails) throws IOException {

    if (encodedDetails == null)  {
      logger.error(new Error("parseRbacDetails(): RbacAuthorizationDetails == null!").toString());
      throw new IOException("Not able to fetch RbacAuthorizationDetails.");
    }

    String[] splitedDetails = encodedDetails.replace(" ", "").split( ",\\s*" ); // split on commas
    RbacAuthorizationDetails authDetails = null;

    if (splitedDetails.length != 3) { // RbacAuthorizationDetails should be provided as 3 comma seperated strings
      logger.error(new Error("parseRbacDetails(): error splitting details!:"
          + encodedDetails
      ).toString());
      throw new IOException("Not able to fetch RbacAuthorizationDetails.");
    }
    else {

      authDetails = new RbacAuthorizationDetails();
      authDetails.setRbacClass(splitedDetails[0]);
      authDetails.setRbacDevice(splitedDetails[1]);
      authDetails.setRbacProperty(splitedDetails[2]);
    }

    return authDetails;
  }
}
