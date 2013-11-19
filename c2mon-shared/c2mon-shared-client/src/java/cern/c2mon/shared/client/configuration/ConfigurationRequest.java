/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.client.configuration;

/**
 * A request to apply a configuration with a given id.
 * Is encoded into XML and sent from a client to the
 * server, which applied the configuration and returns
 * a response.
 * 
 * @author Mark Brightwell
 *
 */
public class ConfigurationRequest {

  /**
   * Id of the configuration to apply.
   */
  private int configId;  
  
  /**
   * User session information.
   */
  private String sessionId;
  
  /**
   * No argument constructor.
   */
  public ConfigurationRequest() { }
  
  /**
   * Constructor.
   * @param configId the unique id of the configuration
   */
  public ConfigurationRequest(final int configId) {
    super();
    this.configId = configId;
  }

  /**
   * Getter.
   * @return the configId
   */
  public int getConfigId() {
    return configId;
  }

  /**
   * Setter method.
   * @param configId the configId to set
   */
  public void setConfigId(final int configId) {
    this.configId = configId;
  }

  /**
   * Setter method.
   * @param sessionId the session id
   */
  public void setSessionId(final String sessionId) {
    this.sessionId = sessionId;
  }

  /**
   * Getter method.
   * @return the session id
   */
  public String getSessionId() {
    return sessionId;
  }


  
}
