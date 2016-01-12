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
package cern.c2mon.shared.daq.process;

/**
 * Message Type enum
 * 
 * @author vilches
 */
public enum ProcessMessageType {
  /**
   * Process type ProcessConnectionRequest
   */
  CONNECT_REQUEST("process-connection-request"), 

  /**
   * Process type ProcessConnectionResponse
   */
  CONNECT_RESPONSE("process-connection-response"), 

  /**
   * Process type ProcessConfigurationRequest
   */
  CONFIG_REQUEST("process-configuration-request"),

  /**
   * Process type ProcessConfigurationResponse
   */
  CONFIG_RESPONSE("process-configuration-response"),

  /**
   * Process type ProcessDisconnectionRequest
   */
  DISCONNETION_REQUEST("process-disconnection-request");
  
  /**
   * Test name
   */
  private String name;

  /**
   * Set message type name (root XML name)
   * 
   * @param name Message type name (root XML name)
   */
  ProcessMessageType(final String name) {
    this.name = name;
  }

  /**
   * Get message type name (root XML name)
   * 
   * @return Message type name (root XML name)
   */
  public final String getName() {
    return this.name;
  }
}
