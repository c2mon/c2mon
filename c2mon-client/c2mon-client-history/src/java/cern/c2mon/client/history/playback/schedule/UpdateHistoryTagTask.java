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

package cern.c2mon.client.history.playback.schedule;

import cern.c2mon.client.common.history.HistoryUpdate;
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
