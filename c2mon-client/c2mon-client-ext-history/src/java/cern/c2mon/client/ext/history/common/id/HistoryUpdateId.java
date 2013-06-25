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
package cern.c2mon.client.ext.history.common.id;

import cern.c2mon.client.ext.history.common.HistoryUpdate;

/**
 * This class is used by the history to generalize, and identify
 * {@link HistoryUpdate}s. Each class implementing the {@link HistoryUpdate},
 * will also need to implement this class to make them self possible to
 * identify.
 * 
 * @author vdeila
 * 
 */
public abstract class HistoryUpdateId {

  /**
   * 
   * @return <code>true</code> if this is a {@link TagValueUpdateId}
   */
  public boolean isTagValueUpdateId() {
    return (this instanceof TagValueUpdateId);
  }

  /**
   * 
   * @return the {@link TagValueUpdateId}, or <code>null</code> if this is not a
   *         {@link TagValueUpdateId}
   */
  public TagValueUpdateId getTagValueUpdateId() {
    if (this instanceof TagValueUpdateId) {
      return (TagValueUpdateId) this;
    }
    else {
      return null;
    }
  }

  /**
   * 
   * @return <code>true</code> if this is a {@link SupervisionEventId}
   */
  public boolean isSupervisionEventId() {
    return (this instanceof SupervisionEventId);
  }

  /**
   * 
   * @return the {@link SupervisionEventId}, or <code>null</code> if this is not
   *         a {@link SupervisionEventId}
   */
  public SupervisionEventId getSupervisionEventId() {
    if (this instanceof SupervisionEventId) {
      return (SupervisionEventId) this;
    }
    else {
      return null;
    }
  }

}
