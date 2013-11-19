/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2013 CERN.
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
