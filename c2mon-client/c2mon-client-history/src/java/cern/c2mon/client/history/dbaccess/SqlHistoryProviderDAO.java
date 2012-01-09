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
package cern.c2mon.client.history.dbaccess;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistorySupervisionEvent;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.SupervisionEventRequest;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.history.ClientDataTagRequestCallback;
import cern.c2mon.client.history.data.utilities.DateUtil;
import cern.c2mon.client.history.dbaccess.beans.DailySnapshotRequestBean;
import cern.c2mon.client.history.dbaccess.beans.HistoryRecordBean;
import cern.c2mon.client.history.dbaccess.beans.InitialRecordHistoryRequestBean;
import cern.c2mon.client.history.dbaccess.beans.ShortTermLogHistoryRequestBean;
import cern.c2mon.client.history.dbaccess.beans.SupervisionEventRequestBean;
import cern.c2mon.client.history.dbaccess.beans.SupervisionRecordBean;
import cern.c2mon.client.history.dbaccess.util.BeanConverterUtil;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * Implementation of the {@link HistoryProvider}<br/>
 * <br/>
 * Gets the data from sql through iBatis.<br/>
 * Support concurrency. Ie. can be queried from several threads at any time.
 * 
 * @author vdeila
 * 
 */
class SqlHistoryProviderDAO extends HistoryProviderAbs {

  /** The logger instance */
  private static final Logger LOG = Logger.getLogger(SqlHistoryProviderDAO.class);

  /** The number of days available from the database */
  private static final int NUMBER_OF_DAYS_AVAILABLE = 30;
  
  /**
   * ORA-01795: maximum number of expressions in a list is 1000
   * 
   * This is used to split up the sql statement into queries of 1000 expressions
   * at a time. (All the expressions in the where statement is one expression,
   * so one tag equals expression)<br/>
   */
  private static final Integer MAXIMUM_NUMBER_OF_TAGS_PER_QUERY = 900;

  /** The sql session factory from which the session will be created */
  private final SqlSessionFactory sessionFactory;
  
  /**
   * Callback to get access to attributes in the {@link ClientDataTagValue}.
   * Like for example the {@link ClientDataTagValue#getType()}
   */
  private final ClientDataTagRequestCallback clientDataTagRequestCallback;

  /**
   * 
   * @param sessionFactory
   *          The sql session factory from which the session will be created
   * @param clientDataTagRequestCallback
   *          Callback to get access to attributes in the
   *          {@link ClientDataTagValue}. Like for example the
   *          {@link ClientDataTagValue#getType()}
   */
  public SqlHistoryProviderDAO(final SqlSessionFactory sessionFactory, final ClientDataTagRequestCallback clientDataTagRequestCallback) {
    this.sessionFactory = sessionFactory;
    this.clientDataTagRequestCallback = clientDataTagRequestCallback;
  }

  @Override
  public Timespan getDateLimits() {
    return new Timespan(
        DateUtil.truncateToDay(
            DateUtil.addDays(
                System.currentTimeMillis(), 
                -NUMBER_OF_DAYS_AVAILABLE)),
        new Date(System.currentTimeMillis()));
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
   * @param providerRequest
   *          The request to request from the database. Is automatically split
   *          if there are too many tags for one request
   * @param maxRecordsIsPerTag
   *          <code>true</code> if the
   *          <code>providerRequest.getMaxRecords()</code> is per tag,
   *          <code>false</code> if it is max records in total
   * @return A collection of records that meets the condition of the parameters
   */
  private Collection<HistoryTagValueUpdate> getHistory(final ShortTermLogHistoryRequestBean providerRequest, final boolean maxRecordsIsPerTag) {
    if (isProviderDisabled()) {
      return new ArrayList<HistoryTagValueUpdate>();
    }
    
    // Tells the listeners that the query is starting
    final Object queryId = fireQueryStarting();

    // If there is no tagIds to fetch, return no elements
    if (providerRequest.getTagIds() == null || providerRequest.getTagIds().length == 0) {
      return new ArrayList<HistoryTagValueUpdate>();
    }

    // Oracle database can have a maximum of 1000 expressions in a query
    final int maximumNumberOfTags;
    if (MAXIMUM_NUMBER_OF_TAGS_PER_QUERY == null) {
      maximumNumberOfTags = providerRequest.getTagIds().length;
    }
    else {
      maximumNumberOfTags = MAXIMUM_NUMBER_OF_TAGS_PER_QUERY;
    }

    // This will contain the indexes of request.getTagIds() of
    // which tags will be retrieved in the same query
    final List<Integer> queryPlan = new ArrayList<Integer>();

    if (maxRecordsIsPerTag && providerRequest.getMaxRecords() != null) {
      // If the max records is per tag, it will have to get only one tag at a
      // time
      // That is, one query per tag
      for (int i = 0; i <= providerRequest.getTagIds().length; i++) {
        queryPlan.add(i);
      }
    }
    else {
      // If there are to many expressions for on query it will have to split up
      // the query
      // into multiple queries.
      for (int i = 0; i < providerRequest.getTagIds().length; i += maximumNumberOfTags) {
        queryPlan.add(i);
      }
      if (!queryPlan.get(queryPlan.size() - 1).equals(providerRequest.getTagIds().length)) {
        queryPlan.add(providerRequest.getTagIds().length);
      }
    }

    // List for the result
    final ArrayList<HistoryTagValueUpdate> result = new ArrayList<HistoryTagValueUpdate>(providerRequest.getTagIds().length);

    // The list which will be filled with the values to return
    final List<HistoryRecordBean> records = new ArrayList<HistoryRecordBean>();

    final SqlSession session = this.sessionFactory.openSession();
    try {
      final HistoryMapper historyMapper = getHistoryMapper(session);

      // Does the query / queries to the database
      for (int i = 0; i < queryPlan.size() - 1 && !isProviderDisabled(); i++) {

        // Gets the list of tags to query
        final Long[] tagIdsToQuery = Arrays.asList(providerRequest.getTagIds()).subList(queryPlan.get(i), queryPlan.get(i + 1)).toArray(new Long[0]);

        // Creates the actual request
        final ShortTermLogHistoryRequestBean request = new ShortTermLogHistoryRequestBean(providerRequest);
        request.setTagIds(tagIdsToQuery);

        // If the maximum records variable is for the total amount of records
        // it must decrease the "max records" increasingly by how many
        // records that have been retrieved so far
        if (!maxRecordsIsPerTag && providerRequest.getMaxRecords() != null) {
          request.setMaxRecords(providerRequest.getMaxRecords() - records.size());
        }

        // Does the call to the sql mapper
        // and adds the values to the list which will be returned
        final Collection<HistoryRecordBean> queryResult = historyMapper.getRecords(request);
        if (queryResult != null) {
          records.addAll(queryResult);
          for (HistoryRecordBean record : queryResult) {
            try {
              final HistoryTagValueUpdate tagValueUpdate = BeanConverterUtil.toTagValueUpdate(record, this.clientDataTagRequestCallback);
              result.add(tagValueUpdate);
            }
            catch (Exception e) {
              LOG.warn(
                  String.format("Failed to convert a bean into a %s", TagValueUpdate.class.getSimpleName()), e);
            }
            if (isProviderDisabled()) {
              break;
            }
          }
        }
        fireQueryProgressChanged(queryId, queryPlan.get(i + 1) / (double) providerRequest.getTagIds().length);
      }
    }
    finally {
      session.close();
      
      fireQueryProgressChanged(queryId, 1.0);

      // Tells the listeners that the query is finished
      fireQueryFinished(queryId);
    }
    return result;
  }

  /**
   * Gets the data tag values, which for each of them was at the end of the
   * given <code>day</code>.
   * 
   * @param tagIds
   *          The tag ids to get the initial value for
   * @param before
   *          The requested records will be having the value that they had on
   *          this time
   * @return The data tag values which was at the end of the given day for the
   *         given tag ids.
   */
  @Override
  public Collection<HistoryTagValueUpdate> getInitialValuesForTags(final Long[] tagIds, final Timestamp before) {
    if (isProviderDisabled() || tagIds.length == 0) {
      return new ArrayList<HistoryTagValueUpdate>();
    }
    
    // Tells the listener that a query is starting
    final Object queryId = fireQueryStarting();

    // List for the result
    final ArrayList<HistoryTagValueUpdate> result = new ArrayList<HistoryTagValueUpdate>(tagIds.length);

    if (tagIds.length == 0) {
      return result;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Retrieving %d initial data tags", tagIds.length));
    }
    final long startTimeTotal = System.currentTimeMillis();
    
    // Opens a session where the data is received from
    final SqlSession session = this.sessionFactory.openSession();
    try {
      final HistoryMapper historyMapper = getHistoryMapper(session);
      for (int i = 0; i < tagIds.length && !isProviderDisabled(); i++) {
        final Long tagId = tagIds[i];
        final long startTime = System.currentTimeMillis();
        final HistoryRecordBean record = historyMapper.getInitialRecord(new InitialRecordHistoryRequestBean(tagId, before));
        if (record != null) {
          try {
            final HistoryTagValueUpdate dataTagValue = BeanConverterUtil.toTagValueUpdate(record, this.clientDataTagRequestCallback);
            if (dataTagValue != null) {
              result.add(dataTagValue);
            }
          }
          catch (Exception e) {
            LOG.warn(
                String.format("Failed to convert a bean into a %s", TagValueUpdate.class.getSimpleName()), e);
          }
          if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Initial record for tag id %d took %.3f seconds", 
                tagId, 
                (System.currentTimeMillis() - startTime) / 1000.0 ));
          }
        }
        fireQueryProgressChanged(queryId, i / (double) tagIds.length);
      }
    }
    finally {
      session.close();
      
      fireQueryProgressChanged(queryId, 1.0);

      // Tells the listener that the query is finished
      fireQueryFinished(queryId);
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(String.format("Retrieved %d initial data tags. It took %.3f seconds", 
            tagIds.length, 
            (System.currentTimeMillis() - startTimeTotal) / 1000.0 ));
      }
    }

    return result;
  }
  
  @Override
  public Collection<HistoryTagValueUpdate> getDailySnapshotRecords(final Long[] tagIds, final Timestamp from, final Timestamp to) {
    // List for the result
    final ArrayList<HistoryTagValueUpdate> result = new ArrayList<HistoryTagValueUpdate>();
    
    if (isProviderDisabled()) {
      return result;
    }
    
    // Tells the listener that a query is starting
    final Object queryId = fireQueryStarting();

    try {
    
      if (tagIds.length == 0) {
        return result;
      }
  
      final List<Long> requests = new ArrayList<Long>(Arrays.asList(tagIds));
      
      // Opens a session where the data is received from
      final SqlSession session = this.sessionFactory.openSession();
      final List<HistoryRecordBean> allRecords = new ArrayList<HistoryRecordBean>();
      try {
        while (requests.size() > 0 && !isProviderDisabled()) {
          final HistoryMapper historyMapper = getHistoryMapper(session);
          int toIndex = MAXIMUM_NUMBER_OF_TAGS_PER_QUERY;
          if (toIndex > requests.size()) {
            toIndex = requests.size();
          }
          final List<Long> currentRequest = requests.subList(0, toIndex);
          final List<HistoryRecordBean> records = historyMapper.getDailySnapshotRecords(new DailySnapshotRequestBean(currentRequest.toArray(new Long[0]), from, to));
          allRecords.addAll(records);
          
          // Removes the requested elements from the list. (Also from the "requests" lists)
          currentRequest.clear();
          
          fireQueryProgressChanged(queryId, 1.0 - (requests.size() / (double) tagIds.length));
        }
      }
      finally {
        session.close();
      }
      
      // Converts the tags into TagValueUpdates
      for (final HistoryRecordBean bean : allRecords) {
        if (isProviderDisabled()) {
          break;
        }
        try {
          final HistoryTagValueUpdate tagValueUpdate = BeanConverterUtil.toTagValueUpdate(bean, this.clientDataTagRequestCallback);
          if (tagValueUpdate != null) {
            result.add(tagValueUpdate);
          }
        }
        catch (Exception e) {
          LOG.warn(
              String.format("Failed to convert a bean into a %s", TagValueUpdate.class.getSimpleName()), e);
        }
      }
    }
    finally {
      fireQueryProgressChanged(queryId, 1.0);
  
      // Tells the listener that the query is finished
      fireQueryFinished(queryId);
    }

    return result;
  }

  @Override
  public Collection<HistorySupervisionEvent> getInitialSupervisionEvents(final Timestamp initializationTime, final Collection<SupervisionEventRequest> requests) {
    final List<HistorySupervisionEvent> result = new ArrayList<HistorySupervisionEvent>();

    if (requests == null || requests.size() == 0 || isProviderDisabled()) {
      return result;
    }

    // Tells the listeners that a query is starting
    final Object queryId = fireQueryStarting();

    // Opens a session where the data is received from
    final SqlSession session = this.sessionFactory.openSession();
    try {
      final HistoryMapper historyMapper = getHistoryMapper(session);

      int progress = 0;

      for (final SupervisionEventRequest request : requests) {
        if (isProviderDisabled()) {
          break;
        }
        
        final Collection<SupervisionRecordBean> records = 
          historyMapper.getInitialSupervisionEvents(
              new SupervisionEventRequestBean(
                  request.getId(), 
                  request.getEntity(),
                  initializationTime,
                  null));

        // Adds the records from the query to the result, converted into
        // SupervisionEvent
        for (final SupervisionRecordBean record : records) {
          record.setInitialValue(true);
          try {
            result.add(BeanConverterUtil.toSupervisionEvent(record));
          }
          catch (Exception e) {
            LOG.warn(String.format("Failed to convert a bean into a %s", SupervisionEvent.class.getSimpleName()), e);
          }
        }

        progress++;
        fireQueryProgressChanged(queryId, progress / (double) requests.size());
      }

    }
    finally {
      session.close();
      
      fireQueryProgressChanged(queryId, 1.0);

      // Tells the listener that the query is finished
      fireQueryFinished(queryId);
    }
    
    return result;
  }
  
  /**
   * 
   * @param from
   *          the start time
   * @param to
   *          the end time
   * @param requests
   *          The supervision events that is requested
   * @return a collection of supervision events which matches any of the ones in
   *         the collection
   */
  @Override
  public Collection<HistorySupervisionEvent> getSupervisionEvents(final Timestamp from, final Timestamp to, final Collection<SupervisionEventRequest> requests) {

    final List<HistorySupervisionEvent> result = new ArrayList<HistorySupervisionEvent>();

    if (requests == null || requests.size() == 0 || isProviderDisabled()) {
      return result;
    }

    // Tells the listeners that a query is starting
    final Object queryId = fireQueryStarting();

    // Opens a session where the data is received from
    final SqlSession session = this.sessionFactory.openSession();
    try {
      final HistoryMapper historyMapper = getHistoryMapper(session);

      int progress = 0;

      for (final SupervisionEventRequest request : requests) {
        if (isProviderDisabled()) {
          break;
        }
        final Collection<SupervisionRecordBean> records = 
          historyMapper.getSupervisionEvents(
              new SupervisionEventRequestBean(
                  request.getId(), 
                  request.getEntity(),
                  from,
                  to));

        // Adds the records from the query to the result, converted into
        // SupervisionEvent
        for (final SupervisionRecordBean record : records) {
          try {
            result.add(BeanConverterUtil.toSupervisionEvent(record));
          }
          catch (Exception e) {
            LOG.warn(String.format("Failed to convert a bean into a %s", SupervisionEvent.class.getSimpleName()), e);
          }
        }

        progress++;
        fireQueryProgressChanged(queryId, progress / (double) requests.size());
      }

    }
    finally {
      session.close();
      
      fireQueryProgressChanged(queryId, 1.0);

      // Tells the listener that the query is finished
      fireQueryFinished(queryId);
    }

    return result;
  }

  /**
   * @param session
   *          The session to get the mapper from
   * @return the mapper
   */
  private HistoryMapper getHistoryMapper(final SqlSession session) {
    return session.getMapper(HistoryMapper.class);
  }

  /**
   * @return the sessionFactory
   */
  protected SqlSessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /**
   * @return the clientDataTagRequestCallback
   */
  protected ClientDataTagRequestCallback getClientDataTagRequestCallback() {
    return clientDataTagRequestCallback;
  }
}
