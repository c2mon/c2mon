/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
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
