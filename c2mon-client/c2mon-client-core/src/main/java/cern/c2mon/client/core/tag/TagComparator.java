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
package cern.c2mon.client.core.tag;

import cern.c2mon.client.common.tag.Tag;

/**
 * Helper class used by {@link TagImpl} to
 * compare two value update objects and to find out whether
 * they are equal.
 *
 * @author Matthias Braeger
 */
class TagComparator {
 
  /**
   * Compares the two given tags and returns true, if they are equal tag updates.
   * @param tag1 the first tag 
   * @param tag2 the second tag to compare against
   * @return true, if the two tags are equal, otherwise false
   */
  static boolean compare(Tag tag1, Tag tag2) {
    if (tag1 == tag2) {
      return true;
    }
    if (tag1 == null || tag2 == null) {
      return false;
    }
    if (!(tag2 instanceof TagImpl)) {
      return false;
    }

    if (tag1.getDaqTimestamp() == null) {
      if (tag2.getDaqTimestamp() != null) {
        return false;
      }
    }
    else if (!tag1.getDaqTimestamp().equals(tag2.getDaqTimestamp())) {
      return false;
    }
    else if (tag1.getId() != tag2.getId()) {
      return false;
    }
    
    if (tag1.getServerTimestamp() == null) {
      if (tag2.getServerTimestamp() != null) {
        return false;
      }
    }
    else if (!tag1.getServerTimestamp().equals(tag2.getServerTimestamp())) {
      return false;
    }
    if (tag1.getTimestamp() == null) {
      if (tag2.getTimestamp() != null) {
        return false;
      }
    }
    else if (!tag1.getTimestamp().equals(tag2.getTimestamp())) {
      return false;
    }
    if (tag1.getDataTagQuality() == null) {
      if (tag2.getDataTagQuality() != null) {
        return false;
      }
    }
    else if (!tag1.getDataTagQuality().equals(tag2.getDataTagQuality())) {
      return false;
    }
    if (tag1.getValue() == null) {
      if (tag2.getValue() != null) {
        return false;
      }
    }
    else if (!tag1.getValue().equals(tag2.getValue())) {
      return false;
    }
    if (tag1.getValueDescription() == null) {
      if (tag2.getValueDescription() != null) {
        return false;
      }
    }
    else if (!tag1.getValueDescription().equals(tag2.getValueDescription())) {
      return false;
    }
    return true;
  }
}
