/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
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
 *****************************************************************************/
package cern.c2mon.client.common.history;

import cern.c2mon.client.common.history.event.PlaybackControlListener;

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
