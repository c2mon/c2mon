/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.client.history.playback.components;

import java.util.Date;

/**
 * Implements a clock. The clock can be used e.g. to fake the system time.
 * 
 * @author Michael Berberich
 */
public class Clock {

  /** Flag that is indicating if the clock is currently paused or running */
  private boolean paused;

  /** The initial start date */
  private Date startDate;

  /** The initial end date */
  private Date endDate;

  /** The time that have elapsed before the last pause */
  private long virtualTimeElapsedBeforeLastPause;

  /** The real time of when the clock was (last) resumed */
  private long resumedAtRealtime;

  /** Multiplier to make the clock go faster or slower */
  private double speedMultiplier = 1.0;

  /**
   * Constructor
   * 
   * @param startDate
   *          The initial start date
   * @param endDate
   *          The initial end date
   */
  public Clock(final Date startDate, final Date endDate) {
    if (startDate == null || endDate == null)
      throw new IllegalArgumentException("Neither the start date nor the end date of the clock must be null!");

    this.startDate = startDate;
    this.endDate = endDate;

    // No time have passed, setting it to zero
    this.virtualTimeElapsedBeforeLastPause = 0;

    // Starts the clock paused
    this.paused = true;
  }

  /**
   * 
   * @return The time elapsed since the clock was (last) resumed. If it is
   *         currently paused it will return zero. This does NOT include the
   *         time BEFORE it was resumed
   */
  private synchronized long elapsedSinceResume() {
    if (this.paused) {
      return 0;
    }
    else {
      // The current time minus the time of when it was last resumed equals the
      // time which have elapsed (in real time)
      final long realtimeSinceUnpausing = (System.currentTimeMillis() - this.resumedAtRealtime);

      // Converts the time into fictional time, which is changed by the clock
      // speed
      return (long) (realtimeSinceUnpausing * this.speedMultiplier);
    }
  }

  /**
   * @return the total elapsed time in milliseconds (with consideration for the
   *         speed multiplier)
   */
  private synchronized long elapsed() {
    // The fictional time elapsed before the unpausing pluss the fictional time
    // after the unpausing equals the total fictional time elapsed
    return this.virtualTimeElapsedBeforeLastPause + elapsedSinceResume();
  }

  /**
   * @return The current time of the clock.
   */
  public synchronized long getTime() {
    return getTime(true);
  }

  /**
   * 
   * @param checkIfEndTimeIsReached
   *          if <code>true</code> it checks first if the end time is reached
   * @return The current time of the clock.
   */
  private synchronized long getTime(final boolean checkIfEndTimeIsReached) {
    if (checkIfEndTimeIsReached) {
      hasReachedEndTime();
    }
    return this.startDate.getTime() + elapsed();
  }

  /**
   * Sets the time
   * 
   * @param time
   *          The time to set
   */
  public synchronized void setTime(final long time) {
    final boolean doPausing = !this.paused;
    // If the clock is running, it is paused while setting the time.
    if (doPausing) {
      pause();
    }

    // Sets the time that must have elapsed for the new time to be correct
    this.virtualTimeElapsedBeforeLastPause = time - this.startDate.getTime();

    // If the clock was paused, resume it.
    if (doPausing) {
      resume();
    }
  }

  /**
   * 
   * @param time
   *          The new ending time
   */
  public synchronized void setEndTime(final long time) {
    this.endDate = new Date(time);
  }

  /**
   * 
   * @param time
   *          The new starting time
   */
  public synchronized void setStartTime(final long time) {
    final long difference = this.startDate.getTime() - time;
    this.virtualTimeElapsedBeforeLastPause += difference;
    this.startDate = new Date(time);
  }

  /**
   * starts the clock
   */
  public synchronized void start() {
    // The same as resume
    resume();
  }

  /**
   * resumes the clock
   */
  private synchronized void resume() {
    // Can only resume if it is paused (or stopped)
    if (this.paused) {
      // Stores the real time of when the clock was resumed
      this.resumedAtRealtime = System.currentTimeMillis();
      this.paused = false;
    }
  }

  /**
   * pauses the clock
   */
  public synchronized void pause() {
    // Can only pause if it is running
    if (!this.paused) {
      // Keeps the count of how much fictional time have elapsed, and adds it to
      // the total
      this.virtualTimeElapsedBeforeLastPause += elapsedSinceResume();
      this.paused = true;
    }
  }

  /**
   * Stops the clock and sets the time to the beginning.
   */
  public synchronized void reset() {
    pause();
    this.virtualTimeElapsedBeforeLastPause = 0;
  }

  /**
   * @param speedMultiplier
   *          the speedMultiplier to set
   */
  public synchronized void setSpeedMultiplier(final double speedMultiplier) {
    // If the clock is currently running it must be paused and resumed
    // for the elapsed time to be counted with the correct, current, multiplier
    final boolean doPausing = !this.paused;
    if (doPausing) {
      pause();
    }
    this.speedMultiplier = speedMultiplier;
    if (doPausing) {
      resume();
    }
  }

  /**
   * Checks if the clock has reached the specified end time.
   * 
   * @return true if the clock has reached its end, false otherwise
   */
  public boolean hasReachedEndTime() {
    if (getTime(false) >= getEndDate().getTime()) {
      pause();
      setTime(getEndDate().getTime());
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Makes this clock equivalent to the passed clock
   * 
   * @param clock
   *          The parameter to synchronize with
   */
  public synchronized void synchronize(final Clock clock) {
    synchronized (clock) {
      this.startDate = clock.startDate;
      this.endDate = clock.endDate;
      this.virtualTimeElapsedBeforeLastPause = clock.virtualTimeElapsedBeforeLastPause;
      this.resumedAtRealtime = clock.resumedAtRealtime;
      this.paused = clock.paused;
      this.speedMultiplier = clock.speedMultiplier;
    }
  }

  /**
   * @return the elapsed time in a convenient format and tells whether the clock
   *         is currently running or not
   */
  public String toString() {
    String s = "time: " + this.getTimeAsDate();
    if (isPaused()) {
      s += " (time paused)";
    }
    else {
      s += " (time running)";
    }

    return s;
  }

  /**
   * @return The start time
   */
  public synchronized long getStartTime() {
    return this.startDate.getTime();
  }

  /**
   * @return The start Date
   */
  public Date getStartDate() {
    return this.startDate;
  }

  /**
   * @return The end time
   */
  public synchronized long getEndTime() {
    return this.endDate.getTime();
  }

  /**
   * @return The end Date
   */
  public Date getEndDate() {
    return this.endDate;
  }

  /**
   * @return The current time as <code>Date</code>
   */
  public Date getTimeAsDate() {
    return new Date(this.getTime());
  }

  /**
   * @return true if the clock is paused, false if the clock is running
   */
  public boolean isPaused() {
    hasReachedEndTime();
    return (this.paused);
  }

  /**
   * @return true if the clock is running, false if the clock is paused
   */
  public boolean isRunning() {
    hasReachedEndTime();
    return !this.paused;
  }

  /**
   * @return the speedMultiplier
   */
  public synchronized double getSpeedMultiplier() {
    return speedMultiplier;
  }

}
