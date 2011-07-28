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
package cern.c2mon.client.history.playback.data.utilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cern.c2mon.client.history.util.TagHistory;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * This class contains methods for converting the results you get from the
 * {@link HistoryProvider}
 * 
 * @author vdeila
 * 
 */
public final class HistoryDataUtil {

  /**
   * Converts a bunch of values into {@link TagHistory} objects.
   * 
   * @param tagValueUpdates
   *          The values to convert
   * @return a collection of the converted objects
   */
  public static Collection<TagHistory> toTagHistoryCollection(final Collection<TagValueUpdate> tagValueUpdates) {
    final Map<Long, TagHistory> dataTagHistoryMap = new HashMap<Long, TagHistory>();

    // Converts the tagValueUpdates into a dataTagHistoryMap
    for (final TagValueUpdate tagValueUpdate : tagValueUpdates) {
      if (tagValueUpdate == null) {
        continue;
      }
      TagHistory history = dataTagHistoryMap.get(tagValueUpdate.getId());
      if (history == null) {
        history = new TagHistory(tagValueUpdate.getId());
        dataTagHistoryMap.put(history.getTagId(), history);
      }
      history.add(tagValueUpdate);
    }

    return dataTagHistoryMap.values();
  }

  /**
   * hidden constructor, utility class
   */
  private HistoryDataUtil() {
  }

}
