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
package cern.c2mon.shared.common.command;

import java.sql.Timestamp;

/**
 * Contains details of a command execution. Only
 * to be used on the server.
 * 
 * @param <T> the type of the command value
 * @author Mark Brightwell
 *
 */
public class CommandExecutionDetails<T> {

  /**
   * Object value set for command execution.
   */
  private T value;
  
  /**
   * Start time of execution on server.
   */
  private Timestamp executionStartTime;
  
  /**
   * Time when response received from DAQ.
   */
  private Timestamp executionEndTime;
  
  /**
   * User requesting the execution.
   */
  private String username;
  
  /**
   * Host from which the execution was made.
   */
  private String host;

  /**
   * @return the value
   */
  public T getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final T value) {
    this.value = value;
  }

  /**
   * @return the executionStartTime
   */
  public Timestamp getExecutionStartTime() {
    return executionStartTime;
  }

  /**
   * @param executionStartTime the executionStartTime to set
   */
  public void setExecutionStartTime(final Timestamp executionStartTime) {
    this.executionStartTime = executionStartTime;
  }

  /**
   * @return the executionEndTime
   */
  public Timestamp getExecutionEndTime() {
    return executionEndTime;
  }

  /**
   * @param executionEndTime the executionEndTime to set
   */
  public void setExecutionEndTime(final Timestamp executionEndTime) {
    this.executionEndTime = executionEndTime;
  }

  /**
   * @return User requesting the execution
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username the user requesting the execution
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return the host from which the execution was made
   */
  public String getHost() {
    return host;
  }

  /**
   * @param host the host from which the execution was made
   */
  public void setHost(String host) {
    this.host = host;
  }
}

