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
package cern.c2mon.client.ext.history;

import java.sql.Timestamp;
import java.util.Collection;

import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.HistorySupervisionEvent;
import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.SupervisionEventRequest;
import cern.c2mon.client.ext.history.common.Timespan;
import cern.c2mon.client.ext.history.common.event.HistoryProviderListener;


/**
 * Dummy class for testing purpose
 * 
 * @author vdeila
 *
 */
public class HistoryProviderDummy implements HistoryProvider {

  @Override
  public void addHistoryProviderListener(HistoryProviderListener listener) {

  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(Long[] tagIds, Timestamp from, Timestamp to) {
    return null;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(Long[] tagIds, Timestamp from, Timestamp to, int maximumTotalRecords) {
    return null;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(Long[] tagIds, int maximumTotalRecords) {
    return null;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(int maximumRecordsPerTag, Long[] tagIds, Timestamp from, Timestamp to) {
    return null;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(int maximumRecordsPerTag, Long[] tagIds) {
    return null;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getInitialValuesForTags(Long[] tagIds, Timestamp before) {
    return null;
  }

  @Override
  public void removeHistoryProviderListener(HistoryProviderListener listener) {

  }

  @Override
  public Collection<HistorySupervisionEvent> getInitialSupervisionEvents(Timestamp initializationTime, Collection<SupervisionEventRequest> requests) {
    return null;
  }

  @Override
  public Collection<HistorySupervisionEvent> getSupervisionEvents(Timestamp from, Timestamp to, Collection<SupervisionEventRequest> requests) {
    return null;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getDailySnapshotRecords(Long[] tagIds, Timestamp from, Timestamp to) {
    return null;
  }

  @Override
  public Timespan getDateLimits() {
    return null;
  }

  @Override
  public void disableProvider() {
    
  }

  @Override
  public void enableProvider() {
    
  }

  @Override
  public void resetProgress() {
    
  }

}
