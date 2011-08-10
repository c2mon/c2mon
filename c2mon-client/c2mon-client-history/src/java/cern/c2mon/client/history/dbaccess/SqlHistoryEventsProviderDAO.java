package cern.c2mon.client.history.dbaccess;

import java.sql.Timestamp;
import java.util.Collection;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistorySupervisionEvent;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.SupervisionEventRequest;
import cern.tim.shared.common.supervision.SupervisionConstants.SupervisionEntity;

/**
 * Implementation of the {@link HistoryProvider}<br/>
 * <br/>
 * Gets data from major history events
 * 
 * @author vdeila
 *
 */
class SqlHistoryEventsProviderDAO extends HistoryProviderAbs {

  public SqlHistoryEventsProviderDAO() {
    throw new RuntimeException("This provider is not implemented");
  }
  
  @Override
  public Collection<HistoryTagValueUpdate> getHistory(Long[] tagIds, Timestamp from, Timestamp to) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(Long[] tagIds, Timestamp from, Timestamp to, int maximumTotalRecords) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(Long[] tagIds, int maximumTotalRecords) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(int maximumRecordsPerTag, Long[] tagIds, Timestamp from, Timestamp to) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(int maximumRecordsPerTag, Long[] tagIds) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getInitialValuesForTags(Long[] tagIds, Timestamp before) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<HistorySupervisionEvent> getInitialSupervisionEvents(Timestamp initializationTime, Collection<SupervisionEventRequest> requests) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<HistorySupervisionEvent> getSupervisionEvents(Timestamp from, Timestamp to, Collection<SupervisionEventRequest> requests) {
    // TODO Auto-generated method stub
    return null;
  }

}
