/*******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
 ******************************************************************************/
package cern.c2mon.shared.common.config;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * This property class is used for Spring property injection for JMS
 * properties shared accross all layers.
 * 
 * @author Matthias Braeger
 */
@Slf4j
@Data
public class CommonJmsProperties {
  
  /**
   * URL of the primary JMS broker to which to publish
   */
  private String url = "failover:tcp://0.0.0.0:61616";
  
  /**
   * User name to authenticate with the broker
   */
  private String username = "";

  /**
   * Password to authenticate with the broker
   */
  private String password = "";
  
  /**
   * Prefix value that is used for generated Connection ID values when a new Connection 
   * is created for the JMS ConnectionFactory. This connection ID is used when logging 
   * some information from the JMS Connection object so a configurable prefix can make 
   * breadcrumbing the logs easier. The default prefix is: <code>ID:c2mon.</code>
   * 
   * @see ActiveMQConnectionFactory#setConnectionIDPrefix(String)
   */
  private String connectionIDPrefix = "ID:c2mon.";
  
  /**
   * Creates a unique client ID string consisting of the user name, host name and process ID.
   * <p/>
   * Here an example: user@hostname[pid]
   * @return the client ID string
   */
  public String getClientIdPrefix() {
    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    
    String hostname = "";
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      log.warn("Couldn't get hostname", e);
    }
    
    StringBuilder clientIdPrefix = new StringBuilder(System.getProperty("user.name"));
    if (!hostname.isEmpty()) {
      clientIdPrefix.append('@');
      clientIdPrefix.append(hostname);
    }
    
    if (!pid.isEmpty()) {
      clientIdPrefix.append('[');
      clientIdPrefix.append(pid);
      clientIdPrefix.append(']');
    }
    
    return clientIdPrefix.toString();
  }
}
