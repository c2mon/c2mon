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
