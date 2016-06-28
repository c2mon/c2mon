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
package cern.c2mon.server.shorttermlog.mapper;

import java.util.List;

import cern.c2mon.shared.client.command.CommandTagLog;

/**
 * Mybatis mapper interface for logging executed Commands to
 * to the STL (with report details).
 * 
 * @author Mark Brightwell
 *
 */
public interface CommandTagLogMapper extends LoggerMapper<CommandTagLog> {

  /**
   * Returns all command logs for this command.
   * Used for test only, as does *not* return
   * correct timestamp (not adjusted for UTC).
   * 
   * @param id of the command
   * @return a list of log events
   */
  List<CommandTagLog> getCommandTagLog(Long id);
  
  /**
   * Delete all entries for this command.
   * Used for test only.
   * 
   * @param id of the command logs to remove
   */
  void deleteAllLogs(Long id);
  
}
