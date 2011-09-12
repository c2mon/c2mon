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
package cern.c2mon.client.history.playback.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistorySupervisionEvent;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.HistoryUpdate;
import cern.c2mon.client.common.history.SupervisionEventRequest;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.common.history.id.HistoryUpdateId;
import cern.c2mon.client.common.history.id.SupervisionEventId;
import cern.c2mon.client.common.history.id.TagValueUpdateId;
import cern.c2mon.client.history.data.event.HistoryLoaderListener;
import cern.c2mon.client.history.data.filter.DailySnapshotSmartFilter;
import cern.c2mon.client.history.data.utilities.MemoryConsumptionAdviser;
import cern.c2mon.client.history.data.utilities.SpeedEstimate;
import cern.c2mon.client.history.data.utilities.StopWatch;
import cern.c2mon.client.history.data.utilities.WorkManager;
import cern.c2mon.client.history.playback.HistoryConfiguration;
import cern.c2mon.client.history.playback.exceptions.NoHistoryProviderAvailableException;
import cern.c2mon.client.history.util.DataIdUtil;

/**
 * Loads all history data and puts it into a {@link HistoryStore}
 * 
 * @author vdeila
 * 
 */
public class HistoryLoader {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(HistoryLoader.class);
  
  /**
   * The maximum memory consumption in percent of the total memory available
   * that can be used. A number between 0.0 - 1.0
   */
  private static final double MAXIMUM_MEMORY_CONSUMPTION_PERCENT = 0.75;

  /**
   * When estimating speed for the first time, it uses this speed to measure for
   * how much to load. The unit is how much is loaded in milliseconds of tag
   * data in total per millisecond.
   * <br/><code>history milliseconds / millisecond</code>
   */
  private static final double STARTING_SPEED = 24.0 * 60.0 * 60.0 * 1000.0 / (1000.0);
  
  /**
   * The number of threads to use when loading history from the STL (Short Term
   * Log)
   */
  private static final int THREAD_COUNT_ON_LOADING_HISTORY = 1;
  
  /**
   * The amount of time that should be targeted when loading the first data
   * frame
   */
   private static final long TARGET_MS_OF_LOADING_FIRST_BUNCH = 700;
  
  /**
  * The amount of time that should be targeted when loading a data frame
  */
  private static final long TARGET_MS_OF_LOADING_PER_BUNCH = 1000 * 5;
  
  /**
  * The minimum amount to load per tag. Used when estimating how much to load.
  */
  private static final long MINIMUM_MS_OF_LOADING_PER_TAG = 1000 * 60 * 4;
  
  /**
   * When calculating how much data to load, it uses this to snap to the closest
   * day, if the deviation limit is not reached
   */
  private static final double LOADING_TIMESTAMP_DEVIATION = 0.5;
  
  /**
   * When calling <code>stopLoading()</code>, how long should the function wait
   * for the buffering to finish..
   */
  private static final long WAIT_FOR_LOADING_TO_STOP_TIMEOUT = 30000;
  
  /**
   * The minimum amount of frames to split the loading into. For example if this
   * value is 10, and 50% of the total time is suggested to be loaded it will
   * only load 10% of it (100% / 10).
   */
  private static final double MINIMUM_AMOUNT_OF_FRAMES = 10.0;

  /**
   * If the <code>startBufferingProcess</code> fails it waits this amount of
   * time (milliseconds) before trying again
   */
  private static final long LOAD_HISTORY_SLEEP_TIME_ON_FAIL = 3000;
  
  /**
   * How many times in a row it must fail before giving up loading history. Used in
   * <code>startBufferingProcess</code> method
   */
  private static final int LOAD_HISTORY_ACCEPTED_FAILS_IN_A_ROW = 2;
  
  /** A list of action listeners */
  private final List<HistoryLoaderListener> historyLoaderListeners;

  /** A lock for historyLoaderListeners list */
  private final ReentrantReadWriteLock historyLoaderListenersLock;
  
  /**
   * <code>true</code> if the last invoked method on the listeners
   * was the "initializingHistoryStarting"<br/>
   * <code>false</code> if it was the "fireInitializingHistoryFinished"
   */
  private volatile boolean didLastFireInitializingHistoryStarting = false;

  /** Tags that is currently being loaded is registered here */
  private final WorkManager<Long> tagsLoading;

  /** The history provider, where all the history data is stored */
  private final HistoryStore historyStore;

  /**
   * getSpeed() will return how many milliseconds it takes to load 1 ms of tag
   * data.
   */
  private final SpeedEstimate tagLoadingSpeedEstimate;
  
  /** The thread doing the buffering */
  private Thread historyBufferingThread;
  
  /** The lock for accessing <code>historyBufferingThread</code> */
  private ReentrantReadWriteLock historyBufferingThreadLock = new ReentrantReadWriteLock();
  
  /** Is set to <code>true</code> if the buffering thread should stop */
  private Boolean stopBufferingThread;
  
  /** Lock for accessing <code>stopBufferingThread</code> */
  private final ReentrantReadWriteLock stopBufferingThreadLock = new ReentrantReadWriteLock();
  
  /** Lock to let only one thread at a time run the initializeTagHistory(...) command */
  private final ReentrantLock initializeTagHistoryLock = new ReentrantLock();
  
  /** The history configuration used to get the data */
  private HistoryConfiguration historyConfiguration = null;
  
  /** The memory consumption manager */
  private final MemoryConsumptionAdviser memoryConsumptionAdviser;
  
  /** The filtering of data tags based on daily snapshot data */
  private final DailySnapshotSmartFilter dailySnapshotFilter;
  
  /** <code>true</code> if the loading of initial data should be done */
  private boolean loadInitialData;
  
  /**
   * Constructor
   */
  public HistoryLoader() {
    this.dailySnapshotFilter = new DailySnapshotSmartFilter();
    this.tagLoadingSpeedEstimate = new SpeedEstimate(STARTING_SPEED);
    this.tagLoadingSpeedEstimate.setNewMeasurementsRatio(3);
    
    this.memoryConsumptionAdviser = new MemoryConsumptionAdviser();
    this.memoryConsumptionAdviser.setMaximumMemoryConsumption(MAXIMUM_MEMORY_CONSUMPTION_PERCENT);
    
    this.historyStore = new HistoryStore();
    this.tagsLoading = new WorkManager<Long>();
    this.historyLoaderListenersLock = new ReentrantReadWriteLock();
    this.historyLoaderListeners = new ArrayList<HistoryLoaderListener>();
    this.historyBufferingThread = null;
    this.stopBufferingThread = false;
    this.loadInitialData = true;
  }
  
  /**
   * Clears all cache data etc.
   */
  public void clear() {
    this.dailySnapshotFilter.clear();
    this.historyStore.clear();
  }
  
  /**
   * Call this method after registering any update listeners to the
   * {@link #getHistoryStore()}. Preferably called after registering a bunch of
   * tags, and not called in a loop.<br/>
   * <br/>
   * This function blocks while loading initial data, but returns when it starts
   * to load more data.
   */
  public void beginLoading() {
    initializeData();
  }
  
  /**
   * Loads initial history data from short term log and the supervision log. The
   * history is loaded into <code>dataTagHistories</code>.
   */
  private void initializeData() {
    
    // Start a batch on the history store (keeps it from updating before the
    // batch is finished)
    this.historyStore.setBatching(true);
    try {
      boolean bufferIntervalUpdated = false;
      
      if (historyStore.isUninitializedTags()) {
      
        try {
          this.initializeTagHistoryLock.lock();
          
          // Notifying listeners that we are starting to initialize history data
          fireInitializingHistoryStarting();
          
          final Collection<HistoryUpdateId> historyUpdateIds = historyStore.getUninitializedTags();
          
          fireInitializingHistoryProgress(String.format("Preparing to initialize history data (%d tags)", historyUpdateIds.size()));
          if (LOG.isDebugEnabled()) {
            LOG.debug(
                String.format("Preparing to initialize %d history tags",
                    historyUpdateIds.size()));
          }
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing tag history");
          }
          
          if (isLoading()) {
            fireInitializingHistoryProgress("Waiting for the buffering request to return");
            
            // If tags are already loading, we stop it while collecting initial data
            stopLoading();
            
            fireInitializingHistoryProgress("The buffering request returned, requesting initial history data");
          }
          else {
            fireInitializingHistoryProgress("Requesting initial history data");
          }
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Invalidates the client data tags which are to be loaded");
          }
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("History player started loading historical data for data tag IDs: " + historyUpdateIds);
          }
          
          // Loads the data at point 0 in time
          loadInitialDataValues(historyUpdateIds);
          
          fireInitializingHistoryProgress("Initial history data received, starting the buffering thread.");
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("All initial data is now loaded for the history player");
          }
          
          // Notifying listeners that we are finished initializing the history data
          fireInitializingHistoryFinished();
          
          // Allows the buffering thread to start
          try {
            stopBufferingThreadLock.writeLock().lock();
            stopBufferingThread = false;
          }
          finally {
            stopBufferingThreadLock.writeLock().unlock();
          }
        }
        finally {
          this.initializeTagHistoryLock.unlock();
        }
        
        bufferIntervalUpdated = true;
      }
      
      if (!historyStore.isLoadingComplete()) {
        // Starting buffering process
        startBufferingProcess();
      }
      
      if (bufferIntervalUpdated) {
        // Notifies the listeners that the initial values are loaded
        historyStore.firePlaybackBufferIntervalUpdated();
      }
    }
    finally { 
      // Ends this batch
      this.historyStore.setBatching(false);
    }
  }
  
  /**
   * Starts a new thread which keeps loading data.
   * This function returns immediately.
   */
  private void startBufferingProcess() {
    try {
      historyBufferingThreadLock.writeLock().lock();
      
      if (this.historyBufferingThread == null) {
        this.historyBufferingThread = new HistoryBufferingProcess();
        this.historyBufferingThread.start();
      }
    }
    finally {
      historyBufferingThreadLock.writeLock().unlock();
    }
  }
  
  /**
   * Stops loading processes. Blocks until the loading have stopped..
   */
  public void stopLoading() {
    // Notifies that the loading must stop
    try {
      stopBufferingThreadLock.writeLock().lock();
      stopBufferingThread = true;
    }
    finally {
      stopBufferingThreadLock.writeLock().unlock();
    }

    // Waits for the loading to stop
    final long timeout = System.currentTimeMillis() + WAIT_FOR_LOADING_TO_STOP_TIMEOUT;
    boolean historyBufferingThreadIsNull = false;
    boolean stillStoppingBufferingThread = true;
    
    while (!historyBufferingThreadIsNull
        && stillStoppingBufferingThread
        && timeout > System.currentTimeMillis()
        ) {
      
      try {
        Thread.sleep(300);
      }
      catch (InterruptedException e) { }
      
      // Get the state of the thread
      try {
        historyBufferingThreadLock.readLock().lock();
        historyBufferingThreadIsNull = historyBufferingThread == null;
      }
      finally {
        historyBufferingThreadLock.readLock().unlock();
      }
      
      // Checks if the thread still should stop
      stillStoppingBufferingThread = isStopBufferingThread();
    }
  }
  
  /**
   * 
   * @return <code>true</code> if the buffering thread is running
   */
  public boolean isLoading() {
    try {
      historyBufferingThreadLock.readLock().lock();
      return historyBufferingThread != null;
    }
    finally {
      historyBufferingThreadLock.readLock().unlock();
    }
  }

  /**
   * 
   * @return <code>true</code> if the buffering thread should stop, <code>false</code> otherwise
   */
  protected boolean isStopBufferingThread() {
    try {
      stopBufferingThreadLock.readLock().lock();
      return stopBufferingThread;
    }
    finally {
      stopBufferingThreadLock.readLock().unlock();
    }
  }
  
  /**
   * Loads the history data of the specified time
   * 
   * @param dataTagIds
   *          The tags to load
   * @param startTime
   *          The start time from where to start to load
   * @param endTime
   *          The end time of where to load to.
   * @return <code>true</code> on success, <code>false</code> if nothing was
   *         loaded because something went wrong
   */
  private boolean loadHistory(final Collection<Long> dataTagIds, final Timestamp startTime, final Timestamp endTime) {
    // Measures the time it takes to load the data
    final StopWatch stopWatch = StopWatch.start();
    
    // Keeps a count of how many threads is left
    final CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT_ON_LOADING_HISTORY);
    
    final ArrayList<Long> dataTagArrayList = new ArrayList<Long>(dataTagIds);
    int numberOfTagsTaken = 0;
    
    // Holds a count of how many errors that have occurred
    final AtomicInteger errorCount = new AtomicInteger();
    
    // Holds a count of how many records have been retrieved
    final AtomicInteger recordRetrievedCount = new AtomicInteger();
    
    // Divides the data tag ids between the threads, and starts them to load asynchronously
    for (int threadCount = 0; 
        threadCount < THREAD_COUNT_ON_LOADING_HISTORY; 
        threadCount++) {
      
      if (dataTagArrayList.size() > numberOfTagsTaken) {
        // As long as it is more tags to load
        int threadNumbersOfTags = (int) (dataTagArrayList.size() / (double) THREAD_COUNT_ON_LOADING_HISTORY);
        if (threadNumbersOfTags == 0) {
          threadNumbersOfTags = 1;
        }
        if (threadCount + 1 == THREAD_COUNT_ON_LOADING_HISTORY) {
          // If it is the last thread to be started, it must take the rest of the tags
          threadNumbersOfTags = dataTagArrayList.size() - numberOfTagsTaken;
        }
        
        final List<Long> threadDataTags = new ArrayList<Long>(dataTagArrayList.subList(numberOfTagsTaken, numberOfTagsTaken + threadNumbersOfTags));
        
        // Loads the history asynchronously
        loadHistoryAsync(threadDataTags, startTime, endTime, countDownLatch, errorCount, recordRetrievedCount);
        
        numberOfTagsTaken += threadNumbersOfTags;
      }
      else {
        // If it is no more data tags to get we don't need to run another thread
        // Usually the thread counts down 
        countDownLatch.countDown();
      }
    }

    // Waiting for the threads to finish
    try {
      countDownLatch.await();
    }
    catch (InterruptedException e) {
      LOG.warn("The thread was interrupted while waiting for the threads to load history data", e);
      return false; 
    }
    
    if (errorCount.get() == 0) {
      if (numberOfTagsTaken > 0) {
        if (recordRetrievedCount.get() / (double) dataTagIds.size() < dataTagIds.size() * 10.0) {
          // If the average number of records per tag is less than 10,
          // the measurement will not be correct as it then is faster to 
          // ask for a larger amount of time with less requests.
          this.tagLoadingSpeedEstimate.addMeasurement(
              this.calculateMsLoaded(dataTagIds.size(), startTime, endTime),
              ((Double) (stopWatch.stop() / 3.0)).longValue());
        }
        else {
          // Adds how much time was used to get the historical data to the speed estimating.
          // Is added to the mesaurement only if zero errors.
          this.tagLoadingSpeedEstimate.addMeasurement(this.calculateMsLoaded(dataTagIds.size(), startTime, endTime), stopWatch.stop());
        }
      }
    }
    else {
      if (errorCount.get() >= THREAD_COUNT_ON_LOADING_HISTORY) {
        // Returns false only if error occurred on all threads
        return false;
      }
    }
    return true;
  }
  
  /**
   * Loads history in an asynchronous thread.
   * 
   * @param dataTags
   *          The tags to load
   * @param startTime
   *          The start time from where to start to load
   * @param endTime
   *          The end time of where to load to.
   * @param countDownLatch
   *          Is counted down once when the thread is finished
   * @param errorCount
   *          If something goes wrong it increments one.
   * @param recordsRetrieved
   *          Adds the number of records that is retrieved to this count
   */
  private void loadHistoryAsync(final List<Long> dataTags, final Timestamp startTime, final Timestamp endTime, final CountDownLatch countDownLatch,
      final AtomicInteger errorCount, final AtomicInteger recordsRetrieved) {
    final Thread loadingThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final HistoryProvider historyProvider;
          try {
            historyProvider = getHistoryConfiguration().getHistoryProvider();
          }
          catch (NoHistoryProviderAvailableException e) {
            LOG.error("No history can be loaded, because no History Provider is available.", e);
            errorCount.incrementAndGet();
            return;
          }
          
          final Collection<HistoryTagValueUpdate> tagValues = historyProvider.getHistory(
              dataTags.toArray(new Long[0]), 
              startTime, 
              endTime);
          
          // Add the collection to the list of records
          recordsRetrieved.addAndGet(historyStore.addHistoryValues(
              DataIdUtil.convertTagIdsToDataIdCollection(dataTags),
              Arrays.asList(tagValues.toArray(new HistoryUpdate[0])),
              endTime));
        }
        catch (Exception e) {
          LOG.error("Error occured while trying to retrieve history data.", e);
        }
        finally {
          countDownLatch.countDown();
        }
      }
    });
    loadingThread.setName("TIM-History-Loader-Buffer-Loading-Thread");
    loadingThread.start();
  }

  /**
   * 
   * @param numberOfTags
   *          The number of tags loaded with the given start and end time
   * @param startTime
   *          The start time of the loading
   * @param endTime
   *          The end time of the loading
   * @return The number of milliseconds loaded in total for the given tag count
   */
  protected long calculateMsLoaded(final long numberOfTags, final Timestamp startTime, final Timestamp endTime) {
    return (endTime.getTime() - startTime.getTime()) * numberOfTags;
  }
  
  /**
   * Estimates the end {@link Timestamp} when loading data, based on the given start time,
   * number of tags, and the desired loading time
   * 
   * @param numberOfTags The number of tags to load
   * @param startTime The start time you will be loading from
   * @param desiredLoadingTime The desired loading time in milliseconds
   * @return The end {@link Timestamp} to load to; startTime + the milliseconds of data to load
   */
  protected Timestamp estimateEndTimestamp(final int numberOfTags, final Timestamp startTime, final long desiredLoadingTime) {
    // Milliseconds of data to load = speed * the desired time of loading
    long estimatedHistoryTime = (long) (tagLoadingSpeedEstimate.getSpeed() * desiredLoadingTime);
    
    // Checks if it is too much loading at once, compared to the total time to load
    final long totalHistoryTimeToLoad = numberOfTags * (getHistoryConfiguration().getTimespan().getEnd().getTime() - getHistoryConfiguration().getTimespan().getStart().getTime());
    final long maximumHistoryTime = (long) (totalHistoryTimeToLoad / (double) MINIMUM_AMOUNT_OF_FRAMES);
    if (estimatedHistoryTime > maximumHistoryTime) {
      estimatedHistoryTime = maximumHistoryTime;
    }
    
    // Check if it is to little loading at once
    final long minumumHistoryTime = MINIMUM_MS_OF_LOADING_PER_TAG * numberOfTags;
    if (estimatedHistoryTime < minumumHistoryTime) {
      estimatedHistoryTime = minumumHistoryTime;
    }
    
    final long estimatedTimePerTag = (long) (estimatedHistoryTime / (double) numberOfTags);
    
    Timestamp endTimestamp = new Timestamp(startTime.getTime() + estimatedTimePerTag);
    
    // Snaps the end time to midnight if it is close enough.
    final int acceptedDeviationHours = (int) ((estimatedTimePerTag * LOADING_TIMESTAMP_DEVIATION) / (1000.0 * 60.0 * 60.0));
    
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(endTimestamp);
    if (calendar.get(Calendar.HOUR_OF_DAY) >= calendar.getActualMaximum(Calendar.HOUR_OF_DAY) - acceptedDeviationHours) {
      putCalendarToStartOfNextDay(calendar);
      endTimestamp = new Timestamp(calendar.getTimeInMillis());
    }
    else if (calendar.get(Calendar.HOUR_OF_DAY) <= calendar.getActualMinimum(Calendar.HOUR_OF_DAY) + acceptedDeviationHours) {
      putCalendarToStartOfNextDay(calendar);
      calendar.add(Calendar.DAY_OF_YEAR, -1);
      if (calendar.getTime().after(startTime)) {
        endTimestamp = new Timestamp(calendar.getTimeInMillis()); 
      }
    }

    if (endTimestamp.after(this.getHistoryConfiguration().getTimespan().getEnd())) {
      return this.getHistoryConfiguration().getTimespan().getEnd();
    }
    else {
      return endTimestamp;
    }
  }

  /**
   * 
   * @param historyUpdateIds
   *          the data ids to load the first value for
   */
  private void loadInitialDataValues(final Collection<HistoryUpdateId> historyUpdateIds) {
    final List<SupervisionEventId> supervisionEventIds = new ArrayList<SupervisionEventId>();
    final List<TagValueUpdateId> tagValueUpdateIds = new ArrayList<TagValueUpdateId>();
    for (final HistoryUpdateId historyUpdateId : historyUpdateIds) {
      if (historyUpdateId.isTagValueUpdateId()) {
        tagValueUpdateIds.add(historyUpdateId.getTagValueUpdateId());
      }
      else if (historyUpdateId.isSupervisionEventId()) {
        supervisionEventIds.add(historyUpdateId.getSupervisionEventId());
      }
      else {
        LOG.error(String.format("The HistoryUpdateId type \"%s\" is not supported.", historyUpdateId.getClass().getName()));
      }
    }
    
    fireInitializingHistoryProgress("Loading initialization data..");
    
    this.historyStore.setBatching(true);
    try {
    
      final int numberOfThreads;
      if (loadInitialData) {
        numberOfThreads = 3;
      }
      else {
        numberOfThreads = 2;
      }
      final CountDownLatch initialLoadingLatch = new CountDownLatch(numberOfThreads);
      
      new Thread("Initial-Supervision-Events-Loader-Thread") {
        @Override
        public void run() {
          try {
            loadInitialSupervisionEvents(supervisionEventIds);
          }
          catch (Exception e) {
            LOG.error("Error while getting inital supervision events", e);
          }
          finally {
            initialLoadingLatch.countDown();
          }
        }
      } .start();
      
      new Thread("Daily-Snapshot-Records-Loader-Thread") {
        @Override
        public void run() {
          try {
            loadDailySnapshotRecords(tagValueUpdateIds);
          }
          catch (Exception e) {
            LOG.error("Error while getting snapshot records", e);
          }
          finally {
            initialLoadingLatch.countDown();
          } 
        }
      } .start();
      
      if (loadInitialData) {
        new Thread("Initial-Data-Tags-Loader-Thread") {
          @Override
          public void run() {
            try {
              loadInitialDataTags(tagValueUpdateIds);
              fireInitializingHistoryProgress("Loading initialization data..");
            }
            catch (Exception e) {
              LOG.error("Error while getting inital data tag values", e);
            }
            finally {
              initialLoadingLatch.countDown();
            } 
          }
        } .start();
      }
      
      try {
        initialLoadingLatch.await();
      }
      catch (InterruptedException e) {
        LOG.error("Interrupted while waiting for initial data to be loaded.", e);
      }
    }
    finally {
      this.historyStore.setBatching(false);
    }
    
    fireInitializingHistoryProgress("Initial data is loaded");
  }
  
  /**
   * 
   * @param supervisionEventIds
   *          the supervision events to load
   */
  private void loadInitialSupervisionEvents(final Collection<SupervisionEventId> supervisionEventIds) {
    if (supervisionEventIds == null || supervisionEventIds.size() == 0) {
      return;
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          String.format("Loading initial supervision events (%d events)", supervisionEventIds.size()));
    }
    
    this.historyStore.addInitialTagValueUpdates(
        Arrays.asList(supervisionEventIds.toArray(new HistoryUpdateId[0])), 
        Arrays.asList(new HistoryUpdate[0]));
    
    final List<SupervisionEventRequest> requests = new ArrayList<SupervisionEventRequest>();
    for (SupervisionEventId supervisionEventId : supervisionEventIds) {
      requests.add(new SupervisionEventRequest(supervisionEventId.getEntityId(), supervisionEventId.getEntity()));
    }
    
    HistoryProvider historyProvider = null;
    
    try {
      historyProvider = getHistoryConfiguration().getHistoryProvider();
    }
    catch (NoHistoryProviderAvailableException e) {
      LOG.error("Unable to load the initial values, because no History Provider is available.", e);
      return;
    }
    
    // Getting initial values
    final Collection<HistorySupervisionEvent> initialValues = 
      historyProvider.getInitialSupervisionEvents(historyStore.getStart(), requests);
    this.historyStore.addInitialTagValueUpdates(
        Arrays.asList(supervisionEventIds.toArray(new HistoryUpdateId[0])), 
        Arrays.asList(initialValues.toArray(new HistoryUpdate[0])));
    
    // Getting the rest of the values
    final Collection<HistorySupervisionEvent> values = 
      historyProvider.getSupervisionEvents(historyStore.getStart(), historyStore.getEnd(), requests);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          String.format("Initial supervision events loaded, adding values (%d values)", values.size()));
    }
    
    this.historyStore.addHistoryValues(
        Arrays.asList(supervisionEventIds.toArray(new HistoryUpdateId[0])), 
        Arrays.asList(values.toArray(new HistoryUpdate[0])), 
        getHistoryStore().getEnd());
    
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          String.format("Initial supervision event values added (%d values)", values.size()));
    }
  }
  
  /**
   * Loads the daily snapshot records and adds them to the {@link #dailySnapshotFilter}.
   * 
   * @param tagValueUpdateIds
   */
  private void loadDailySnapshotRecords(final Collection<TagValueUpdateId> tagValueUpdateIds) {
    final List<Long> tagIds = new ArrayList<Long>();
    for (final TagValueUpdateId tagValueUpdateId : tagValueUpdateIds) {
      tagIds.add(tagValueUpdateId.getTagId());
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Loading daily snapshot records from the database (for %d tags)", tagIds.size()));
    }
    
    Collection<HistoryTagValueUpdate> values = null;
    try {
      values = getHistoryConfiguration().getHistoryProvider().getDailySnapshotRecords(tagIds.toArray(new Long[0]), this.getHistoryConfiguration().getTimespan().getStart(), this.getHistoryConfiguration().getTimespan().getEnd());
    }
    catch (NoHistoryProviderAvailableException e) {
      LOG.error("Unable to load the daily snaphot records, because no History Provider is available.", e);
      return;
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Daily snapshot records retrieved, filtering...");
    }
    
    // Adds the data to the daily snapshot filter
    this.dailySnapshotFilter.addDailySnapshotValues(values);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Daily snapshot records is loaded");
    }
  }
  
  /**
   * Loads the first value of the data tags history from short term log. This is
   * necessary in order to be able to initialize the data tags at time t0. The
   * value is added to the tag in <code>dataTagHistories</code>.
   * 
   * @param tagValueUpdateIds
   *          The TagValueUpdateIds to get the first value for
   */
  private void loadInitialDataTags(final Collection<TagValueUpdateId> tagValueUpdateIds) {
    if (tagValueUpdateIds == null || tagValueUpdateIds.size() == 0) {
      fireInitializingHistoryProgress("No tags to load");
      return;
    }
    
    final List<Long> tagIds = new ArrayList<Long>();
    for (final TagValueUpdateId tagValueUpdateId : tagValueUpdateIds) {
      tagIds.add(tagValueUpdateId.getTagId());
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Loading initial values from the database (for %d tags)", tagIds.size()));
    }
    fireInitializingHistoryProgress("Loading initial values from the short term log");
    
    // Gets the initial data
    Collection<HistoryTagValueUpdate> values = null;
    
    try {
      values = getHistoryConfiguration().getHistoryProvider().getInitialValuesForTags(tagIds.toArray(new Long[0]), this.getHistoryConfiguration().getTimespan().getStart());
    }
    catch (NoHistoryProviderAvailableException e) {
      LOG.error("Unable to load the initial values, because no History Provider is available.", e);
      return;
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Initial values loaded, storing and filtering the data");
    }
    fireInitializingHistoryProgress("Initial data loaded, storing and filtering the data");
    
    // Adds the tags to the history store
    this.historyStore.addInitialTagValueUpdates(Arrays.asList(tagValueUpdateIds.toArray(new HistoryUpdateId[0])), 
        Arrays.asList(values.toArray(new HistoryUpdate[0])));
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Initial values is filtered, notifying view(s)");
    }
    fireInitializingHistoryProgress("Initial data is loaded");
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Initial values loading is complete");
    }
  }
  
  /*
   * Methods to invoke methods on the listeners
   */
  
  /**
   * Invokes the method on the listeners
   */
  private void fireInitializingHistoryStarting() {
    if (!didLastFireInitializingHistoryStarting) {
      didLastFireInitializingHistoryStarting = true;
      for (HistoryLoaderListener listener : getHistoryLoaderListeners()) {
        listener.onInitializingHistoryStarting();
      }
    }
  }
  
  /**
   * Invokes the method on the listeners
   */
  private void fireInitializingHistoryFinished() {
    if (didLastFireInitializingHistoryStarting) {
      didLastFireInitializingHistoryStarting = false;
      for (HistoryLoaderListener listener : getHistoryLoaderListeners()) {
        listener.onInitializingHistoryFinished();
      }
    }
  }
  
  /**
   * Invokes the method on the listeners
   * 
   * @param progressMessage The progress message to give to the listeners
   */
  private void fireInitializingHistoryProgress(final String progressMessage) {
    for (HistoryLoaderListener listener : getHistoryLoaderListeners()) {
      listener.onInitializingHistoryProgressStatusChanged(progressMessage);
    }
  }
  
  /*
   * Methods for adding and removing listeners
   */

  /**
   * 
   * @return All the HistoryLoaderListener listeners
   */
  protected HistoryLoaderListener[] getHistoryLoaderListeners() {
    try {
      this.historyLoaderListenersLock.readLock().lock();
      return this.historyLoaderListeners.toArray(new HistoryLoaderListener[0]);
    }
    finally {
      this.historyLoaderListenersLock.readLock().unlock();
    }
  }

  /**
   * 
   * @param listener
   *          The listener to add
   */
  public void addHistoryLoaderListener(final HistoryLoaderListener listener) {
    try {
      this.historyLoaderListenersLock.writeLock().lock();
      this.historyLoaderListeners.add(listener);
    }
    finally {
      this.historyLoaderListenersLock.writeLock().unlock();
    }
  }

  /**
   * 
   * @param listener
   *          The listener to remove
   */
  public void removeHistoryLoaderListener(final HistoryLoaderListener listener) {
    try {
      this.historyLoaderListenersLock.writeLock().lock();
      this.historyLoaderListeners.remove(listener);
    }
    finally {
      this.historyLoaderListenersLock.writeLock().unlock();
    }
  }

  /**
   * 
   * @param historyConfiguration The new history configuration to set
   */
  public void setHistoryConfiguration(final HistoryConfiguration historyConfiguration) {
    this.historyConfiguration = historyConfiguration;
    this.historyStore.setStart(historyConfiguration.getTimespan().getStart());
    this.historyStore.setEnd(historyConfiguration.getTimespan().getEnd());
  }
  
  /**
   * 
   * @return The history configuration
   */
  public HistoryConfiguration getHistoryConfiguration() {
    return historyConfiguration;
  }
  
  /**
   * @return the historyStore
   */
  public HistoryStore getHistoryStore() {
    return historyStore;
  }
  
  /**
   * 
   * @param calendar
   *          Sets the time to the very beginning of the next day (relative to
   *          what it is set to)
   */
  private void putCalendarToStartOfNextDay(final Calendar calendar) {
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.add(Calendar.DAY_OF_YEAR, 1);
  }
  
  /** This threads loads data using the history provider */
  class HistoryBufferingProcess extends Thread {
    public HistoryBufferingProcess() {
      setName("TIM-History-Buffering");
    }
    
    @Override
    public void run() {
      try {
        runProcess();
      }
      catch (Exception e) {
        LOG.error("An error occured trying to load the history data..", e);
      }
    }
    
    /**
     * Starts the loading
     */
    private void runProcess() {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Buffering process is started");
      }
      
      // Which data frame is currently loaded
      long currentDataFrame = 0;
      
      // How many times loadHistory have returned false in a row
      int failsInARow = 0;
      
      // Set to true whenever you want to stop loading
      boolean stopLoading = false;
      
      boolean fireMemoryWarning = false;
      
      while (true) {
        fireMemoryWarning = !memoryConsumptionAdviser.haveEnoughMemory();
        // While the buffering thread should not stop and loading is not complete
        if (stopLoading
            || isStopBufferingThread() 
            || historyStore.isLoadingComplete()
            || fireMemoryWarning) {
          try {
            historyBufferingThreadLock.writeLock().lock();
            historyBufferingThread = null;
          }
          finally {
            historyBufferingThreadLock.writeLock().unlock();
          }
          break;
        }
        
        // Skips days that doesn't need to be loaded
        long totalSkippedTime = 0;
        for (final HistoryUpdateId historyUpdateId : historyStore.getRegisteredDataIds()) {
          if (historyUpdateId.isTagValueUpdateId() && historyStore.isTagInitialized(historyUpdateId)) {
            // Don't load the tag if it is not accepted
            final Timestamp tagIsLoadedUntil = historyStore.getTagHaveRecordsUntilTime(historyUpdateId);
            
            final Timespan skipTimespan = dailySnapshotFilter.getTimespan(historyUpdateId.getTagValueUpdateId().getTagId(), tagIsLoadedUntil);
            
            if (skipTimespan != null) {
              historyStore.addHistoryValues(
                    new ArrayList<HistoryUpdateId>(Arrays.asList(historyUpdateId)), 
                    new ArrayList<HistoryUpdate>(),
                    skipTimespan.getEnd());
              totalSkippedTime += skipTimespan.getEnd().getTime() - tagIsLoadedUntil.getTime();
            }
          }
        }
        
        if (totalSkippedTime > 0 && LOG.isDebugEnabled()) {
          LOG.debug(String.format("Smart filtering skipped loading %.2f hours of history", totalSkippedTime / (double) (60L * 60L * 1000L)));
        }
        
        // Get the time of which everything is loaded until
        Timestamp loadedUntilTime = historyStore.getHistoryIsLoadedUntilTime(true);
        
        // List of tags which should be loaded
        final List<Long> tagsWithOldestTime = new ArrayList<Long>();
        
        // Finds also the oldest timestamp
        Timestamp oldestTimestamp = getHistoryConfiguration().getTimespan().getEnd();
        
        for (int tryCount = 0; tryCount <= 1 && tagsWithOldestTime.size() <= 0; tryCount++) {
          
          if (tryCount == 1) {
            // If it doesn't find any tags the first time
            // it uses the oldest timestamp
            if (oldestTimestamp.before(getHistoryConfiguration().getTimespan().getEnd())) {
              loadedUntilTime = oldestTimestamp;
              if (LOG.isDebugEnabled()) {
                LOG.debug("Buffering: Didn't find any matching dates, using the oldest timestamp");
              }
            }
            else {
              if (LOG.isDebugEnabled()) {
                LOG.debug("Buffering: The oldest timestamp is the end timestamp.");
              }
            }
          }
          
          // Finds the tags to load
          for (final HistoryUpdateId historyUpdateId : historyStore.getRegisteredDataIds()) {
            if (historyUpdateId.isTagValueUpdateId() && historyStore.isTagInitialized(historyUpdateId)) {
              // Don't load the tag if it is not accepted
              final Timestamp tagIsLoadedUntil = historyStore.getTagHaveRecordsUntilTime(historyUpdateId);
              
              if (tagIsLoadedUntil.before(oldestTimestamp)) {
                oldestTimestamp = tagIsLoadedUntil;
              }
              
              if (tagIsLoadedUntil.equals(loadedUntilTime)) {
                tagsWithOldestTime.add(historyUpdateId.getTagValueUpdateId().getTagId());
              }
            }
          }
        }
        
        if (isStopBufferingThread()) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Buffering thread were interrupted from the loading loop");
          }
          continue;
        }
        
        // Registers that we want to load the given tags.
        // The returned tags is the tags which is not already being worked on.
        final Collection<Long> tagsToLoad = tagsLoading.registerWork(tagsWithOldestTime);
        
        if (tagsToLoad.size() == 0) {
          // If there for some reason is no tags to load
          if (tagsWithOldestTime.size() > 0) {
            // Checks if tags at the moment is being loaded
            // In that case, wait for a few seconds
            try {
              Thread.sleep(5000);
            }
            catch (InterruptedException e) { }
          }
          else {
            stopLoading = true;
            if (!historyStore.isLoadingComplete(true)) {
              LOG.error("The history is not fully loaded, but also no more loading can be done.. Canceling loading.. This may be due to timeout.");
            }
          }
        }
        else {
          // Load the tags
          
          // Gets the start time and estimates the ending time
          final Timestamp startTimestamp = new Timestamp(loadedUntilTime.getTime() + 1);
          final Timestamp endTimestamp;
          
          // Estimates the ending time given how long it wants to load
          if (currentDataFrame == 0) {
            // If it is the first frame, it targets a lower time for the retrieval. Choosing shorter time periode
            endTimestamp = estimateEndTimestamp(tagsToLoad.size(), startTimestamp, TARGET_MS_OF_LOADING_FIRST_BUNCH);
          }
          else {
            endTimestamp = estimateEndTimestamp(tagsToLoad.size(), startTimestamp, TARGET_MS_OF_LOADING_PER_BUNCH);
            
            if (LOG.isDebugEnabled()) {
              LOG.debug(String.format("Loading history at speed: %.2f hours of data per second", tagLoadingSpeedEstimate.getSpeed() / (60.0*60.0)));
            }
          }
          
          // Loads the history
          final boolean result = loadHistory(tagsToLoad, startTimestamp, endTimestamp);
          
          if (result) {
            failsInARow = 0;
            currentDataFrame ++;
          }
          else {
            failsInARow ++;
            if (failsInARow >= LOAD_HISTORY_ACCEPTED_FAILS_IN_A_ROW) {
              LOG.error(String.format(
                  "Loading history failed %d times in a row, no more histroy will be loaded..",
                  failsInARow));
              stopLoading = true;
            }
            else {
              LOG.warn("Loading history have failed, trying again");
              try {
                Thread.sleep(LOAD_HISTORY_SLEEP_TIME_ON_FAIL);
              }
              catch (InterruptedException e) { }
            }
          }
          
          // Unregisters the work on the tags
          tagsLoading.unregisterWork(tagsToLoad);
        }
        
      } // End of while loop
      
      if (fireMemoryWarning) {
        final Thread outOfMemoryInvokerThread = new Thread(new Runnable() {
          @Override
          public void run() {
            for (HistoryLoaderListener listener : getHistoryLoaderListeners()) {
              listener.onStoppedLoadingDueToOutOfMemory();
            }
          }
        });
        outOfMemoryInvokerThread.setName("TIM-Memory-Warning-Invoker-Thread");
        outOfMemoryInvokerThread.start();
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Buffering process terminates");
      }
      
      // Notifies the listeners of how much is loaded
      historyStore.firePlaybackBufferIntervalUpdated();
      
    }
  }
  
}
