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
package cern.c2mon.client.ext.history.playback.schedule;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import cern.c2mon.client.ext.history.common.HistoryUpdate;
import cern.c2mon.client.ext.history.common.event.PlaybackControlAdapter;
import cern.c2mon.client.ext.history.common.id.HistoryUpdateId;
import cern.c2mon.client.ext.history.playback.HistoryPlayerImpl;
import cern.c2mon.client.ext.history.playback.HistoryPlayerInternal;
import cern.c2mon.client.ext.history.util.HistoryGroup;

/**
 * 
 * This class schedules the tag value updates.
 * 
 * @author vdeila
 * 
 */
public class HistoryScheduler {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryScheduler.class);

  /** The amount of milliseconds that it can be behind schedule (in real time) */
  public static final long BEHIND_SCHEDULE_THRESHOLD = 200;

  /** A timer to schedule data tag update events */
  private TimerQueue timer;

  /** A callback for the {@link #timer} to check the time */
  private TimerQueueClock timerQueueClock;

  /** The history player the events is based on */
  private final HistoryPlayerInternal historyPlayer;

  /** Whether or not rescheduling is needed before playback */
  private AtomicBoolean needsRescheduling = new AtomicBoolean(false);
  
  /** For sorting a list of HistoryUpdateIds, making the supervision events come last */
  private HistoryUpdateIdSorter historyUpdateIdSorter;
  
  /**
   * 
   * @param historyPlayer
   *          The history player to schedule
   */
  public HistoryScheduler(final HistoryPlayerInternal historyPlayer) {
    this.historyPlayer = historyPlayer;
    this.timerQueueClock = new TimTimerClockDelegate();
    createTimTimer();

    this.historyUpdateIdSorter = new HistoryUpdateIdSorter();
    
    this.historyPlayer.getPlaybackControl().addPlaybackControlListener(new PlaybackControlAdapter() {
      @Override
      public void onClockTimeChanging(final long newTime) {
        needsRescheduling.set(false);
        cancelAllScheduledEvents();
      }
      
      @Override
      public void onClockTimeChanged(final long newTime) {
        rescheduleEvents();
      }

      @Override
      public void onPlaybackStarting() {
        rescheduleIfNeeded();
      }
    });
  }

  /**
   * Cancels all previously scheduled events.
   */
  public void cancelAllScheduledEvents() {
    this.createTimTimer(false);
  }

  /**
   * Reinitializes the history player. This means canceling all events that have
   * already been scheduled, update all client data tags with the proper value
   * at the clock's current time and schedule new events for future updates.
   */
  public void rescheduleEvents() {
    this.needsRescheduling.compareAndSet(false, true);
    if (historyPlayer.getPlaybackControl().isPlaying()) {
      rescheduleIfNeeded();
    }
    this.updateDataTagsWithValueAtCurrentTime();
  }
  
  /**
   * Reschedules only if needed. Is called only when currently playing, or is
   * going to play
   */
  private void rescheduleIfNeeded() {
    if (this.needsRescheduling.compareAndSet(true, false)) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("History player reinitialized at " + new Date(historyPlayer.getPlaybackControl().getClockTime()));
      }

      this.scheduleEvents();
    }
  }
  
  /**
   * Creates an instance of the {@link TimerQueue} and adds a listener to it
   * 
   * @return the created timer
   */
  private TimerQueue createTimTimer() {
    return createTimTimer(true);
  }
  
  /**
   * Creates an instance of the {@link TimerQueue} and adds a listener to it
   * 
   * @param createNew
   *          <code>true</code> to also create a new timer. (Default)
   * @return the created timer
   */
  private TimerQueue createTimTimer(final boolean createNew) {
    final TimerQueue newTimer;
    if (createNew) {
      newTimer = new TimerQueue(this.timerQueueClock);
      newTimer.addTimerQueueListener(historyPlayer.getClockSynchronizer());
    }
    else {
      newTimer = null;
    }
    final TimerQueue oldTimer;
    synchronized (this) {
      oldTimer = this.timer;
      this.timer = newTimer;
    }
    if (oldTimer != null) {
      oldTimer.cancel();
      oldTimer.removeTimerQueueListener(historyPlayer.getClockSynchronizer());
    }
    return newTimer;
  }
  
  /**
   * Schedule future events for the provided collection of data ids
   */
  private synchronized void scheduleEvents() {
    LOG.debug("Schedules history events");

    final long currentTime = historyPlayer.getPlaybackControl().getClockTime();

    final TimerQueue timerQueue = createTimTimer();
    
    // iterate over the data
    for (final HistoryUpdateId historyUpdateId : historyPlayer.getHistoryLoader().getHistoryStore().getRegisteredDataIds()) {
      final HistoryGroup dataTagHistory = historyPlayer.getHistoryLoader().getHistoryStore().getHistory(historyUpdateId);

      // schedule update tasks for all the records
      if (dataTagHistory != null) {
        Timestamp lastTimestamp = null;

        for (final HistoryUpdate value : dataTagHistory.getHistory()) {
          if (value != null && value.getExecutionTimestamp() != null && value.getExecutionTimestamp().getTime() >= currentTime) {
            Timestamp timestamp = null;
            if (lastTimestamp != null && value.getExecutionTimestamp().getTime() < lastTimestamp.getTime()) {
              timestamp = new Timestamp(lastTimestamp.getTime() + 1);
            }
            else {
              timestamp = value.getExecutionTimestamp();
            }
            if (timestamp != null) {
              try {
                timerQueue.schedule(new UpdateHistoryTagTask(value) {
                  @Override
                  public void update(final HistoryUpdate value) {
                    historyPlayer.getPublisher().publish(value);
                  }
                }, timestamp);
              }
              catch (IllegalStateException e) {
                // If another thread is calling the cancelAllScheduledEvents()
                // it doesn't need to do this anymore, as this function is soon
                // to be called again.
                LOG.debug("Canceling event scheduling");
                return;
              }
            }
            lastTimestamp = timestamp;
          }
        }
      }
    }
  }

  /**
   * Updates all registered data with the value at the current time of the clock
   */
  public void updateDataTagsWithValueAtCurrentTime() {
    final Collection<HistoryUpdateId> tagIds = Arrays.asList(historyPlayer.getHistoryLoader().getHistoryStore().getRegisteredDataIds());
    updateDataTagsWithValueAtCurrentTime(tagIds);
  }
  
  /**
   * Updates all data tags with the value at the current time of the clock
   * 
   * @param historyUpdateIds
   *          The tag IDs of the data tags to update
   */
  public void updateDataTagsWithValueAtCurrentTime(final Collection<HistoryUpdateId> historyUpdateIds) {

    if (!historyPlayer.isHistoryPlayerActive()) {
      return;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("History player updates the following data tags with the value at the current time of the player's clock: " + historyUpdateIds);
    }

    // Sorts the updates so the supervision events comes last.
    // This is needed since the TagValueUpdates cleans the ClientDataTag, which removes all
    // supervision data.
    final HistoryUpdateId[] sortedIds = historyUpdateIds.toArray(new HistoryUpdateId[0]);
    Arrays.sort(sortedIds, this.historyUpdateIdSorter);
    
    final long currentTime = historyPlayer.getPlaybackControl().getClockTime();

    // iterate over the data tag IDs which should be updated
    for (final HistoryUpdateId historyUpdateId : sortedIds) {
      if (historyPlayer.getHistoryLoader().getHistoryStore().isTagInitialized(historyUpdateId)) {
        // Gets the current value of the tag from the history store
        final HistoryUpdate historyValue = historyPlayer.getHistoryLoader().getHistoryStore().getTagValue(historyUpdateId, currentTime);

        try {
          // update the data tag with the latest value or invalidate it if none
          // was found
          if (historyValue == null) {
            historyPlayer.getPublisher().invalidate(historyUpdateId, "No history records was found in the short term log at the specified time");
          }
          else {
            historyPlayer.getPublisher().publishInitialValue(historyValue);
          }
        }
        catch (Exception e) {
          LOG.error(String.format("Error when updating datatag with id %d", historyUpdateId), e);
        }
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("The data tags are updated");
    }
  }

  /** For sorting a list of HistoryUpdateIds, making the supervision events come last */
  static class HistoryUpdateIdSorter implements Comparator<HistoryUpdateId> {
    @Override
    public int compare(final HistoryUpdateId o1, final HistoryUpdateId o2) {
      if (!o1.isSupervisionEventId() && o2.isSupervisionEventId()) {
        return -1;
      }
      if (o1.isSupervisionEventId() && !o2.isSupervisionEventId()) {
        return 1;
      }
      return 0;
    }
  }
  
  /**
   * Callback for the {@link HistoryPlayerImpl#timer} to get the time, etc.
   */
  class TimTimerClockDelegate implements TimerQueueClock {
    @Override
    public long getBehindScheduleThreshold() {
      return (long) (BEHIND_SCHEDULE_THRESHOLD * historyPlayer.getPlaybackControl().getPlaybackSpeed());
    }

    @Override
    public double getSpeedMultiplier() {
      return historyPlayer.getPlaybackControl().getPlaybackSpeed();
    }

    @Override
    public long getTime() {
      return historyPlayer.getPlaybackControl().getClockTime();
    }
  }

}
