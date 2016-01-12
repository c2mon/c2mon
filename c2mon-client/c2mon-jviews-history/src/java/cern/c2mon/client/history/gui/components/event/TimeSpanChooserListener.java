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
package cern.c2mon.client.history.gui.components.event;

import java.util.Date;

import cern.c2mon.client.history.gui.components.TimeSpanChooser;

/**
 * Interface to notify about events
 * 
 * @see TimeSpanChooser
 * 
 * @author vdeila
 */
public interface TimeSpanChooserListener {

  /**
   * Invoked when a new start date is selected
   * 
   * @param newStartDate
   *          the new selected start date
   */
  void onSelectedStartDateChanged(final Date newStartDate);

  /**
   * Invoked when a new end date is selected
   * 
   * @param newEndDate
   *          the new selected end date
   */
  void onSelectedEndDateChanged(final Date newEndDate);

}
