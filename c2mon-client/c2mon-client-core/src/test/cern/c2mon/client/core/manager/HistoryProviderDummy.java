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
package cern.c2mon.client.core.manager;

import java.sql.Timestamp;
import java.util.Collection;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistorySupervisionEvent;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.SupervisionEventRequest;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.common.history.event.HistoryProviderListener;

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
