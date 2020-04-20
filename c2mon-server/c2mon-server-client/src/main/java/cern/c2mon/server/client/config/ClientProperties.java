/******************************************************************************
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
 *****************************************************************************/
package cern.c2mon.server.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

import cern.c2mon.shared.client.config.ClientJmsProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.client")
public class ClientProperties {

  /**
   * JMS properties
   */
  private Jms jms = new Jms();

  @Data
  public class Jms extends ClientJmsProperties {

    /**
     * The topic prefix used to publish data tags and rules to the client. The
     * process ID will be appended.
     */
    private String tagTopicPrefix = "c2mon.client.tag";
    
    /**
     * Set the time-to-live in seconds for all client messages that are distributed via JMS topics
     * Default is 60 sec
     */
    private int clientTopicMsgTimeToLive = 60;

    /** Specify the initial number of concurrent consumers to receive client requests */
    private int initialConsumers = 5;

    /** Specify the maximum number of concurrent consumers to receive client requests */
    private int maxConsumers = 10;
  }
}
