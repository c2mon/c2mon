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
   * Name of the topic on which the server is publishing alarms
   */
  private String alarmTopic = "c2mon.client.alarm";

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
}
