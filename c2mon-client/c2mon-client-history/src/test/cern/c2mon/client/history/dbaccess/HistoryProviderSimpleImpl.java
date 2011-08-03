package cern.c2mon.client.history.dbaccess;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.SupervisionEventRequest;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionConstants.SupervisionEntity;

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
   */
  public HistoryProviderSimpleImpl(final List<HistoryTagValueUpdate> initialRecords, final List<HistoryTagValueUpdate> historyRecords) {
    this.initialRecords = initialRecords;
    this.historyRecords = historyRecords;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getInitialValuesForTags(final Long[] pTagIds, final Timestamp before) {
    final List<HistoryTagValueUpdate> result = new ArrayList<HistoryTagValueUpdate>();

    fireQueryStarting();
    fireQueryProgressChanged(0.0);

    for (int i = 0; i < pTagIds.length; i++) {
      final Long searchingTagId = pTagIds[i];
      for (final HistoryTagValueUpdate record : initialRecords) {
        if (searchingTagId.equals(record.getId()) && before.compareTo(record.getServerTimestamp()) > 0) {
          result.add(record);
          break;
        }
      }
      
      if (emulatedResponseTime != null) {
        try {
          Thread.sleep(emulatedResponseTime);
        }
        catch (InterruptedException e) { }
      }

      fireQueryProgressChanged(i / (double) pTagIds.length);
    }

    fireQueryProgressChanged(1.0);
    fireQueryFinished();

    return result;
  }

  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final Long[] tagIds, final Timestamp from, final Timestamp to) {
    fireQueryStarting();

    fireQueryProgressChanged(0.0);

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
    
    if (emulatedResponseTime != null) {
      try {
        Thread.sleep(emulatedResponseTime * tagIds.length);
      }
      catch (InterruptedException e) { }
    }

    fireQueryProgressChanged(1.0);
    fireQueryFinished();
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
  public Collection<SupervisionEvent> getSupervisionEvents(final Long id, final SupervisionEntity entity) {
    throw new UnsupportedOperationException("Trying to use a method that is not implemented");
  }

  @Override
  public Collection<SupervisionEvent> getSupervisionEvents(final Collection<SupervisionEventRequest> requests) {
    throw new UnsupportedOperationException("Trying to use a method that is not implemented");
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

}
