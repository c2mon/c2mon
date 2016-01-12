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
package cern.c2mon.client.ext.history.data.utilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.HistorySupervisionEvent;
import cern.c2mon.client.ext.history.common.HistoryUpdate;
import cern.c2mon.client.ext.history.common.id.HistoryUpdateId;
import cern.c2mon.client.ext.history.updates.HistorySupervisionEventImpl;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.client.ext.history.util.HistoryGroup;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
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
   * Converts a bunch of values into {@link HistoryGroup} objects.
   * 
   * @param tagValueUpdates
   *          The values to convert
   * @return a collection of the converted objects
   */
  public static Collection<HistoryGroup> toTagHistoryCollection(final Collection<HistoryUpdate> tagValueUpdates) {
    final Map<HistoryUpdateId, HistoryGroup> dataTagHistoryMap = new HashMap<HistoryUpdateId, HistoryGroup>();

    // Converts the tagValueUpdates into a dataTagHistoryMap
    for (final HistoryUpdate historyUpdate : tagValueUpdates) {
      if (historyUpdate == null) {
        continue;
      }
      
      HistoryGroup history = dataTagHistoryMap.get(historyUpdate.getUpdateId());
      if (history == null) {
        history = new HistoryGroup(historyUpdate.getUpdateId());
        dataTagHistoryMap.put(historyUpdate.getUpdateId(), history);
      }
      if (historyUpdate instanceof TagValueUpdate) {
        history.add(new HistoryTagValueUpdateImpl((TagValueUpdate) historyUpdate));
      }
      else if (historyUpdate instanceof SupervisionEvent && historyUpdate.getUpdateId().isSupervisionEventIdType()) {
        final SupervisionEvent event = (SupervisionEvent) historyUpdate;
        final boolean initialValue;
        if (event instanceof HistorySupervisionEvent) {
          final HistorySupervisionEvent historySupervisionEvent = (HistorySupervisionEvent) event;
          initialValue = historySupervisionEvent.isInitialValue();
        }
        else {
          initialValue = false;
        }
        
        final HistorySupervisionEventImpl historySupervisionEvent = new HistorySupervisionEventImpl(
            historyUpdate.getUpdateId().toSupervisionEventId(), event.getStatus(), event.getEventTime(), event.getMessage());
        historySupervisionEvent.setInitialValue(initialValue);
        history.add(historySupervisionEvent);
      }
      else {
        throw new RuntimeException(String.format("The HistoryUpdate type is not supported (%s)", historyUpdate.getClass().getName()));
      }
    }

    return dataTagHistoryMap.values();
  }

  /**
   * hidden constructor, utility class
   */
  private HistoryDataUtil() {
  }

}
