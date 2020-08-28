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
package cern.c2mon.shared.client.config;

import lombok.Data;

import cern.c2mon.shared.common.config.CommonJmsProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
public class ClientJmsProperties extends CommonJmsProperties{

  /**
   * Name of the topic on which the server is publishing supervision events
   */
  private String supervisionTopic = "c2mon.client.supervision";

  /**
   * Name of the topic on which the server is publishing its heartbeat
   */
  private String heartbeatTopic = "c2mon.client.heartbeat";
  
  /**
   * The broadcast topic channel to which the messages shall be sent
   */
  private String broadcastTopic = "c2mon.client.broadcastmessage";

  /**
   * Name of the topic on which the server is publishing {@link AlarmValue} objects as JSON string
   */
  private String alarmTopic = "c2mon.client.alarm";

  
  /**
   * Name of the topic on which the server is publishing the full {@link TransferTagImpl} object, 
   * including the nested {@link AlarmValue} objects that have changed.
   */
  private String alarmWithTagTopic = "c2mon.client.tagWithAlarmsTopic";

  /**
   * Topic on which all control tags are published
   */
  private String controlTagTopic = "c2mon.client.controltag";

  /**
   * Name of the queue on which to make normal requests to the server
   */
  private String requestQueue = "c2mon.client.request";

  /**
   * Name of the queue on which to make admin requests to the server
   */
  private String adminRequestQueue = "c2mon.client.admin";

  /**
   * Name of the queue on which to make configuration requests to the server
   */
  private String configRequestQueue = "c2mon.client.config";
  
  /**
   * Timeout of client request in milliseconds. 
   */
  private int requestTimeout = 10_000;
}
