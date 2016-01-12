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
package cern.c2mon.client.ext.history.common.event;

/**
 * Is used by the {@link PlaybackControl} to inform about events
 * 
 * @author vdeila
 *
 */
public interface PlaybackControlListener {

  /**
   * Invoked when the playback is starting
   */
  void onPlaybackStarting();

  /**
   * Invoked when the playback is stopped or paused
   */
  void onPlaybackStopped();
  
  /**
   * Invoked when the clock is going to be manually changed, is NOT called
   * continuously when the playback is running
   * 
   * @param newTime
   *          The new time that it is being set to.
   */
  void onClockTimeChanging(final long newTime);

  /**
   * Invoked when the clock have been manually changed, is NOT called
   * continuously when the playback is running
   * 
   * @param newTime
   *          The new time that it have been set to
   */
  void onClockTimeChanged(final long newTime);

  /**
   * Invoked when the playback speed is changed
   * 
   * @param newMultiplier
   *          The new multiplier that is set
   */
  void onClockPlaybackSpeedChanged(final double newMultiplier);
}
