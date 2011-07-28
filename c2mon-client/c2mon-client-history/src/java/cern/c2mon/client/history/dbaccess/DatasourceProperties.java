/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
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
 *****************************************************************************/
package cern.c2mon.client.history.dbaccess;

import java.util.Properties;


/**
 * Fills is the data source properties into a {@link Properties}
 * 
 * @author vdeila
 */
public final class DatasourceProperties {

  /** Singleton instance */
  private static DatasourceProperties instance = null;

  /**
   * @return An instance
   */
  public static DatasourceProperties getInstance() {
    if (instance == null) {
      instance = new DatasourceProperties();
    }
    return instance;
  }
  
  /** The properties file when loaded */
  private final Properties properties;
  
  /**
   * <code>true</code> if the data source is available. <code>false</code> if
   * the settings couldn't be read from the system properties.
   */
  private final boolean datasourceAvailable;
  
  /** The property key for the jdbc snapshot log driver */
  public static final String PROPERTY_JDBC_TIMSTLOG_DRIVER = "jdbc.timstlog.driver";
  
  /** The property key for the jdbc snapshot log url */
  public static final String PROPERTY_JDBC_TIMSTLOG_URL = "jdbc.timstlog.url";
  
  /** The property key for the jdbc snapshot log username */
  public static final String PROPERTY_JDBC_TIMSTLOG_USERNAME = "jdbc.timstlog.user";
  
  /** The property key for the jdbc snapshot log password */
  public static final String PROPERTY_JDBC_TIMSTLOG_PASSWORD = "jdbc.timstlog.password";
  
  /** The default driver used if non is specified */
  private static final String DEFAULT_JDBC_TIMSTLOG_DRIVER = "oracle.jdbc.OracleDriver";
  
  /**
   * Is Singleton and therefore private
   */
  private DatasourceProperties() {    
    // Gets the driver, url, username and password from the system properties
    final Properties properties = new Properties();
    boolean didLoadFully = true;
    try {
      properties.setProperty(PROPERTY_JDBC_TIMSTLOG_DRIVER, System.getProperty(HistorySystemProperties.JDBC_RO_DRIVER, DEFAULT_JDBC_TIMSTLOG_DRIVER));
      properties.setProperty(PROPERTY_JDBC_TIMSTLOG_URL, System.getProperty(HistorySystemProperties.JDBC_RO_URL));
      properties.setProperty(PROPERTY_JDBC_TIMSTLOG_USERNAME, System.getProperty(HistorySystemProperties.JDBC_RO_USERNAME));
      properties.setProperty(PROPERTY_JDBC_TIMSTLOG_PASSWORD, System.getProperty(HistorySystemProperties.JDBC_RO_PASSWORD));
    }
    catch (Exception e) {
      didLoadFully = false;
    }
    
    if (didLoadFully) {
      // Checks if any of the values is null
      for (Object value : properties.values()) {
        if (value == null) {
          didLoadFully = false;
          break;
        }
      }
    }
    
    datasourceAvailable = didLoadFully;
    
    if (didLoadFully) {
      this.properties = properties;
    }
    else {
      this.properties = null;
    }
  }
  
  /**
   * 
   * @return <code>true</code> if the data source is available.
   */
  public boolean isDatasourceAvailable() {
    return this.datasourceAvailable;
  }

  /**
   * Is a reference to the original, it must therefore NOT be changed.
   * 
   * @return The properties, or <code>null</code> if the data source is not available.
   * (<code>isDatasourceAvailable() == false</code>)
   */
  public Properties getProperties() {
    return this.properties;
  }

}
