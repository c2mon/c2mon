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
package cern.c2mon.client.history.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cern.c2mon.client.common.history.id.HistoryUpdateId;
import cern.c2mon.client.common.history.id.TagValueUpdateId;

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
