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
package cern.c2mon.client.ext.history.data.filter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.Timespan;

/**
 * This class is used to check if a day of history is needed to be loaded for a
 * tag or not. Is based on data from the daily snapshot table.
 * 
 * @author vdeila
 * 
 */
public class DailySnapshotSmartFilter {

  /** The threshold for a timespan to be added */
  private static final Long IGNORE_TIMESPAN_SHORTER_THAN = 24L * 60L * 60L * 1000L;
  
  /** The values from the daily snapshot table */
  private final Map<Long, List<Timespan>> timespansToSkip;

  /**
   * 
   * @param dailySnapshotValues
   *          The values from the daily snapshot table, which can be used to do
   *          filtering.
   * 
   * @see {@link #addDailySnapshotValues(Collection)}
   */
  public DailySnapshotSmartFilter(final Collection<HistoryTagValueUpdate> dailySnapshotValues) {
    this();
    addDailySnapshotValues(dailySnapshotValues);
  }

  /**
   * 
   * @see {@link #addDailySnapshotValues(Collection)}
   */
  public DailySnapshotSmartFilter() {
    this.timespansToSkip = new HashMap<Long, List<Timespan>>();
  }

  /**
   * 
   * @param dailySnapshotValues
   *          The values from the daily snapshot table
   */
  public void addDailySnapshotValues(final Collection<HistoryTagValueUpdate> dailySnapshotValues) {
    // Puts the values for each tag id into a list
    final Map<Long, List<HistoryTagValueUpdate>> valuesByTagId = new HashMap<Long, List<HistoryTagValueUpdate>>();
    for (final HistoryTagValueUpdate value : dailySnapshotValues) {
      List<HistoryTagValueUpdate> values = valuesByTagId.get(value.getId());
      if (values == null) {
        values = new ArrayList<HistoryTagValueUpdate>();
        valuesByTagId.put(value.getId(), values);
      }
      values.add(value);
    }

    // Comparator used for sorting each list ascending
    final Comparator<HistoryTagValueUpdate> comparator = new Comparator<HistoryTagValueUpdate>() {
      @Override
      public int compare(final HistoryTagValueUpdate o1, final HistoryTagValueUpdate o2) {
        return o1.getLogTimestamp().compareTo(o2.getLogTimestamp());
      }
    };

    // Finds and adds the days which can be skipped
    for (final Long tagId : valuesByTagId.keySet()) {
      final List<HistoryTagValueUpdate> listValues = valuesByTagId.get(tagId);
      final HistoryTagValueUpdate[] values = listValues.toArray(new HistoryTagValueUpdate[0]);
      final List<Timespan> tagTimespans = new ArrayList<Timespan>();
      Arrays.sort(values, comparator);
      for (int i = 0; i < values.length; i++) {
        final Timestamp end = values[i].getLogTimestamp();
        final Timestamp start = values[i].getServerTimestamp();
        if (end.getTime() - start.getTime() >= IGNORE_TIMESPAN_SHORTER_THAN) {
          tagTimespans.add(new Timespan(start, end));
        }
      }
      if (tagTimespans.size() > 0) {
        // Merges timespans which overlap, assuming the list is sorted ascending
        Timespan current = tagTimespans.get(0);
        for (int i = 1; i < tagTimespans.size(); i++) {
          Timespan previous = current;
          current = tagTimespans.get(i);
          
          if (current.getStart().before(previous.getEnd())) {
            // Removes the previous one
            tagTimespans.remove(i - 1);
            i--;
            
            // Updates the current timespan to overlap the area of them both
            if (previous.getStart().before(current.getStart())) {
              current.setStart(previous.getStart());
            }
          }
        }
        
        timespansToSkip.put(tagId, tagTimespans);
      }
    }
  }

  /**
   * 
   * @param tagId
   *          the tag id
   * @param time
   *          the time which will be inside the timespan
   * @return if <code>time</code> is inside a period which can be skipped, it
   *         will return the timespan of this period. <code>null</code> if the
   *         time isn't inside any timespan which can be skipped.
   */
  public Timespan getTimespan(final Long tagId, final Timestamp time) {
    final List<Timespan> timespans = this.timespansToSkip.get(tagId);
    if (timespans != null) {
      for (final Timespan timespan : timespans) {
        if (timespan.getStart().compareTo(time) <= 0
            && timespan.getEnd().compareTo(time) >= 0) {
          return new Timespan(timespan);
        }
      }
    }
    return null;
  }

  /**
   * 
   * @param tagId
   *          the tag id
   * @return a list of days which can be skipped
   */
  public Collection<Timespan> getTimespansToSkip(final Long tagId) {
    final List<Timespan> result = new ArrayList<Timespan>();
    final List<Timespan> timespans = this.timespansToSkip.get(tagId);
    if (timespans != null) {
      for (Timespan timespan : timespans) {
        result.add(new Timespan(timespan));
      }
    }
    return result;
  }

  /**
   * 
   * @param tagId
   *          the tag id of the values to remove (which will be forgotten by the
   *          smart filtering)
   */
  public void deleteFilter(final Long tagId) {
    this.timespansToSkip.remove(tagId);
  }
  
  /**
   * Removes all information / data
   */
  public void clear() {
    this.timespansToSkip.clear();
  }

}
