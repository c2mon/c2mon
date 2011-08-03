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
package cern.c2mon.client.history.playback.player;

import java.util.Date;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.PlaybackControl;
import cern.c2mon.client.common.history.event.PlaybackControlListener;
import cern.c2mon.client.history.playback.HistoryPlayerImpl;
import cern.c2mon.client.history.playback.PlaybackSynchronizeControl;
import cern.c2mon.client.history.playback.components.Clock;
import cern.c2mon.client.history.playback.components.ListenersManager;

/**
 * This class controls the {@link Clock} of the playback
 * 
 * @see PlaybackControl
 * @see PlaybackSynchronizeControl
 * 
 * @author vdeila
 * 
 */
public class PlaybackControlImpl implements PlaybackControl, PlaybackSynchronizeControl {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryPlayerImpl.class);

  /** A manager which keeps track of the listeners */
  private final ListenersManager<PlaybackControlListener> listenersManager = new ListenersManager<PlaybackControlListener>();

  /** The clock the history player is based on */
  private Clock clock;

  /** The current status of the playback */
  private PlaybackStatus playbackStatus = PlaybackStatus.PAUSED;
  
  /** The possible statuses of the playback */
  private enum PlaybackStatus {
    /** Playing */
    PLAYING,
    /** Paused */
    PAUSED,
    /** Were playing, but playback is disabled */
    PLAYING_DISABLED,
    /** Were paused, and playback is disabled */
    PAUSED_DISABLED
  };
  
  /**
   * Creates a new instance
   */
  public PlaybackControlImpl() {
    // Creates a default clock
    this.clock = new Clock(new Date(), new Date());
  }

  @Override
  public boolean isPlaying() {
    return isClockRunning();
  }

  @Override
  public void resume() {
    if (!isClockRunning()) {
      firePlaybackStarting();
      resumeClock();
    }
  }

  @Override
  public void pause() {
    pauseClock();
    firePlaybackStopped();
  }

  @Override
  public void setClockTime(final long time) {
    getClock().setTime(time);
    fireClockTimeSet(time);
  }

  @Override
  public long getClockTime() {
    return getClock().getTime();
  }

  @Override
  public double getPlaybackSpeed() {
    return getClock().getSpeedMultiplier();
  }

  @Override
  public void setPlaybackSpeed(final double multiplier) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Sets speed to %.1f", multiplier));
    }

    // Sets the new speed
    getClock().setSpeedMultiplier(multiplier);

    // Notifies listeners
    fireClockPlaybackSpeedChanged(multiplier);

  }

  @Override
  public synchronized void disablePlayback() {
    switch (playbackStatus) {
    case PLAYING:
      playbackStatus = PlaybackStatus.PLAYING_DISABLED;
      getClock().pause();
      break;
    case PAUSED:
      playbackStatus = PlaybackStatus.PAUSED_DISABLED;
      break;
    default:
      break;
    }
  }

  @Override
  public synchronized void enablePlayback() {
    switch (playbackStatus) {
    case PLAYING_DISABLED:
      playbackStatus = PlaybackStatus.PLAYING;
      getClock().start();
      break;
    case PAUSED_DISABLED:
      playbackStatus = PlaybackStatus.PAUSED;
      break;
    default:
      break;
    }
  }

  /** Pauses the clock */
  private synchronized void pauseClock() {
    switch (playbackStatus) {
    case PLAYING_DISABLED:
      playbackStatus = PlaybackStatus.PAUSED_DISABLED;
      break;
    case PLAYING:
      playbackStatus = PlaybackStatus.PAUSED;
      getClock().pause();
      break;
    default:
      break;
    }
  }

  /** Resumes the clock */
  private synchronized void resumeClock() {
    switch (playbackStatus) {
    case PAUSED_DISABLED:
      playbackStatus = PlaybackStatus.PLAYING_DISABLED;
      break;
    case PAUSED:
      playbackStatus = PlaybackStatus.PLAYING;
      getClock().start();
      break;
    default:
      break;
    }
  }

  /**
   * 
   * @return <code>true</code> if the player is running
   */
  private synchronized boolean isClockRunning() {
    switch (playbackStatus) {
    case PAUSED_DISABLED:
    case PAUSED:
      return false;
    case PLAYING_DISABLED:
    case PLAYING:
      if (getClock().hasReachedEndTime()) {
        pause();
        return false;
      }
      else {
        return true;
      }
    default:
      break;
    }
    throw new java.lang.IllegalStateException("The current playback status is unsupported");
  }

  /**
   * 
   * @param listener
   *          The listener to add
   */
  @Override
  public void addPlaybackControlListener(final PlaybackControlListener listener) {
    this.listenersManager.add(listener);
  }

  /**
   * 
   * @param listener
   *          The listener to remove
   */
  @Override
  public void removePlaybackControlListener(final PlaybackControlListener listener) {
    this.listenersManager.remove(listener);
  }

  /**
   * Fires the {@link PlaybackControlListener#onPlaybackStarting()} event
   */
  protected void firePlaybackStarting() {
    for (final PlaybackControlListener listener : listenersManager.getAll()) {
      listener.onPlaybackStarting();
    }
  }

  /**
   * Fires the playbackStopped() event
   */
  protected void firePlaybackStopped() {
    for (final PlaybackControlListener listener : listenersManager.getAll()) {
      listener.onPlaybackStopped();
    }
  }

  /**
   * Fires the clockPlaybackSpeedChanged(newTime) event
   * 
   * @param newMultiplier
   *          The new multiplier to tell to the listeners
   */
  protected void fireClockPlaybackSpeedChanged(final double newMultiplier) {
    for (final PlaybackControlListener listener : listenersManager.getAll()) {
      listener.onClockPlaybackSpeedChanged(newMultiplier);
    }
  }

  /**
   * Fires the clockTimeSet(newTime) event
   * 
   * @param newTime
   *          The new time to tell the listeners
   */
  protected void fireClockTimeSet(final long newTime) {
    for (final PlaybackControlListener listener : listenersManager.getAll()) {
      listener.onClockTimeSet(newTime);
    }
  }

  /**
   * 
   * @return the clock
   */
  private synchronized Clock getClock() {
    return this.clock;
  }

  /**
   * Sets a new start and end time for the clock
   * 
   * @param start
   *          the start time to set
   * @param end
   *          the end time to set
   */
  public synchronized void setClockTimespan(final Date start, final Date end) {
    if (!getClock().getStartDate().equals(start)) {
      getClock().setStartTime(start.getTime());
    }
    if (!getClock().getEndDate().equals(end)) {
      getClock().setEndTime(end.getTime());
    }
  }

}
