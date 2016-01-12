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
package cern.c2mon.client.ext.history.common.tag;

import java.sql.Timestamp;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * This class holds the fields necessary for one record, used by the
 * {@link HistoryTagImpl} to calculate the return value of
 * {@link HistoryTagImpl#getValue()}.
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
  public HistoryTagRecord(final Tag tagUpdate) {
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
