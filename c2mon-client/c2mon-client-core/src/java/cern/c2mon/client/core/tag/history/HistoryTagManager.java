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
package cern.c2mon.client.core.tag.history;

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
import cern.c2mon.client.core.C2monHistoryManager;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.tag.HistoryTag;
import cern.c2mon.client.core.tag.history.HistoryTagConfigurationStatus.LoadingStatus;

/**
 * This class manages the {@link HistoryTag} with the different
 * {@link HistoryTagConfiguration}s to keep it from loading the same data over
 * and over again.
 * 
 * @author vdeila
 */
public final class HistoryTagManager {

  /** Log4j instance */
  private static final Logger LOG = Logger.getLogger(HistoryTagManager.class);
  
  /** The instance, if any */
  private static HistoryTagManager instance = null;
  
  /**
   * @return the instance of a HistoryTagManager 
   */
  public static synchronized HistoryTagManager getInstance() {
    if (instance == null) {
      LOG.debug("New instance is being created.");
      instance = new HistoryTagManager();
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
  private HistoryTagManager() {
    this.historyManager = C2monServiceGateway.getHistoryManager();
  }
  
  /**
   * @param configuration
   *          the configurations to subscribe to
   * @param historyTag
   *          the history tag which subscribes
   */
  public void subscribe(final HistoryTagConfiguration configuration, final HistoryTag historyTag) {
    HistoryTag otherHistoryTag = null;
    boolean didAdd = false;
    statesLock.writeLock().lock();
    try {
      if (states.get(configuration) == null) {
        states.put(configuration, new HistoryTagConfigurationStatus());
        didAdd = true;
      }
      else {
        final Iterator<HistoryTag> historyTagIterator = states.get(configuration).getHistoryTagsIterator();
        if (historyTagIterator.hasNext()) {
          otherHistoryTag = historyTagIterator.next();
        } 
      }
      states.get(configuration).addHistoryTag(historyTag);
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
      if (otherHistoryTag != null) {
        historyTag.onLoaded(configuration, otherHistoryTag.getData());
      }
      else {
        LOG.warn("Were not able to get the history data from another history tag, reloading data..");
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
      historyTag.onCancelled(configuration);
      break;
    default:
      considerStartingThread();
      break;
    }
  }
  
  /**
   * @param configuration
   *          the configuration to unsubscribe from
   * @param historyTag
   *          the history tag to unsubscribe
   */
  public synchronized void unsubscribe(final HistoryTagConfiguration configuration, final HistoryTag historyTag) {
    statesLock.writeLock().lock();
    try {
      final HistoryTagConfigurationStatus state = states.get(configuration);
      if (state != null) {
        state.removeHistoryTag(historyTag);
        if (state.getHistoryTagCount() == 0) {
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
              if (configuration.getDays() != null) {
                downloaderConfiguration.setNumberOfDays(configuration.getDays());
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
              final Iterator<HistoryTag> iterator = state.getHistoryTagsIterator();
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
            
            state.compareAndSetStatus(LoadingStatus.Loading, LoadingStatus.Ready);
            
            // Tells the listeners about the data
            final Iterator<HistoryTag> iterator = state.getHistoryTagsIterator();
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

