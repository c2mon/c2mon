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
package cern.c2mon.client.ext.history.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cern.c2mon.client.ext.history.common.id.HistoryUpdateId;
import cern.c2mon.client.ext.history.common.id.TagValueUpdateId;

/**
 * Utility class for data ids
 * 
 * @author vdeila
 * 
 */
public final class DataIdUtil {

  /**
   * 
   * @param tagIds
   *          the tag ids to convert into a {@link HistoryUpdateId} collection
   * @return a {@link HistoryUpdateId} collection with the given tag ids
   */
  public static Collection<HistoryUpdateId> convertTagIdsToDataIdCollection(final Collection<Long> tagIds) {
    final List<HistoryUpdateId> historyUpdateIds = new ArrayList<HistoryUpdateId>();
    for (final Long tagId : tagIds) {
      historyUpdateIds.add(new TagValueUpdateId(tagId));
    }
    return historyUpdateIds;
  }

  /** Hidden constructor */
  private DataIdUtil() {
  }
}
