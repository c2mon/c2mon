/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.cachepersistence.listener;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import cern.c2mon.server.cache.C2monBufferedCacheListener;
import cern.c2mon.server.cache.C2monCacheWithListeners;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.daq.lifecycle.Lifecycle;

/**
 * A common implementation of the SynchroBufferListener
 * designed for the cache persistence mechanism (cache to
 * database). 
 * 
 * <p>One is instantiated for each cache for which updates
 * should be persisted (Tag caches, alarms).
 * 
 * @author Mark Brightwell
 *
 */
public class PersistenceSynchroListener implements C2monBufferedCacheListener<Long>, SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceSynchroListener.class);

  /**
   * The cache which will be persisted.
   */
  private C2monCacheWithListeners<Long, ? extends Cacheable> timCache;
  
  /**
   * The DAQ used to persist the Collection to
   * the database.
   */
  private BatchPersistenceManager persistenceManager;
  
  /**
   * Listener container lifecycle hook.
   */
  private Lifecycle listenerContainer;
  
  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  private int bufferedListenerPullFrequency;
  
  /**
   * The constructor that should be used to instantiate a new
   * listener. This listener is then automatically registered
   * with the C2monCache provided.
   * 
   * @param timCache the cache the listener should listen to
   * @param batchPersistenceManager the DAO object that contains the logic
   *                  for persisting the cache elements
   */
  public PersistenceSynchroListener(final C2monCacheWithListeners<Long, ? extends Cacheable> timCache,
                                    final BatchPersistenceManager batchPersistenceManager,
                                    final int bufferedListenerPullFrequency) {
    this.timCache = timCache;
    this.persistenceManager = batchPersistenceManager;
    this.bufferedListenerPullFrequency = bufferedListenerPullFrequency;
  }
  
  /**
   * Should be run on instantiation of this listener: it registers
   * to the provided C2monCache as a {@link C2monBufferedCacheListener}.
   */
  @PostConstruct
  public void init() {   
    listenerContainer = timCache.registerKeyBufferedListener(this, bufferedListenerPullFrequency);
  }

  @Override
  public void confirmStatus(Collection<Long> keyCollection) {    
    notifyElementUpdated(keyCollection); //also persist cache status on confirmation
  }

  @Override
  public String getThreadName() {
    return "CacheDbBackup";
  }

  @Override
  public void notifyElementUpdated(Collection<Long> keyCollection) {    
    persistenceManager.persistList(keyCollection);
  }
  
  @Override
  public boolean isAutoStartup() {   
    return true;
  }

  @Override
  public void stop(Runnable runnable) {
    stop();
    runnable.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    LOGGER.debug("Starting cache persistence listener for cache " + timCache.getClass().getSimpleName());
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {  
    LOGGER.debug("Stopping cache persistence listener for cache " + timCache.getClass().getSimpleName());
    listenerContainer.stop();
    running = false;    
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST;    
  }
}
