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

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.event.PlaybackControlAdapter;
import cern.c2mon.client.history.playback.PlaybackSynchronizeControl;
import cern.c2mon.client.history.playback.schedule.event.TimerQueueListener;

/**
 * Synchronizes the clock, if the view is updating to the tags too slow, the
 * clock will be paused until the views is updated before giving more updates.
 * 
 * @author vdeila
 * 
 */
public class ClockSynchronizer implements TimerQueueListener {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(ClockSynchronizer.class);

  /** The thread used to delay the clock when the tag updates is taking too long */
  private Thread timerBehindScheduleThread = null;

  /** The lock for {@link #timerBehindScheduleThread} */
  private ReentrantReadWriteLock timerBehindScheduleThreadLock = new ReentrantReadWriteLock();
  
  /** The play back control to control the player */
  private final PlaybackSynchronizeControl playbackControl;
  
  /**
   * 
   * @param playbackControl the playback control of the history player to synchronize
   */
  public ClockSynchronizer(final PlaybackSynchronizeControl playbackControl) {
    this.playbackControl = playbackControl;
    
    this.playbackControl.addPlaybackControlListener(new PlaybackControlAdapter() {
      @Override
      public void onPlaybackStopped() {
        interruptBehindScheduleThread();
      }
    });
  }
  
  /**
   * Is invoked when the timer is behind schedule. This will pause the clock
   * until it is back on schedule
   * 
   * @param byTime
   *          Milliseconds behind schedule
   */
  @Override
  public void timerIsBehindSchedule(final long byTime) {
    try {
      timerBehindScheduleThreadLock.readLock().lock();
      if (timerBehindScheduleThread != null) {
        // The thread is already running
        return;
      }
    }
    finally {
      timerBehindScheduleThreadLock.readLock().unlock();
    }
    try {
      timerBehindScheduleThreadLock.writeLock().lock();
      if (timerBehindScheduleThread == null) {
        timerBehindScheduleThread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              if (playbackControl.isPlaying()) {
                final long timeToSleep = (long) (byTime / playbackControl.getPlaybackSpeed());
                // Pauses the clock
                playbackControl.disablePlayback();
                try {
                  if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("The data is going behind schedule, delaying the clock by %d milliseconds", timeToSleep));
                  }
  
                  // Waits as long as it is behind schedule
                  try {
                    Thread.sleep(timeToSleep);
                  }
                  catch (InterruptedException e) {
                    if (LOG.isDebugEnabled()) {
                      LOG.debug(String.format("The pausing of the clock were interrupted"));
                    }
                    return;
                  }
                }
                finally {
                  // Resumes the clock again
                  playbackControl.enablePlayback();
                }
              }
            }
            finally {
              try {
                timerBehindScheduleThreadLock.writeLock().lock();
                timerBehindScheduleThread = null;
              }
              finally {
                timerBehindScheduleThreadLock.writeLock().unlock();
              }
            }
          }
        });
        timerBehindScheduleThread.setName("Timer-Behind-Schedule-Thread");
        timerBehindScheduleThread.start();
      }
    }
    finally {
      timerBehindScheduleThreadLock.writeLock().unlock();
    }
  }

  /**
   * Is invoked when the timer is back on schedule
   */
  @Override
  public void timerIsOnSchedule() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("The TimTimer is back on schedule");
    }
    interruptBehindScheduleThread();
  }

  /**
   * Interrupts the {@link #timerBehindScheduleThread} (only if it is running)
   * 
   * @return <code>true</code> if the thread was interrupted
   */
  public boolean interruptBehindScheduleThread() {
    boolean didInterrupt = false;
    try {
      timerBehindScheduleThreadLock.readLock().lock();
      if (timerBehindScheduleThread != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Trying to interrupt the timer-behind-schedule-thread");
        }
        try {
          didInterrupt = true;
          timerBehindScheduleThread.interrupt();
        }
        catch (final Exception e) {
          LOG.debug("Failed when trying to interrupt the timer-Behind-Schedule-Thread", e);
        }
      }
    }
    finally {
      timerBehindScheduleThreadLock.readLock().unlock();
    }
    return didInterrupt;
  }
}
