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

package cern.c2mon.client.ext.history.playback.schedule;

import cern.c2mon.client.ext.history.common.HistoryUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * Instances of this class are used to schedule an update of a
 * {@link TagValueUpdate}. Tasks can be scheduled with the {@link TimerQueue}.
 * 
 * @author Michael Berberich
 * @author vdeila
 * @see TimerQueue
 */
public abstract class UpdateHistoryTagTask extends TimerTask {

  /** The value with that the data tag will be updated */
  private HistoryUpdate value;

  /**
   * Constructor
   * 
   * @param value
   *          The value with that will be passed to
   *          {@link #update(HistoryUpdate)}
   */
  public UpdateHistoryTagTask(final HistoryUpdate value) {
    this.value = value;
  }

  /**
   * The data tag is updated as soon as the timer runs this method.
   */
  @Override
  public final void run() {
    update(value);
  }

  /**
   * This method is called when the task should be executed
   * 
   * @param value
   *          the value which were given under construction
   */
  public abstract void update(HistoryUpdate value);
}
