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
package cern.c2mon.shared.client.supervision;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;

/**
 * Heartbeat objects are exchanged between the TIM server and TIM clients. Each
 * of the TIM servers periodically generates a HeartBeat object and distributes
 * it to its clients.
 * 
 * <p>Heartbeat objects are distributed by the HeartbeatManager on a JMS topic
 * in JSON format.
 * 
 * @author Mark Brightwell
 */
public final class Heartbeat {
  
  /**
   * Heartbeat in milliseconds.
   */
  private static final int HEARTBEAT_INTERVAL = 30000;
  
  /**
   * Returns the interval between heartbeat messages sent by 
   * the C2MON server.
   * 
   * @return the heartbeat interval
   */
  public static int getHeartbeatInterval() {
    return HEARTBEAT_INTERVAL;
  }
  
  /**
   * Time when this Heartbeat was created (be careful when using this on
   * badly synchronised machines!)
   */
  private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

  /**
   * Name of the host on which this Heartbeat was created.
   */
  private String hostname;

  /**
   * Constructor. Create a new Heartbeat initialised with the current time and
   * the name of the local host.
   */
  public Heartbeat() {
    try {
      this.hostname = InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException e) {
      this.hostname = "UNKNOWN";
    }
  }

  /**
   * Get the canonical host name of the machine on which this Heartbeat was
   * created.
   * 
   * @return the hostName
   */
  public String getHostName() {
    return this.hostname;
  }

  /**
   * Get the timestamp when this Heartbeat was created.
   * 
   * @return the time this heartbeat was created
   */
  public Timestamp getTimestamp() {
    return this.timestamp;
  }

}
