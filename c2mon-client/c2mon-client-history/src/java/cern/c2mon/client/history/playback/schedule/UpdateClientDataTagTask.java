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

import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * Instances of this class are used to schedule an update of a
 * <code>ClientDataTag</code>. Tasks can be scheduled with the
 * <code>TimTimer</code>.
 * 
 * @author Michael Berberich
 * @see TimTimer
 */
public class UpdateClientDataTagTask extends TimTimerTask {
  /** The data tag to update */
  private TagUpdateListener updateDelegate;

  /** The value with that the data tag will be updated */
  private TagValueUpdate value;

  /**
   * Constructor
   * 
   * @param cdt The data tag to update
   * @param value The value with that the data tag will be updated
   */
  public UpdateClientDataTagTask(final TagUpdateListener cdt, final TagValueUpdate value) {
    this.updateDelegate = cdt;
    this.value = value;
  }

  /**
   * The data tag is updated as soon as the timer runs this method.
   */
  @Override
  public void run() {
    updateDelegate.onUpdate(value);
  }
}
