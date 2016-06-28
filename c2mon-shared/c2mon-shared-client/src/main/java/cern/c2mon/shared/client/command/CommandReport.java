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
package cern.c2mon.shared.client.command;


import java.sql.Timestamp;

import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.common.datatag.DataTagConstants;


/**
 * The CommandReport
 * <p>
 * The CommandReport class implements the java.io.Serializable interface because
 * CommandReport objects are sent to client applications as JMS ObjectMessages. 
 * <p>
 * Remark: switched MODE constants from TagMode to DataTagConstants TODO may want to move these to .tag package if used for many tag types
 *
 * @author Jan Stowisek, Matthias Braeger
 */

public interface CommandReport extends ClientRequestResult {  
  /**
   * @return the unique identifier of the CommandTag concerned by this report
   */
  Long getId();
  
  /**
   * @return the execution status.
   */
  CommandExecutionStatus getStatus();

  /**
   * @return The status description or "UNKNOWN"
   * @see CommandExecutionStatus#getDescription()
   */
  String getStatusText();

  /**
   * @return The free-text description of the execution status
   *         (if it has been set)
   */
  String getReportText();

  
  /**
   * @return The value as returned by the equipment on execution. This is only set if
   * the execution was successful.
   */
  String getReturnValue();

  
  /**
   * @return the timestamp of the command execution on the DAQ layer
   */
  Timestamp getTimestamp();

  /**
   * MODE_OPERATIONAL = 0; <br>
   * MODE_MAINTENANCE = 1; <br>
   * MODE_TEST = 2;
   * @return Either 0, 1 or 2
   * @see DataTagConstants#MODE_MAINTENANCE
   * @see DataTagConstants#MODE_OPERATIONAL
   * @see DataTagConstants#MODE_TEST
   */
  short getMode();

  /**
   * @return <code>true</code>, in case the command status is set to
   *         {@link CommandExecutionStatus#STATUS_OK}
   */
  boolean isOK();
}
