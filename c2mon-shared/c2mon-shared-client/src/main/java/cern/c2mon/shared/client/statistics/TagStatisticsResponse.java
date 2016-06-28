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
package cern.c2mon.shared.client.statistics;

import java.util.Map;

import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * This class represents a set of statistics about the server, including the
 * current number of configured and invalid tags for the server itself and for
 * each DAQ process.
 *
 * @author Justin Lewis Salmon
 */
public interface TagStatisticsResponse extends ClientRequestResult {

  /**
   * Retrieve the total number of configured tags in the server.
   *
   * @return the number of configured tags
   */
  Integer getTotal();

  /**
   * Retrieve the total number of invalid tags in the server.
   *
   * @return the number of invalid tags
   */
  Integer getInvalid();

  /**
   * Retrieve the map of individual process tag statistics.
   *
   * @return the map of process statistics
   */
  Map<String, ProcessTagStatistics> getProcesses();
}
