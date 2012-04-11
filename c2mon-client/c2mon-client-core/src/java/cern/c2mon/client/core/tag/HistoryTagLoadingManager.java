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
package cern.c2mon.client.core.tag;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.HistoryLoadingConfiguration;
import cern.c2mon.client.common.history.HistoryLoadingManager;
import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryTagValueUpdate;
import cern.c2mon.client.common.history.exception.HistoryProviderException;
import cern.c2mon.client.common.history.tag.HistoryTagConfiguration;
import cern.c2mon.client.common.history.tag.HistoryTagManagerListener;
import cern.c2mon.client.common.history.tag.HistoryTagRecord;
import cern.c2mon.client.core.C2monHistoryManager;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.tag.HistoryTagConfigurationStatus.LoadingStatus;

/**
 * This class manages the {@link HistoryTagManagerListener}s with the different
 * {@link HistoryTagConfiguration}s to keep it from loading the same data
 * several times.
 * 
 * @author vdeila
 */
public final class HistoryTagLoadingManager {

  /** Log4j instance */
  private static final Logger LOG = Logger.getLogger(HistoryTagLoadingManager.class);
  
  /** The instance, if any */
  private static HistoryTagLoadingManager instance = null;
  
  /**
   * @return the instance of a HistoryTagManager 
   */
  public static synchronized HistoryTagLoadingManager getInstance() {
    if (instance == null) {
      LOG.debug("New instance is being created.");
      instance = new HistoryTagLoadingManager();
    }
    return instance;
  }
  
  /** The timeout of how long the {@link DownloadManager} will wait until it terminates */
  private static final long QUEUE_THREAD_TIMEOUT = 1000L * 60L;
  
  /** The maximum number of threads to download */
  private static final int MAXIMUM_NUMBER_OF_DOWNLOADER_THREADS = 3;
  
  /** Keeps track of what data is loaded */
  private final Map<HistoryTagConfiguration, HistoryTagConfigurationStatus> states = new HashMap<HistoryTagConfiguration, HistoryTagConfigurationStatus>();
  
  /** The lock for {@link #states} */
  private final ReentrantReadWriteLock statesLock = new ReentrantReadWriteLock();
  
  /** The history provider */
  private HistoryProvider historyProvider = null;
  
  /** The queue of the data to download */
  private BlockingQueue<HistoryTagConfiguration> downloadQueue = new LinkedBlockingQueue<HistoryTagConfiguration>(); 
  
  /** The number of threads that is running */
  private final AtomicInteger numberOfDownloadThreads = new AtomicInteger(0);
  
  /** The history manager */
  private final C2monHistoryManager historyManager;
  
  /** Singelton */
  private HistoryTagLoadingManager() {
    this.historyManager = C2monServiceGateway.getHistoryManager();
  }
  
  /**
   * @param configuration
   *          the configuration to subscribe to
   * @param listener
   *          the listener that subscribes to the data
   */
  public void subscribe(final HistoryTagConfiguration configuration, final HistoryTagManagerListener listener) {
    if (configuration == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(String.format("The %s '%s' was not subscribed", 
            HistoryTagManagerListener.class.getSimpleName(), listener.toString()));
      }
      return;
    }
    
    HistoryTagManagerListener otherSubscriber = null;
    boolean didAdd = false;
    statesLock.writeLock().lock();
    try {
      if (states.get(configuration) == null) {
        states.put(configuration, new HistoryTagConfigurationStatus());
        didAdd = true;
      }
      else {
        final Iterator<HistoryTagManagerListener> subscribersIterator = states.get(configuration).getSubscribersIterator();
        if (subscribersIterator.hasNext()) {
          otherSubscriber = subscribersIterator.next();
        } 
      }
      states.get(configuration).addSubscriber(listener);
    }
    finally {
      statesLock.writeLock().unlock();
    }

    if (didAdd) {
      downloadQueue.add(configuration);
    }
    
    final HistoryTagConfigurationStatus state = states.get(configuration);
    switch (state.getStatus()) {
    case Ready:
      if (otherSubscriber != null) {
        listener.onLoaded(configuration, otherSubscriber.getCurrentData(configuration));
      }
      else {
        // If there isn't any other HistoryTagManagerListener tag to get the data from it must be retrieved again,
        // ensuring that it has the latest data
        LOG.warn("Were not able to get the history data from another listener, reloading data..");
        statesLock.readLock().lock();
        try {
          states.get(configuration).compareAndSetStatus(state.getStatus(), LoadingStatus.NotInitialized);
        }
        finally {
          statesLock.readLock().unlock();
        }
        considerStartingThread();
      }
      break;
    case Invalid:
      listener.onCancelled(configuration);
      break;
    case Loading:
      break;
    default:
      considerStartingThread();
      break;
    }
  }
  
  /**
   * @param configuration
   *          the configuration to subscribe to
   * @param listener
   *          the listener that subscribes to the data
   */
  public void unsubscribe(final HistoryTagConfiguration configuration, final HistoryTagManagerListener listener) {
    statesLock.writeLock().lock();
    try {
      final HistoryTagConfigurationStatus state = states.get(configuration);
      if (state != null) {
        state.removeSubscriber(listener);
        if (state.getSubscribersCount() == 0) {
          states.remove(configuration).compareAndSetStatus(LoadingStatus.NotInitialized, LoadingStatus.Invalid);
        }
      }
    }
    finally {
      statesLock.writeLock().unlock();
    }
  }
  
  /** Starts more downloading threads if necessary */
  private void considerStartingThread() {
    int downloadThreads = numberOfDownloadThreads.get();
    while (downloadQueue.size() >  downloadThreads
        && downloadThreads < MAXIMUM_NUMBER_OF_DOWNLOADER_THREADS) {
      if (numberOfDownloadThreads.compareAndSet(downloadThreads, downloadThreads + 1)) {
        new DownloadManager().start();
      }
      downloadThreads = numberOfDownloadThreads.get();
    }
  }
  
  /** The thread for downloading data */
  class DownloadManager extends Thread {
    /** Constructor */
    public DownloadManager() {
      super("History-Tag-Manager-Download-Thread");
      setDaemon(true);
    }
    
    @Override
    public void run() {
      try {
        while (true) {
          // Gets a configuration to download
          HistoryTagConfiguration configuration;
          try {
            configuration = downloadQueue.poll(QUEUE_THREAD_TIMEOUT, TimeUnit.MILLISECONDS);
          }
          catch (InterruptedException e) {
            LOG.debug("Interrupted while waiting in queue for a configuration to download", e);
            configuration = null;
          }
          if (configuration == null) {
            break;
          }
          
          // Gets the state
          HistoryTagConfigurationStatus state;
          statesLock.readLock().lock();
          try {
            state = states.get(configuration);
          }
          finally {
            statesLock.readLock().unlock();
          }
          
          // Checks that it still exists, and that it is not already downloaded, or canceled. 
          if (state != null && state.compareAndSetStatus(LoadingStatus.NotInitialized, LoadingStatus.Loading)) {
            Collection<HistoryTagValueUpdate> tagValueUpdates;
            try {
              final HistoryLoadingManager downloader = 
                historyManager.createHistoryLoadingManager(
                    getHistoryProvider(), 
                    Arrays.asList(configuration.getTagId()));
              final HistoryLoadingConfiguration downloaderConfiguration = new HistoryLoadingConfiguration();
              
              downloaderConfiguration.setLoadInitialValues(configuration.isInitialRecord());
              downloaderConfiguration.setLoadSupervisionEvents(configuration.isSupervision());
              if (configuration.getRecords() != null) {
                downloaderConfiguration.setMaximumRecords(configuration.getRecords());
              }
              if (configuration.getTotalMilliseconds() != null) {
                final Timestamp startTime = new Timestamp(System.currentTimeMillis() - configuration.getTotalMilliseconds());
                final Timestamp endTime = new Timestamp(System.currentTimeMillis());
                downloaderConfiguration.setStartTime(startTime);
                downloaderConfiguration.setEndTime(endTime);
              }
              
              downloader.setConfiguration(downloaderConfiguration);
              downloader.beginLoading(false);
              tagValueUpdates = downloader.getAllHistoryConverted(configuration.getTagId());
              if (tagValueUpdates == null) {
                tagValueUpdates = new ArrayList<HistoryTagValueUpdate>();
              }
            }
            catch (Exception e) {
              LOG.error(String.format("Something went wrong when trying to get history data for tag '%d'", configuration.getTagId()), e);
              state.compareAndSetStatus(LoadingStatus.Loading, LoadingStatus.Invalid);
              
              // Tells the listeners about the canceled download
              final Iterator<HistoryTagManagerListener> iterator = state.getSubscribersIterator();
              while (iterator.hasNext()) {
                try {
                  iterator.next().onCancelled(configuration);
                }
                catch (Exception e1) {
                  LOG.error("Something went wrong when trying to update a history tag.", e1);
                }
              }
              
              continue;
            }
            
            // Convert the data
            final List<HistoryTagRecord> convertedData = new ArrayList<HistoryTagRecord>();
            for (HistoryTagValueUpdate tagValueUpdate : tagValueUpdates) {
              convertedData.add(new HistoryTagRecord(tagValueUpdate));
            }
            
            if (state.compareAndSetStatus(LoadingStatus.Loading, LoadingStatus.Ready)) {
              // Tells the listeners about the data
              final Iterator<HistoryTagManagerListener> iterator = state.getSubscribersIterator();
              while (iterator.hasNext()) {
                try {
                  iterator.next().onLoaded(configuration, convertedData);
                }
                catch (Exception e) {
                  LOG.error("Something went wrong when trying to update a history tag.", e);
                }
              }
            }
          }
        }
      }
      catch (Exception e) {
        LOG.error("Something went wrong trying to download data for history tag.", e);
      }
      finally {
        numberOfDownloadThreads.decrementAndGet();
        considerStartingThread();
      }
    }
  }
  
  /**
   * @return a history provider, or <code>null</code> if a history provider
   *         could not be created
   */
  private synchronized HistoryProvider getHistoryProvider() {
    if (this.historyProvider == null) {
      try {
        this.historyProvider = historyManager.getHistoryProviderFactory().createHistoryProvider();
      }
      catch (HistoryProviderException e) {
        LOG.warn("Cannot create a history provider. History tags cannot load the data.", e);
        this.historyProvider = null;
      }
    }
    return this.historyProvider;
  }
  
}

