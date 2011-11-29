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
package cern.c2mon.client.core.tag.history;

import java.sql.Timestamp;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.tag.HistoryTag;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * This class holds the fields necessary for one record, used by the
 * {@link HistoryTag} to calculate the return value of
 * {@link HistoryTag#getValue()}.
 * 
 * @author vdeila
 */
public class HistoryTagRecord {

  /** The label */
  private final Timestamp timestamp;

  /** The value */
  private final Object value;

  /** Whether or not the value is valid */
  private final boolean valid;

  /**
   * @param tagValueUpdate
   *          the tag value update to copy
   */
  public HistoryTagRecord(final TagValueUpdate tagValueUpdate) {
    this.timestamp = tagValueUpdate.getServerTimestamp();
    this.value = tagValueUpdate.getValue();
    this.valid = tagValueUpdate.getDataTagQuality().isValid();
  }

  /**
   * @param tagUpdate
   *          the client data tag value to copy
   */
  public HistoryTagRecord(final ClientDataTagValue tagUpdate) {
    this.timestamp = tagUpdate.getServerTimestamp();
    this.value = tagUpdate.getValue();
    this.valid = tagUpdate.getDataTagQuality().isValid();
  }

  /**
   * @return the timestamp
   */
  public Timestamp getTimestamp() {
    return timestamp;
  }

  /**
   * @return the value
   */
  public Object getValue() {
    return value;
  }

  /**
   * @return Whether or not the value is valid
   */
  public boolean isValid() {
    return valid;
  }

  
}
