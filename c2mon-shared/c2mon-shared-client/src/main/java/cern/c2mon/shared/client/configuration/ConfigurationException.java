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
package cern.c2mon.shared.client.configuration;

/**
 * An exception occuring during server runtime reconfiguration.
 * Is thrown by the "create", "update" and "remove" methods
 * in the Configuration module.
 * 
 * @author Mark Brightwell
 *
 */
public class ConfigurationException extends RuntimeException {
  
  /**
   * Serial UID.
   */
  private static final long serialVersionUID = 3979140248444789949L;
  
  /**
   * Wrapped report for sending to the client. 
   */
  private final ConfigurationReport configurationReport;

  /**
   * Constructor.
   * @param configurationReport attached report
   * @param throwable wrapped exception
   */
  public ConfigurationException(final ConfigurationReport configurationReport, final Throwable throwable) {    
    super(throwable);
    this.configurationReport = configurationReport;
  }

  /**
   * Getter method.
   * @return the configurationReport
   */
  public ConfigurationReport getConfigurationReport() {
    return configurationReport;
  }
  
}
