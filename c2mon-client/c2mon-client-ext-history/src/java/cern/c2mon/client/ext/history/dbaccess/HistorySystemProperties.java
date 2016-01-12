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
package cern.c2mon.client.ext.history.dbaccess;

/**
 * This abstract class contains system property names which are specific to TIM-client
 * and which can be as System Properties.
 * 
 * @author vdeila
 *
 */
public final class HistorySystemProperties {
  
  /** The jdbc read only url. */
  public static final String JDBC_RO_URL = "c2mon.jdbc.ro.url";
  
  /** The jdbc read only driver. */
  public static final String JDBC_DRIVER = "c2mon.jdbc.driver";
  
  /** The jdbc read only username */
  public static final String JDBC_RO_USERNAME = "c2mon.jdbc.ro.user";
  
  /** The jdbc read only password */
  public static final String JDBC_RO_PASSWORD = "c2mon.jdbc.ro.password";
  
  
  /** Private constructor, no instance is necessary */
  private HistorySystemProperties() {
    // Nothing here
  }
}
