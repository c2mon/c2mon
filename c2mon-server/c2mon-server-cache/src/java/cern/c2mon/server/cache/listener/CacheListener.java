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
package cern.c2mon.server.cache.listener;

import org.apache.log4j.Logger;
import org.springframework.context.support.ApplicationObjectSupport;

import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.util.threadhandler.ThreadHandler;

/**
 * Asynchronous cache listener that passes events received by the Ehcache listener
 * to the wrapped {@link C2monCacheListener} on a single thread.
 * 
 * <p>A new CacheListener is created each time a new (asynchronous) listener is registered, wrapping
 * the C2monCacheListener implemented by the module listener class.
 * 
 * <p>It instantiates threads for each cache notification method, and passes each
 * received object to the appropriate thread.
 * 
 * <p><b>This class is deprecated and the {@link MultiThreadedCacheListener} should
 * preferably be used instead (with #threads = 1 for a single-threaded listener)</b>
 * 
 * @author Mark Brightwell
 *
 */
public final class CacheListener<T extends Cacheable> extends ApplicationObjectSupport implements C2monCacheListener<T>, Lifecycle {

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(CacheListener.class);  
  
  /**
   * The threads dealing with updates.
   */
  private ThreadHandler notifyUpdateThreadHandler;
  private ThreadHandler statusConfirmationHandler;
      
  /**
   * Indicates if the listener is enabled (if not, notifications are ignored and exception is thrown).
   */
  private volatile boolean running = false;
  
  /**
   * Constructor returning a CacheListener wrapping the {@link C2monCacheListener}.
   * Instantiates and starts the required threads.
   * 
   * @param timCacheListener the wrapped listener object
   */
  public CacheListener(final C2monCacheListener<T> timCacheListener) {
    super();    
    
    try {
      this.notifyUpdateThreadHandler = new ThreadHandler(timCacheListener, C2monCacheListener.class.getMethod("notifyElementUpdated", new Class< ? >[] {Cacheable.class}));
      this.statusConfirmationHandler = new ThreadHandler(timCacheListener, C2monCacheListener.class.getMethod("confirmStatus", new Class< ? >[] {Cacheable.class}));
    } catch (SecurityException e) {
      LOGGER.error("Security exception caught.", e);
    } catch (NoSuchMethodException e) {
      LOGGER.error("No such method found in C2monCacheListener.", e);      
    }
        
  }


  @Override
  public void confirmStatus(final T cacheable) {
    statusConfirmationHandler.put(new Object[] {cacheable});
  }


  @Override
  public void notifyElementUpdated(final T cacheable) {  
    notifyUpdateThreadHandler.put(new Object[] {cacheable});
  }
 

  /**
   * For management purposes.
   * @return the size of the task queue for onUpdate calls
   */  
  public int getTaskQueueSize() {
    return notifyUpdateThreadHandler.getTaskQueueSize();
  }
  
  /**
   * For management purposes.
   * @return the size of the task queue for status confirmations
   */
  public int getConfirmStatusQueueSize() {
    return statusConfirmationHandler.getTaskQueueSize();
  }
  
  @Override
  public boolean isRunning() {
    return running;
  }

  /**
   * Synchronized to prevent simultaneous stop.
   */
  @Override
  public void start() {
    if (!running) {
      running = true;
      notifyUpdateThreadHandler.start();
      statusConfirmationHandler.start();
    }    
  }

  /**
   * Waits for all tasks to complete then shuts down the thread.
   * Should be called when the cache is closed on server shutdown.
   * Synchronized to prevent simultaneous start.
   */
  @Override
  public void stop() {
    if (running) {
      running = false;
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Shutting down CacheListener threads.");
      }
      notifyUpdateThreadHandler.shutdown(); 
      statusConfirmationHandler.shutdown();      
    }    
  }



}
