package cern.c2mon.client.history.dbaccess;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cern.c2mon.client.common.history.HistorySupervisionEvent;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.SupervisionEventRequest;
import cern.c2mon.client.common.history.Timespan;

/**
 * This implementation uses only data which is given to it when it is
 * constructed
 * 
 * @author vdeila
 * 
 */
public class HistoryProviderSimpleImpl extends HistoryProviderAbs {

  /** The list of initial records */
  private final List<HistoryTagValueUpdate> initialRecords;

  /** The list of records */
  private final List<HistoryTagValueUpdate> historyRecords;
  
  /** The list of inital supervision events */
  private final List<HistorySupervisionEvent> initialSupervisionRecords;
  
  /** The list of supervision events */
  private final List<HistorySupervisionEvent> supervisionRecords;

  /**
   * If set, it will delay the responses with this amount of milliseconds per
   * requested tag
   */
  private Long emulatedResponseTime = null;

  /**
   * 
   * @param initialRecords
   *          The list of initial records
   * @param historyRecords
   *          The list of records
   * @param initialSupervisionRecords
   *          The list of inital supervision events
   * @param supervisionRecords
   *          The list of supervision events
   */
  public HistoryProviderSimpleImpl(
      final List<HistoryTagValueUpdate> initialRecords, 
      final List<HistoryTagValueUpdate> historyRecords,
      final List<HistorySupervisionEvent> initialSupervisionRecords,
      final List<HistorySupervisionEvent> supervisionRecords) {
    this.initialRecords = initialRecords;
    this.historyRecords = historyRecords;
    this.initialSupervisionRecords = initialSupervisionRecords;
    this.supervisionRecords = supervisionRecords;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getInitialValuesForTags(final Long[] pTagIds, final Timestamp before) {
    final List<HistoryTagValueUpdate> result = new ArrayList<HistoryTagValueUpdate>();

    final Object id = fireQueryStarting();
    fireQueryProgressChanged(id, 0.0);

    for (int i = 0; i < pTagIds.length; i++) {
      final Long searchingTagId = pTagIds[i];
      for (final HistoryTagValueUpdate record : initialRecords) {
        if (searchingTagId.equals(record.getId()) && before.compareTo(record.getServerTimestamp()) > 0) {
          result.add(record);
          break;
        }
      }
      
      emulateWait(1);
      fireQueryProgressChanged(id, i / (double) pTagIds.length);
    }

    fireQueryProgressChanged(id, 1.0);
    fireQueryFinished(id);

    return result;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final Long[] tagIds, final Timestamp from, final Timestamp to) {
    final Object id = fireQueryStarting();

    fireQueryProgressChanged(id, 0.0);

    // Filters out the tags and timespan that is asked for.
    final List<HistoryTagValueUpdate> result = new ArrayList<HistoryTagValueUpdate>();
    final List<Long> tagIdList = Arrays.asList(tagIds);
    for (final HistoryTagValueUpdate value : historyRecords) {
      if (tagIdList.contains(value.getId())) {
        if (from.compareTo(value.getServerTimestamp()) <= 0 && to.compareTo(value.getServerTimestamp()) >= 0) {
          result.add(value);
        }
      }
    }
    
    emulateWait(tagIds.length);

    fireQueryProgressChanged(id, 1.0);
    fireQueryFinished(id);
    return result;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final Long[] tagIds, final Timestamp from, final Timestamp to, final int maximumTotalRecords) {
    throw new UnsupportedOperationException("Trying to use a method that is not implemented");
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final Long[] tagIds, final int maximumTotalRecords) {
    throw new UnsupportedOperationException("Trying to use a method that is not implemented");
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final int maximumRecordsPerTag, final Long[] tagIds, final Timestamp from, final Timestamp to) {
    throw new UnsupportedOperationException("Trying to use a method that is not implemented");
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final int maximumRecordsPerTag, final Long[] tagIds) {
    throw new UnsupportedOperationException("Trying to use a method that is not implemented");
  }

  @Override
  public Collection<HistorySupervisionEvent> getInitialSupervisionEvents(final Timestamp initializationTime, final Collection<SupervisionEventRequest> requests) {
    final List<HistorySupervisionEvent> result = new ArrayList<HistorySupervisionEvent>();

    final Object id = fireQueryStarting();
    fireQueryProgressChanged(id, 0.0);

    // Filters out the events that is requested
    for (final SupervisionEventRequest request : requests) {
      for (HistorySupervisionEvent record : initialSupervisionRecords) {
        if (record.getEntityId().equals(request.getId())
            && record.getEntity().equals(request.getEntity())
            && record.getEventTime().compareTo(initializationTime) <= 0) {
          result.add(record);
          break;
        }
      }
      emulateWait(1);
      fireQueryProgressChanged(id, result.size() / (double) requests.size());
    }

    fireQueryProgressChanged(id, 1.0);
    fireQueryFinished(id);

    return result;
  }

  @Override
  public Collection<HistorySupervisionEvent> getSupervisionEvents(final Timestamp from, final Timestamp to, final Collection<SupervisionEventRequest> requests) {
    final Object id = fireQueryStarting();

    fireQueryProgressChanged(id, 0.0);

    // Filters out the events that is requested
    final List<HistorySupervisionEvent> result = new ArrayList<HistorySupervisionEvent>();
    
    for (final SupervisionEventRequest request : requests) { 
      for (final HistorySupervisionEvent event : supervisionRecords) {
        if (event.getEntityId().equals(request.getId())
          && event.getEntity().equals(request.getEntity())
          && event.getEventTime().compareTo(from) >= 0
          && event.getEventTime().compareTo(to) <= 0) {
          result.add(event);
          break;
        }
      }
      emulateWait(1);
      fireQueryProgressChanged(id, result.size() / (double) requests.size());
    }

    fireQueryProgressChanged(id, 1.0);
    fireQueryFinished(id);
    return result;
  }

  /**
   * Emulates the time it takes to get the records from the database
   */
  private void emulateWait(final int numberOfTags) {
    if (emulatedResponseTime != null) {
      try {
        Thread.sleep(emulatedResponseTime * numberOfTags);
      }
      catch (InterruptedException e) { }
    }
  }
  
  /**
   * 
   * @return If set, it will delay the responses with this amount of
   *         milliseconds per requested tag
   */
  public Long getEmulatedResponseTime() {
    return emulatedResponseTime;
  }

  /**
   * 
   * @param emulatedResponseTime
   *          If set, it will delay the responses with this amount of
   *          milliseconds per requested tag
   */
  public void setEmulatedResponseTime(final Long emulatedResponseTime) {
    this.emulatedResponseTime = emulatedResponseTime;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getDailySnapshotRecords(final Long[] tagIds, final Timestamp from, final Timestamp to) {
    return Collections.emptyList();
  }

  @Override
  public Timespan getDateLimits() {
    throw new RuntimeException("This function is not supported..");
  }

}
