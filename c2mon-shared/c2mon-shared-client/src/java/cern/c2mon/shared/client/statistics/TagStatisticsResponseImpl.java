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

/**
 * Implementation of {@link TagStatisticsResponse}.
 *
 * @author Justin Lewis Salmon
 */
public class TagStatisticsResponseImpl implements TagStatisticsResponse {

  /**
   * The total number of configured tags on the server.
   */
  private Integer total;

  /**
   * The total number of invalid tags on the server.
   */
  private Integer invalid;

  /**
   * The map of process names to individual process statistics objects.
   */
  private Map<String, ProcessTagStatistics> processes;

  /**
   * Constructor.
   *
   * @param total the total number of configured tags on the server
   * @param invalid the total number of invalid tags on the server
   * @param processes the map of process names to individual process statistics
   *          objects
   */
  public TagStatisticsResponseImpl(final Integer total, final Integer invalid, final Map<String, ProcessTagStatistics> processes) {
    this.total = total;
    this.invalid = invalid;
    this.processes = processes;
  }

  /**
   * No-arg constructor, required for serialisation
   */
  public TagStatisticsResponseImpl() {
  }

  @Override
  public Integer getTotal() {
    return total;
  }

  @Override
  public Integer getInvalid() {
    return invalid;
  }

  @Override
  public Map<String, ProcessTagStatistics> getProcesses() {
    return processes;
  }
}
