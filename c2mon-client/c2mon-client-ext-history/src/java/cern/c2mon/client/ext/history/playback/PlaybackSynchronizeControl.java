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
package cern.c2mon.client.ext.history.playback;

import cern.c2mon.client.ext.history.common.PlaybackControl;
import cern.c2mon.client.ext.history.playback.schedule.ClockSynchronizer;

/**
 * This interface describes methods to be able to disable the playback for a
 * while, and then enable it again. This is used by
 * {@link ClockSynchronizer} to make the clock synchronized with
 * the updated tags.
 * 
 * @see ClockSynchronizer
 * 
 * @author vdeila
 * 
 */
public interface PlaybackSynchronizeControl extends PlaybackControl {

  /**
   * Disables the playback, ie. pauses it if it is already playing
   */
  void disablePlayback();

  /**
   * Enables the playback, ie. resumes it if it is in a "playing" state
   */
  void enablePlayback();

}
