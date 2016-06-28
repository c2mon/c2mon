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

/**
 * This class represents a set of statistics about a DAQ process, including the
 * current number of configured and invalid tags.
 *
 * @author Justin Lewis Salmon
 */
public class ProcessTagStatistics {

  /**
   * The total number of configured tags for the process.
   */
  Integer total;

  /**
   * The total number of invalid tags for the process.
   */
  Integer invalid;

  /**
   * Constructor.
   *
   * @param total the total number of configured tags for the process
   * @param invalid the total number of invalid tags for the process
   */
  public ProcessTagStatistics(final Integer total, final Integer invalid) {
    this.total = total;
    this.invalid = invalid;
  }

  /**
   * Retrieve the total number of configured tags for the process.
   *
   * @return the number of configured tags
   */
  public Integer getTotal() {
    return total;
  }

  /**
   * Retrieve the total number of invalid tags for the process.
   *
   * @return the number of invalid tags
   */
  public Integer getInvalid() {
    return invalid;
  }
}
