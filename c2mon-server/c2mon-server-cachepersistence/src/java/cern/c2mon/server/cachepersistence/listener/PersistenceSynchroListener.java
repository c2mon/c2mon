/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cachepersistence.listener;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.context.SmartLifecycle;

import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.server.cache.C2monCacheWithListeners;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.shared.common.Cacheable;

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
public class PersistenceSynchroListener implements BufferedTimCacheListener<Long>, SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(PersistenceSynchroListener.class);
  
  /**
   * SynchroBuffer max time.
   */
  private int bufferMaxTime;
  
  /**
   * SynchroBuffer min time.
   */
  private int bufferMinTime;
  
  /**
   * SynchroBuffer window growth.
   */
  private int bufferWindowGrowth;
  
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
  
  /**
   * The constructor that should be used to instantiate a new
   * listener. This listener is then automatically registered
   * with the C2monCache provided.
   * 
   * @param timCache the cache the listener should listen to
   * @param batchPersistenceManager the DAO object that contains the logic
   *                  for persisting the cache elements
   */
  public PersistenceSynchroListener(final C2monCacheWithListeners<Long, ? extends Cacheable> timCache, final BatchPersistenceManager batchPersistenceManager) {    
    this.timCache = timCache;
    this.persistenceManager = batchPersistenceManager;
  }
  
  /**
   * Should be run on instantiation of this listener: it registers
   * to the provided C2monCache as a {@link BufferedTimCacheListener}.
   */
  @PostConstruct
  public void init() {   
    listenerContainer = timCache.registerKeyBufferedListener(this); 
  }

  @Override
  public void confirmStatus(Collection<Long> keyCollection) {    
    notifyElementUpdated(keyCollection); //also persist cache status on confirmation
  }

  @Override
  public void notifyElementUpdated(Collection<Long> keyCollection) {    
    persistenceManager.persistList(keyCollection);
  }
  
  @Override
  public boolean isAutoStartup() {   
    return false;
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
