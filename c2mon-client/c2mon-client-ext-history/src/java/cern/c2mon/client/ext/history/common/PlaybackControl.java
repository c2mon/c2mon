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
package cern.c2mon.client.ext.history.common;

import cern.c2mon.client.ext.history.common.event.PlaybackControlListener;

/**
 * This interface describes the methods that is used to control the history
 * playback. For example {@link #pause()}, {@link #resume()}, etc.
 * 
 * @author vdeila
 * 
 */
public interface PlaybackControl {

  /**
   * Starts or resumes the playback
   */
  void resume();

  /**
   * Stops the playback
   */
  void pause();

  /**
   * 
   * @return <code>true</code> if the history player is playing.
   *         <code>false</code> if it is not paused, stopped or not initialized.
   */
  boolean isPlaying();

  /**
   * 
   * @return The multiplier of the time
   */
  double getPlaybackSpeed();

  /**
   * Changes the speed of the playback
   * 
   * @param multiplier
   *          The multiplier to set for the time
   */
  void setPlaybackSpeed(final double multiplier);

  /**
   * 
   * @param time
   *          The new time to set
   */
  void setClockTime(final long time);

  /**
   * 
   * @return the current time of the history clock
   */
  long getClockTime();
  
  /**
   * 
   * @param listener The listener to add
   */
  void addPlaybackControlListener(final PlaybackControlListener listener);
  
  /**
   * 
   * @param listener The listener to remove
   */
  void removePlaybackControlListener(final PlaybackControlListener listener);

}
