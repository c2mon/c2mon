package cern.c2mon.client.history.dbaccess;

import java.security.InvalidParameterException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.SavedHistoryEvent;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.history.ClientDataTagRequestCallback;
import cern.c2mon.client.history.dbaccess.beans.HistoryRecordBean;
import cern.c2mon.client.history.dbaccess.beans.SavedHistoryEventRecordBean;
import cern.c2mon.client.history.dbaccess.beans.SavedHistoryRequestBean;
import cern.c2mon.client.history.dbaccess.beans.ShortTermLogHistoryRequestBean;
import cern.c2mon.client.history.dbaccess.util.BeanConverterUtil;
import cern.c2mon.client.history.util.KeyForValuesMap;

/**
 * Implementation of the {@link HistoryProvider}<br/>
 * <br/>
 * Gets saved history events
 * 
 * @author vdeila
 */
class SqlHistoryEventsProviderDAO extends SqlHistoryProviderDAO {

  /** The logger instance */
  private static final Logger LOG = Logger.getLogger(SqlHistoryEventsProviderDAO.class);
  
  /**
   * ORA-01795: maximum number of expressions in a list is 1000
   * 
   * This is used to split up the sql statement into queries of 1000 expressions
   * at a time. (All the expressions in the where statement is one expression,
   * so one tag equals expression)<br/>
   */
  private static final Integer MAXIMUM_NUMBER_OF_TAGS_PER_QUERY = 900;
  
  /** the event id to get the data for */
  private final Long eventId;
  
  /** The event which this provider gets data for */
  private final SavedHistoryEvent event;
  
  /** All records retrieved from the database */
  private final KeyForValuesMap<Long, HistoryTagValueUpdate> allRecords;
  
  /** The list of tag ids which are loaded */
  private final List<Long> loadedTagIds;
  
  /**
   * @param event
   *          the event to get data for
   * @param sessionFactory
   *          The sql session factory from which the session will be created
   * @param clientDataTagRequestCallback
   *          Callback to get access to attributes in the
   *          {@link ClientDataTagValue}. Like for example the
   *          {@link ClientDataTagValue#getType()}
   */
  public SqlHistoryEventsProviderDAO(final SavedHistoryEvent event, final SqlSessionFactory sessionFactory, final ClientDataTagRequestCallback clientDataTagRequestCallback) {
    super(sessionFactory, clientDataTagRequestCallback);
    this.event = event;
    this.eventId = event.getId();
    this.allRecords = new KeyForValuesMap<Long, HistoryTagValueUpdate>();
    this.loadedTagIds = new ArrayList<Long>();
  }
  
  @Override
  public Timespan getDateLimits() {
    return new Timespan(event.getStartDate(), event.getEndDate());
  }
  
  /**
   * Checks which tag ids doesn't exist in {@link #allRecords}
   * 
   * @param tagIds
   *          the tag ids to search for
   * @return a list of tags ids which isn't available in {@link #allRecords}
   */
  private List<Long> getMissingTags(final Long[] tagIds) {
    final List<Long> tagIdsNotFound = new ArrayList<Long>();
    // Fills the result with the records already retrieved from the database
    for (Long tagId : tagIds) {
      if (tagId != null) {
        if (!this.loadedTagIds.contains(tagId)) {
          tagIdsNotFound.add(tagId);
        }
      }
    }
    return tagIdsNotFound;
  }
  
  /**
   * Gets all the records for the {@link #eventId} and <code>tagIds</code>. All
   * tag ids which isn't already in the {@link #allRecords} will be retrieved
   * from the database.
   * 
   * @param tagIds
   *          the tag ids to load into {@link #allRecords}
   */
  private synchronized void loadTags(final Long[] tagIds) {
    
    // Returns the list of tag ids which must be requested from the server
    final List<Long> tagIdsToRequest = getMissingTags(tagIds);
    
    final int tagIdsToRequestInitially =  tagIdsToRequest.size();
    
    if (!tagIdsToRequest.isEmpty()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(
            String.format("Getting data for the event id %d, for %d tag ids; %s", this.eventId, tagIdsToRequest.size(), tagIdsToRequest.toString()));
      }
      
      this.loadedTagIds.addAll(tagIdsToRequest);
      
      final int totalNumberOfTagsToRequest = tagIdsToRequest.size();
      // Tells the listeners that the query is starting
      final Object id = fireQueryStarting();
      
      final SqlSession session = getSessionFactory().openSession();
      try {
        final SavedHistoryMapper mapper = getSavedHistoryMapper(session);
        
        while (!tagIdsToRequest.isEmpty() && !isProviderDisabled()) {
          final int toIndex;
          if (MAXIMUM_NUMBER_OF_TAGS_PER_QUERY <= tagIdsToRequest.size()) {
            toIndex = MAXIMUM_NUMBER_OF_TAGS_PER_QUERY;
          }
          else {
            toIndex = tagIdsToRequest.size();
          }
          final List<Long> tagIdsToRequestSubList = tagIdsToRequest.subList(0, toIndex);
          final SavedHistoryRequestBean request = new SavedHistoryRequestBean(this.eventId, tagIdsToRequestSubList);
          final List<SavedHistoryEventRecordBean> requestResult = mapper.getRecords(request);
          
          if (LOG.isDebugEnabled()) {
            LOG.debug(String.format(
                "Retrieved %d records for %d tags from the database",
                requestResult.size(), tagIdsToRequestSubList.size()));
          }
          
          addRecords(requestResult);
          tagIdsToRequestSubList.clear();
          
          fireQueryProgressChanged(id, tagIdsToRequest.size() / (double) totalNumberOfTagsToRequest);
        }
        
        if (LOG.isDebugEnabled()) {
          LOG.debug(String.format(
              "All data retrieved for %d records",
              tagIdsToRequestInitially));
        }
      }
      finally {
        session.close();
      }
      
      fireQueryProgressChanged(id, 1.0);

      // Tells the listeners that the query is finished
      fireQueryFinished(id);
    }
  }
  
  /**
   * 
   * @param records the records to add to the {@link #allRecords} map
   */
  private void addRecords(final Collection<SavedHistoryEventRecordBean> records) {
    final List<Long> newTagIds = new ArrayList<Long>();
    // Converts the tags into TagValueUpdates
    for (final HistoryRecordBean bean : records) {
      final HistoryTagValueUpdate tagValueUpdate = 
        BeanConverterUtil.toTagValueUpdate(bean, getClientDataTagRequestCallback());
      if (tagValueUpdate != null) {
        if (this.allRecords.add(bean.getTagId(), tagValueUpdate)) {
          newTagIds.add(bean.getTagId());
        }
      }
    }
  }

  /**
   * @return a sorter sorting the list by timestamp descending
   */
  private Comparator<HistoryTagValueUpdate> getRecordsSorter() {
    return new Comparator<HistoryTagValueUpdate>() {
      @Override
      public int compare(final HistoryTagValueUpdate o1, final HistoryTagValueUpdate o2) {
        int result = compareTimestamps(o1.getServerTimestamp(), o2.getServerTimestamp());
        if (result == 0) {
          result = compareTimestamps(o1.getDaqTimestamp(), o2.getDaqTimestamp());
          if (result == 0) {
            result = compareTimestamps(o1.getLogTimestamp(), o2.getLogTimestamp());
          }
        }
        return result;
      }
    };
  }
  
  /**
   * 
   * @param t1
   *          a timestamp
   * @param t2
   *          a timestamp
   * @return <code>0</code> if any of the two parameters is null, else it
   *         compares the timestamps
   */
  private int compareTimestamps(final Timestamp t1, final Timestamp t2) {
    if (t1 != null && t2 != null) {
      return t1.compareTo(t2);
    }
    return 0;
  }
  
  /**
   * 
   * @param tagIds
   *          The tag ids to get the historical data for
   * @param from
   *          The start time
   * @param to
   *          The end time
   * @return A collection of all the records that was found for the given tag
   *         ids in the given time periode
   */
  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final Long[] tagIds, final Timestamp from, final Timestamp to) {
    return getHistory(new ShortTermLogHistoryRequestBean(tagIds, from, to));
  }

  /**
   * 
   * @param tagIds
   *          The tag ids to get the historical data for
   * @param from
   *          The start time
   * @param to
   *          The end time
   * @param maximumTotalRecords
   *          The maximum records to return in total
   * @return A collection of all the records that was found for the given tag
   *         ids in the given time periode
   */
  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final Long[] tagIds, final Timestamp from, final Timestamp to, final int maximumTotalRecords) {
    return getHistory(new ShortTermLogHistoryRequestBean(tagIds, from, to, maximumTotalRecords), false);
  }

  /**
   * 
   * @param tagIds
   *          The tag ids to get the historical data for
   * @param maximumTotalRecords
   *          The maximum records to return in total
   * @return A collection of all the records that was found for the given tag
   *         ids in the given time periode
   */
  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final Long[] tagIds, final int maximumTotalRecords) {
    return getHistory(new ShortTermLogHistoryRequestBean(tagIds, null, null, maximumTotalRecords), false);
  }

  /**
   * 
   * @param maximumRecordsPerTag
   *          The maximum records to return per tag
   * @param tagIds
   *          The tag ids to get the historical data for
   * @param from
   *          The start time
   * @param to
   *          The end time
   * 
   * @return A collection of all the records that was found for the given tag
   *         ids in the given time periode
   */
  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final int maximumRecordsPerTag, final Long[] tagIds, final Timestamp from, final Timestamp to) {
    return getHistory(new ShortTermLogHistoryRequestBean(tagIds, from, to, maximumRecordsPerTag), true);
  }

  /**
   * 
   * @param maximumRecordsPerTag
   *          The maximum records to return per tag
   * @param tagIds
   *          The tag ids to get the historical data for
   * @return A collection of all the records that was found for the given tag
   *         ids in the given time periode
   */
  @Override
  public Collection<HistoryTagValueUpdate> getHistory(final int maximumRecordsPerTag, final Long[] tagIds) {
    return getHistory(new ShortTermLogHistoryRequestBean(tagIds, null, null, maximumRecordsPerTag), true);
  }

  /**
   * 
   * @param providerRequest
   *          The request to request from the database. Is automatically split
   *          if there are too many tags for one request
   * @return A collection of records
   */
  protected Collection<HistoryTagValueUpdate> getHistory(final ShortTermLogHistoryRequestBean providerRequest) {
    return getHistory(providerRequest, false);
  }
  
  /**
   * 
   * @param request
   *          The request to request from the database. Is automatically split
   *          if there are too many tags for one request
   * @param maxRecordsIsPerTag
   *          <code>true</code> if the
   *          <code>providerRequest.getMaxRecords()</code> is per tag,
   *          <code>false</code> if it is max records in total
   * @return A collection of records that meets the condition of the parameters
   */
  private Collection<HistoryTagValueUpdate> getHistory(final ShortTermLogHistoryRequestBean request, final boolean maxRecordsIsPerTag) {
    if (request == null) {
      throw new InvalidParameterException("The 'request' parameter cannot be null!");
    }
    if (request.getTagIds() == null) {
      throw new InvalidParameterException("The 'request.getTagIds()' cannot be null!");
    }
    
    final Object queryId = fireQueryStarting();
    
    loadTags(request.getTagIds());
    
    if (LOG.isDebugEnabled()) {
      String fromTime = "null";
      String toTime = "null";
      if (request.getFromTime() != null) {
        fromTime = request.getFromTime().toString();
      }
      if (request.getToTime() != null) {
        toTime = request.getToTime().toString();
      }
      LOG.debug(String.format("Filtering data for %d tags. From '%s' to '%s'",
          request.getTagIds().length, fromTime, toTime));
    }
    
    final List<HistoryTagValueUpdate> result = new ArrayList<HistoryTagValueUpdate>();
    
    int progress = 0;
    for (Long tagId : request.getTagIds()) {
      final HistoryTagValueUpdate[] records = allRecords.getValues(tagId).toArray(new HistoryTagValueUpdate[0]);
      Arrays.sort(records, getRecordsSorter());
      
      Integer startIndex = null;
      Integer endIndex = null;
      
      // Finds the start index
      if (request.getFromTime() != null) {
        for (int i = 0; i < records.length; i++) {
          if (records[i].getServerTimestamp() == null) {
            LOG.error(String.format(
                "Server timestamp for a record from saved history events is null. (Event id: %d, tag id: %d)",
                this.eventId, tagId));
            continue;
          }
          if (!request.getFromTime().after(records[i].getServerTimestamp())) {
            startIndex = i;
            break;
          }
        }
      }
      else {
        startIndex = 0;
      }
      
      // Finds the end index
      if (request.getToTime() != null && startIndex != null) {
        for (int i = records.length - 1; i >= startIndex; i--) {
          if (records[i].getServerTimestamp() == null) {
            LOG.error(String.format(
                "Server timestamp for a record from saved history events is null. (Event id: %d, tag id: %d)",
                this.eventId, tagId));
            continue;
          }
          if (request.getToTime().after(records[i].getServerTimestamp())) {
            endIndex = i + 1;
            break;
          }
        }
      }
      else {
        endIndex = records.length - 1;
      }
      
      if (startIndex != null && endIndex != null) {
        final List<HistoryTagValueUpdate> tagRecords = 
          new ArrayList<HistoryTagValueUpdate>(Arrays.asList(records));
        
        // Removes the records before "fromTime" and after "toTime"
        if (endIndex != tagRecords.size()) {
          tagRecords.subList(endIndex, tagRecords.size()).clear();
        }
        if (startIndex != 0) {
          if (startIndex > tagRecords.size()) {
            tagRecords.clear();
          }
          else {
            tagRecords.subList(0, startIndex).clear();
          }
        }
        
        if (maxRecordsIsPerTag
            && request.getMaxRecords() != null 
            && tagRecords.size() > request.getMaxRecords()) {
          // Remove the last part of the list, cutting it down to the "maxRecords"
          tagRecords.subList(request.getMaxRecords(), tagRecords.size()).clear();
        }
        
        result.addAll(tagRecords);
      }
      // else there is nothing to add to the result
      
      progress++;
      fireQueryProgressChanged(queryId, progress / (double) request.getTagIds().length);
    }
    
    Collections.sort(result, getRecordsSorter());
    
    if (!maxRecordsIsPerTag
        && request.getMaxRecords() != null 
        && result.size() > request.getMaxRecords()) {
      // Remove the last part of the list, cutting it down to the "maxRecords"
      result.subList(request.getMaxRecords(), result.size()).clear();
    }
    
    fireQueryProgressChanged(queryId, 1.0);
    fireQueryFinished(queryId);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Filtering finish, returning %d records for %d tags", result.size(), request.getTagIds().length));
    }
    
    return result;
  }
  
  @Override
  public Collection<HistoryTagValueUpdate> getInitialValuesForTags(final Long[] tagIds, final Timestamp before) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Initial values is requested for %d tag ids before '%s'",
          tagIds.length, before.toString()));
    }
    return getHistory(new ShortTermLogHistoryRequestBean(tagIds, null, before));
  }

  @Override
  public Collection<HistoryTagValueUpdate> getDailySnapshotRecords(final Long[] tagIds, final Timestamp from, final Timestamp to) {
    return new ArrayList<HistoryTagValueUpdate>();
  }
  
  /**
   * @param session
   *          The session to get the mapper from
   * @return a saved history events mapper
   */
  private SavedHistoryMapper getSavedHistoryMapper(final SqlSession session) {
    return session.getMapper(SavedHistoryMapper.class);
  }

}
