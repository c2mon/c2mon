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

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.PlaybackControl;
import cern.c2mon.client.common.history.event.PlaybackControlListener;
import cern.c2mon.client.history.playback.HistoryPlayerImpl;
import cern.c2mon.client.history.playback.HistoryPlayerInternal;
import cern.c2mon.client.history.playback.PlaybackSynchronizeControl;
import cern.c2mon.client.history.playback.components.ListenersManager;

/**
 * This class controls the {@link HistoryPlayerImpl} class.
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

  /** The history player it is controlling */
  private final HistoryPlayerInternal historyPlayer;
  
  /** A manager which keeps track of the listeners */
  private final ListenersManager<PlaybackControlListener> listenersManager = new ListenersManager<PlaybackControlListener>();

  /**
   * 
   * @param historyPlayer The history player to control 
   */
  public PlaybackControlImpl(final HistoryPlayerInternal historyPlayer) {
    this.historyPlayer = historyPlayer;
  }

  @Override
  public boolean isPlaying() {
    return this.historyPlayer.getClock().isRunning();
  }

  @Override
  public void resume() {
    if (!this.historyPlayer.getClock().isRunning()) {
      this.historyPlayer.getClock().start();
      firePlaybackStarted();
    }
  }

  @Override
  public void pause() {
    this.historyPlayer.getClockSynchronizer().interruptBehindScheduleThread();
    this.historyPlayer.getClock().pause();
    firePlaybackStopped();
  }

  @Override
  public void setClockTime(final long time) {
    this.historyPlayer.getClock().setTime(time);
    fireClockTimeSet(time);
  }

  @Override
  public long getClockTime() {
    return this.historyPlayer.getClock().getTime();
  }

  @Override
  public double getPlaybackSpeed() {
    return this.historyPlayer.getClock().getSpeedMultiplier();
  }

  @Override
  public void setPlaybackSpeed(final double multiplier) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Sets speed to %.1f", multiplier));
    }

    // Sets the new speed
    this.historyPlayer.getClock().setSpeedMultiplier(multiplier);

    // Notifies listeners
    fireClockPlaybackSpeedChanged(multiplier);

  }

  enum PlaybackStatus {
    PLAYING, PAUSED, PLAYING_DISABLED, PAUSED_DISABLED
  };

  private PlaybackStatus playbackStatus = PlaybackStatus.PAUSED;

  @Override
  public void disablePlayback() {
    switch (playbackStatus) {
    case PLAYING:
      playbackStatus = PlaybackStatus.PLAYING_DISABLED;
      this.historyPlayer.getClock().pause();
      break;
    case PAUSED:
      playbackStatus = PlaybackStatus.PAUSED_DISABLED;
      break;
    default:
      break;
    }
  }

  @Override
  public void enablePlayback() {
    switch (playbackStatus) {
    case PLAYING_DISABLED:
      playbackStatus = PlaybackStatus.PLAYING;
      this.historyPlayer.getClock().start();
      break;
    case PAUSED_DISABLED:
      playbackStatus = PlaybackStatus.PAUSED;
      break;
    default:
      break;
    }

  }
  
  /**
   * 
   * @param listener The listener to add
   */
  @Override
  public void addPlaybackControlListener(final PlaybackControlListener listener) {
    this.listenersManager.add(listener);
  }
  
  /**
   * 
   * @param listener The listener to remove
   */
  @Override
  public void removePlaybackControlListener(final PlaybackControlListener listener) {
    this.listenersManager.remove(listener);
  }

  /**
   * Fires the playbackStarted() event
   */
  protected void firePlaybackStarted() {
    for (final PlaybackControlListener listener : listenersManager.getAll()) {
      listener.onPlaybackStarted();
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
   * @param newMultiplier The new multiplier to tell to the listeners
   */
  protected void fireClockPlaybackSpeedChanged(final double newMultiplier) {
    for (final PlaybackControlListener listener : listenersManager.getAll()) {
      listener.onClockPlaybackSpeedChanged(newMultiplier);
    }
  }
  
  /**
   * Fires the clockTimeSet(newTime) event
   * 
   * @param newTime The new time to tell the listeners
   */
  protected void fireClockTimeSet(final long newTime) {
    for (final PlaybackControlListener listener : listenersManager.getAll()) {
      listener.onClockTimeSet(newTime);
    }
  }
  
}
