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
package cern.c2mon.client.history.playback.schedule;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.event.PlaybackControlAdapter;
import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.history.playback.HistoryPlayerImpl;
import cern.c2mon.client.history.playback.HistoryPlayerInternal;
import cern.c2mon.client.history.playback.components.ListenersManager;
import cern.c2mon.client.history.playback.schedule.event.HistorySchedulerListener;
import cern.c2mon.client.history.tag.HistoryTagValueUpdateImpl;
import cern.c2mon.client.history.util.TagHistory;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.TagQualityStatus;

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
  private static final long BEHIND_SCHEDULE_THRESHOLD = 200;

  /** A timer to schedule data tag update events */
  private TimerQueue timer;

  /** A callback for the {@link #timer} to check the time */
  private TimerQueueClock timerQueueClock;

  /** Is a callback for the tim timer for updating the data tags */
  private final TagUpdateListener updateValueCallback;

  /** The history player the events is based on */
  private final HistoryPlayerInternal historyPlayer;

  /** A manager which keeps track of the listeners */
  private final ListenersManager<HistorySchedulerListener> listenersManager = new ListenersManager<HistorySchedulerListener>();

  /**
   * 
   * @param historyPlayer
   *          The history player to schedule
   */
  public HistoryScheduler(final HistoryPlayerInternal historyPlayer) {
    this.historyPlayer = historyPlayer;
    this.timerQueueClock = new TimTimerClockDelegate();
    createTimTimer();

    this.updateValueCallback = new TagUpdateListener() {
      @Override
      public void onUpdate(final TagValueUpdate value) {
        historyPlayer.getPublisher().publish(value);
      }
    };

    this.historyPlayer.getPlaybackControl().addPlaybackControlListener(new PlaybackControlAdapter() {
      @Override
      public void onClockTimeSet(final long newTime) {
        rescheduleEvents();
      }
    });
  }

  /**
   * Cancels all previously scheduled events.
   */
  public void cancelAllScheduledEvents() {
    this.createTimTimer();
  }

  /**
   * Reinitializes the history player. This means canceling all events that have
   * already been scheduled, update all client data tags with the proper value
   * at the clock's current time and schedule new events for future updates.
   */
  public void rescheduleEvents() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("History player reinitialized at " + new Date(historyPlayer.getPlaybackControl().getClockTime()));
    }

    final Collection<Long> tagIds = Arrays.asList(historyPlayer.getHistoryLoader().getHistoryStore().getRegisteredTags());

    this.cancelAllScheduledEvents();
    this.updateDataTagsWithValueAtCurrentTime(tagIds);
    this.scheduleEvents(tagIds);
  }

  /**
   * Creates an instance of the {@link TimerQueue} and adds a listener to it
   */
  private void createTimTimer() {
    final TimerQueue oldTimer = this.timer;
    if (oldTimer != null) {
      oldTimer.cancel();
      oldTimer.purge();
      oldTimer.removeTimerQueueListener(historyPlayer.getClockSynchronizer());
    }
    this.timer = new TimerQueue(this.timerQueueClock);
    this.timer.addTimerQueueListener(historyPlayer.getClockSynchronizer());
  }

  /**
   * Schedule future events for the provided collection of data tag IDs
   * 
   * @param tagIDs
   *          The data tag IDs to schedule update events for
   */
  private void scheduleEvents(final Collection<Long> tagIDs) {
    LOG.debug("Schedules history events");

    final long currentTime = historyPlayer.getPlaybackControl().getClockTime();

    final TimerQueue timerQueue = this.timer;

    // iterate over the data tags
    for (final Long tagId : tagIDs) {
      final TagHistory dataTagHistory = historyPlayer.getHistoryLoader().getHistoryStore().getTagHistory(tagId);

      // schedule update tasks for all the records
      if (dataTagHistory != null) {
        Timestamp lastTimestamp = null;

        for (final TagValueUpdate value : dataTagHistory.getHistory()) {
          if (value != null && value.getServerTimestamp() != null && value.getServerTimestamp().getTime() >= currentTime) {
            Timestamp timestamp = null;
            if (lastTimestamp != null && value.getServerTimestamp().getTime() < lastTimestamp.getTime()) {
              timestamp = new Timestamp(lastTimestamp.getTime() + 1);
            }
            else {
              timestamp = value.getServerTimestamp();
            }
            if (timestamp != null) {
              try {
                timerQueue.schedule(new UpdateClientDataTagTask(this.updateValueCallback, value), timestamp);
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
   * Updates all data tags with the value at the current time of the clock
   * 
   * @param tagIds
   *          The tag IDs of the data tags to update
   */
  public void updateDataTagsWithValueAtCurrentTime(final Collection<Long> tagIds) {

    if (!historyPlayer.isHistoryPlayerActive()) {
      return;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("History player updates the following data tags with the value at the current time of the player's clock: " + tagIds);
    }

    // Notifying listeners
    fireStartingUpdatingOfDataTags(tagIds);

    final long currentTime = historyPlayer.getPlaybackControl().getClockTime();

    // iterate over the data tag IDs which should be updated
    for (Long tagId : tagIds) {
      if (historyPlayer.getHistoryLoader().getHistoryStore().isTagInitialized(tagId)) {
        // Gets the current value of the tag from the history store
        TagValueUpdate tagValue = historyPlayer.getHistoryLoader().getHistoryStore().getTagValue(tagId, currentTime);

        try {
          // update the data tag with the latest value or invalidate it if none
          // was found
          if (tagValue == null) {
            final DataTagQualityImpl dataTagQuality = new DataTagQualityImpl(TagQualityStatus.UNDEFINED_TAG,
                "No history records was found in the short term log at the specified time");

            tagValue = new HistoryTagValueUpdateImpl(tagId, dataTagQuality, null, null, new Timestamp(System.currentTimeMillis()), "", TagMode.OPERATIONAL);
          }

          historyPlayer.getPublisher().publish(tagValue);
        }
        catch (Exception e) {
          LOG.error(String.format("Error when updating datatag with id %d", tagId), e);
        }
      }
    }

    // Notifying listeners
    fireFinishedUpdatingOfDataTags(tagIds);

    if (LOG.isDebugEnabled()) {
      LOG.debug("The data tags are updated");
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

  /**
   * Fires the startingUpdatingOfDataTags(tagIds) event
   * 
   * @param tagIds
   *          The tag ids which are updated
   */
  private void fireStartingUpdatingOfDataTags(final Collection<Long> tagIds) {
    for (final HistorySchedulerListener listener : listenersManager.getAll()) {
      listener.startingUpdatingOfDataTags(tagIds);
    }
  }

  /**
   * Fires the finishedUpdatingOfDataTags(tagIds) event
   * 
   * @param tagIds
   *          The tag ids which are updated
   */
  private void fireFinishedUpdatingOfDataTags(final Collection<Long> tagIds) {
    for (final HistorySchedulerListener listener : listenersManager.getAll()) {
      listener.finishedUpdatingOfDataTags(tagIds);
    }
  }

  /**
   * 
   * @param listener
   *          The listener to add
   */
  public void addHistorySchedulerListener(final HistorySchedulerListener listener) {
    this.listenersManager.add(listener);
  }

  /**
   * 
   * @param listener
   *          The listener to remove
   */
  public void removeHistorySchedulerListener(final HistorySchedulerListener listener) {
    this.listenersManager.remove(listener);
  }
}
