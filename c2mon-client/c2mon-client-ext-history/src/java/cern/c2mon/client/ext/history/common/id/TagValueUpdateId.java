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

/**
 * This class is used to identify one or multiple {@link TagValueUpdate}
 * 
 * @author vdeila
 *
 */
public class TagValueUpdateId extends HistoryUpdateId {

  /** the tag id */
  private final Long tagId;
  
  /**
   * 
   * @param tagId the tag id
   */
  public TagValueUpdateId(final Long tagId) {
    this.tagId = tagId;
  }

  /**
   * @return the tagId
   */
  public Long getTagId() {
    return tagId;
  }

  @Override
  public String toString() {
    return String.format("TagValueUpdate %d", this.tagId);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tagId == null) ? 0 : tagId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TagValueUpdateId))
      return false;
    TagValueUpdateId other = (TagValueUpdateId) obj;
    if (tagId == null) {
      if (other.tagId != null)
        return false;
    }
    else if (!tagId.equals(other.tagId))
      return false;
    return true;
  }

}
