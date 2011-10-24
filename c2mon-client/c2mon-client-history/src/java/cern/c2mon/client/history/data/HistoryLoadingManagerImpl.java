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
package cern.c2mon.client.history.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.HistoryLoadingManager;
import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.SupervisionEventRequest;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.common.history.event.HistoryLoadingManagerListener;
import cern.c2mon.client.common.history.exception.LoadingParameterException;
import cern.c2mon.client.common.history.id.SupervisionEventId;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.history.data.filter.DailySnapshotSmartFilter;
import cern.c2mon.client.history.data.utilities.DateUtil;

/**
 * This class implements the {@link HistoryLoadingManager} and can be used to
 * load data from a {@link HistoryProvider}
 * 
 * @see HistoryProvider
 * @see HistoryLoadingManager
 * 
 * @author vdeila
 * 
 */
public class HistoryLoadingManagerImpl extends HistoryLoadingManagerAbs implements HistoryLoadingManager {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryLoadingManagerImpl.class);

  /** The timeout for waiting for the loading to stop */
  private static final long STOP_LOADING_TIMEOUT = 20000;
  
  /** The history provider to use */
  private final HistoryProvider historyProvider;
  
  /** The filtering of data tags based on daily snapshot data */
  private final DailySnapshotSmartFilter dailySnapshotFilter;

  /** The loading process, if it is running */
  private LoadingThread loadingThread = null;
  
  /** Lock for {@link #loadingThread} */
  private final ReentrantLock loadingThreadLock;

  /** The earliest time loaded (exluding the initial data) */
  private Timestamp earliestTimeLoaded;

  /** The latest time loaded */
  private Timestamp latestTimeLoaded;
  
  /**
   * 
   * @param historyProvider
   *          the history provider to use to get the history data
   */
  public HistoryLoadingManagerImpl(final HistoryProvider historyProvider) {
    super();

    this.earliestTimeLoaded = null;
    this.latestTimeLoaded = null;
    this.dailySnapshotFilter = new DailySnapshotSmartFilter();
    this.historyProvider = historyProvider;
    this.loadingThreadLock = new ReentrantLock();
  }

  @Override
  public void beginLoading(final boolean async) throws LoadingParameterException {
    boolean startThread = false;
    loadingThreadLock.lock();
    try {
      if (this.loadingThread == null) {
        validateConfiguration();
        setLoading(true);
        this.loadingThread = new LoadingThread();
        startThread = true;
      }
    }
    finally {
      loadingThreadLock.unlock();
    }
    if (startThread) {
      if (async) {
        this.loadingThread.start();
      }
      else {
        this.loadingThread.run();
      }
    }
  }
  
  /**
   * Validates the parameters {@link #getConfiguration()}
   * 
   * @throws LoadingParameterException
   *           if any of the parameters is invalid
   */
  private void validateConfiguration() throws LoadingParameterException {
    if (getConfiguration() == null) {
      throw new LoadingParameterException("No loading parameters is set.");
    }
    boolean numberOfDaysSet = getConfiguration().getNumberOfDays() != null;
    boolean maximumRecordsSet = getConfiguration().getMaximumRecords() != null;
    boolean startSet = getConfiguration().getStartTime() != null;
    boolean endSet = getConfiguration().getEndTime() != null;
    
    if (!numberOfDaysSet
        && !maximumRecordsSet
        && (!endSet || !startSet)) {
      throw new LoadingParameterException(
          "Invalid loading parameters set. Must at least have start and end, or number of days, or maximum records set.");
    }
  }
  
  /**
   * Loads the daily snapshots into the {@link #dailySnapshotFilter}
   * 
   * @param start
   *          the start day
   * @param end
   *          the end day
   */
  private void loadDailySnapshots(final Timestamp start, final Timestamp end) {
    this.dailySnapshotFilter.addDailySnapshotValues(
        this.historyProvider.getDailySnapshotRecords(getTagsToLoad().keySet().toArray(new Long[0]), start, end));
  }
  
  /**
   * The thread loading the data
   */
  class LoadingThread extends Thread {
    /** Constructor */
    public LoadingThread() {
      super("History-Loading-Thread");
    }
    
    @Override
    public void run() {
      // Notifying listeners
      for (HistoryLoadingManagerListener listener : getListeners()) {
        try {
          listener.onLoadingStarting();
        }
        catch (Exception e) {
          LOG.error("Error while notifying listener", e);
        }
      }
      
      final Map<Long, ClientDataTag> tags = getTagsToLoad();
      
      boolean allDailySnapshotsIsLoaded = false;
      
      if (getConfiguration().getStartTime() != null
          && getConfiguration().getEndTime() != null) {
        loadDailySnapshots(getConfiguration().getStartTime(), getConfiguration().getEndTime());
        allDailySnapshotsIsLoaded = true;
      }
      
      
      Timestamp loadingEndTime = getConfiguration().getEndTime();
      if (loadingEndTime == null) {
        loadingEndTime = DateUtil.latestTimeInDay(System.currentTimeMillis());
      }
      
      boolean maximumRecordsReached = false;
      boolean loadingTimesReached = false;
      boolean numberOfDaysReached = false;
      
      boolean startTimeReached = false;
      boolean endTimeReached = false;
      
      Integer numberOfDays;
      Integer numberOfRecords;
      if (getConfiguration().getMaximumRecords() == null) {
        numberOfRecords = null;
      }
      else {
        numberOfRecords = 0;
      }
      if (getConfiguration().getNumberOfDays() == null) {
        numberOfDays = null;
      }
      else {
        numberOfDays = 0;
      }
      
      Timestamp lastestTime = loadingEndTime;
      Timestamp earliestTime = loadingEndTime;
      
      Timestamp earliestTimeWithData = loadingEndTime;
      
      while (!maximumRecordsReached
          && !loadingTimesReached
          && !numberOfDaysReached
          && isLoading()
          && !earliestTime.before(getConfiguration().getEarliestTimestamp())) {
        
        // Sets the maxium number of records variable
        Integer maximumNumberOfRecords;
        if (numberOfRecords == null) {
          maximumNumberOfRecords = null;
        }
        else {
          maximumNumberOfRecords = getConfiguration().getMaximumRecords() - numberOfRecords;
        }
        
        // Sets the loading start time
        Timestamp loadingStartTime = getConfiguration().getStartTime();
        
        // If no loading start time is set
        // or there are more than one day to the start time,
        // it is set to the beginning of the day which is now going to be loaded
        if (loadingStartTime == null
            || !DateUtil.isDaysEqual(loadingStartTime.getTime(), loadingEndTime.getTime())) {
          loadingStartTime = DateUtil.earliestTimeInDay(loadingEndTime.getTime());
        }
        
        if (!allDailySnapshotsIsLoaded) {
          loadDailySnapshots(loadingStartTime, loadingEndTime);
        }
        
        // Filters out which tags to load
        final List<Long> tagsToLoad = new ArrayList<Long>();
        for (final Long tagId : tags.keySet()) {
          final Timespan timespan = dailySnapshotFilter.getTimespan(tagId, loadingStartTime);
          // Load the tag if the start or end time is outside of the timespan
          if (timespan == null
              || timespan.getStart().before(loadingStartTime)
              || timespan.getEnd().after(loadingEndTime)) {
            tagsToLoad.add(tagId);
          }
        }
        
        if (tagsToLoad.size() > 0) {
          Collection<HistoryTagValueUpdate> result;
          
          // Loads the data from history
          if (maximumNumberOfRecords == null) {
            result = historyProvider.getHistory(tagsToLoad.toArray(new Long[0]), loadingStartTime, loadingEndTime);
          }
          else {
            result = historyProvider.getHistory(tagsToLoad.toArray(new Long[0]), loadingStartTime, loadingEndTime, (int) maximumNumberOfRecords);
          }
          if (result != null) {
            addTagValueUpdates(result);
            
            if (numberOfRecords != null) {
              numberOfRecords += result.size();
            }
            
            if (result.size() > 0) {
              if (loadingStartTime.compareTo(earliestTimeWithData) < 0) {
                earliestTimeWithData = loadingStartTime;
              }
            }
          }
        }
        if (numberOfDays != null) {
          numberOfDays++;
        }
        
        if (loadingEndTime.compareTo(lastestTime) > 0) {
          lastestTime = loadingEndTime;
        }
        if (loadingStartTime.compareTo(earliestTime) < 0) {
          earliestTime = loadingStartTime;
        }
        
        // Checking which criterias is met
        maximumRecordsReached = 
          numberOfRecords != null
          && numberOfRecords >= getConfiguration().getMaximumRecords();
          
        numberOfDaysReached = 
          numberOfDays != null 
          && numberOfDays >= getConfiguration().getNumberOfDays();
          
        startTimeReached = 
          getConfiguration().getStartTime() != null
          && loadingStartTime.compareTo(getConfiguration().getStartTime()) <= 0;

        endTimeReached = 
            getConfiguration().getEndTime() != null
            && loadingEndTime.compareTo(getConfiguration().getEndTime()) <= 0;
         
        loadingTimesReached = startTimeReached && endTimeReached;
        
        // Sets new ending day
        loadingEndTime = DateUtil.latestTimeInDay(DateUtil.addDays(loadingEndTime.getTime(), -1).getTime());
        
      } // End of while
      
      HistoryLoadingManagerImpl.this.earliestTimeLoaded = earliestTime;
      HistoryLoadingManagerImpl.this.latestTimeLoaded = lastestTime;
      
      if (isLoading()) {
        // Gets supervision events
        final List <SupervisionEventRequest> supervisionRequests = new ArrayList<SupervisionEventRequest>();
        for (SupervisionEventId id : getSupervisionEventsToLoad()) {
          supervisionRequests.add(new SupervisionEventRequest(id.getEntityId(), id.getEntity()));
        }
        
        if (getConfiguration().isLoadInitialValues()) {
          // Gets initial records
          addTagValueUpdates(historyProvider.getInitialValuesForTags(
              tags.keySet().toArray(new Long[0]), 
              earliestTimeWithData));
          
          addSupervisionEvents(historyProvider.getInitialSupervisionEvents(
              earliestTimeWithData, 
              supervisionRequests));
        }
        
        // Loads the supervision events
        addSupervisionEvents(historyProvider.getSupervisionEvents(earliestTime, lastestTime, supervisionRequests));
      }
      
      // Notifying listeners
      for (HistoryLoadingManagerListener listener : getListeners()) {
        try {
          listener.onLoadingComplete();
        }
        catch (Exception e) {
          LOG.error("Error while notifying listener", e);
        }
      }
      
      // Stops loading.
      loadingThreadLock.lock();
      try {
        setLoading(false);
        synchronized (loadingThread) {
          loadingThread.notify();
        }
        loadingThread = null;
      }
      finally {
        loadingThreadLock.unlock();
      }
    }
    
  }

  @Override
  public void stopLoading(final boolean wait) {
    if (setLoading(false)) {
      if (wait) {
        synchronized (loadingThread) {
          try {
            loadingThread.wait(STOP_LOADING_TIMEOUT);
          }
          catch (InterruptedException e) { }
        }
      }
    }
  }

  @Override
  public Timestamp getEarliestTimeLoaded() {
    return this.earliestTimeLoaded;
  }

  @Override
  public Timestamp getLatestTimeLoaded() {
    return this.latestTimeLoaded;
  }

}
