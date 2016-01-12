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
  public boolean isTagValueUpdateIdType() {
    return (this instanceof TagValueUpdateId);
  }

  /**
   * 
   * @return the {@link TagValueUpdateId}, or <code>null</code> if this is not a
   *         {@link TagValueUpdateId}
   */
  public TagValueUpdateId toTagValueUpdateId() {
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
  public boolean isSupervisionEventIdType() {
    return (this instanceof SupervisionEventId);
  }

  /**
   * 
   * @return the {@link SupervisionEventId}, or <code>null</code> if this is not
   *         a {@link SupervisionEventId}
   */
  public SupervisionEventId toSupervisionEventId() {
    if (this instanceof SupervisionEventId) {
      return (SupervisionEventId) this;
    }
    else {
      return null;
    }
  }

}
