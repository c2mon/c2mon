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

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

/**
 * Abstract listener implementation that batches the notifications before
 * calling the BufferedListener
 * 
 * @author Mark Brightwell
 * @param <T> the type of cache object received
 * @param <S> the type of object passed to the listener
 *
 */
public abstract class AbstractBufferedCacheListener<T extends Cacheable, S> implements C2monCacheListener<T>, Lifecycle {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AbstractBufferedCacheListener.class);

  /**
   * Max number of objects passed to the listener.
   */
  private static final int MAX_TO_LISTENER = 20000;
  
  /**
   * Max sleep time between pulls (could be longer if previous task is longer)
   */
  private static final int SLEEP_BETWEEN_PULLS = 10000;
  
  /**
   * Queues keeping the keys for supported methods.
   */
  private LinkedBlockingQueue<S> onUpdateQueue = new LinkedBlockingQueue<S>();  
  private LinkedBlockingQueue<S> statusConfirmationQueue = new LinkedBlockingQueue<S>();
  
  /**
   * Wrapped listener.
   */
  private BufferedTimCacheListener<S> bufferedKeyTimCacheListener;
  
  /**
   * Indicates if the listener is enabled (if not, notifications are ignored and exception is thrown).
   */
  private volatile boolean enabled;
  
  /**
   * Notice that duplicate policy is ignored if finite capacity is set (duplicate.OK is set) - TODO rewrite/adapt SynchroBuffer for this...
   * @param minWindowSize
   * @param maxWindowSize
   * @param windowGrowthFactor
   * @param duplicatePolicy
   * @param capacity
   * @param bufferedKeyTimCacheListener
   */
  public AbstractBufferedCacheListener(BufferedTimCacheListener<S> bufferedKeyTimCacheListener) {    
    this.bufferedKeyTimCacheListener = bufferedKeyTimCacheListener; 
    enabled = false;
  }  

  abstract S getDerivedObject(T cacheable);    
  
  @Override
  public void confirmStatus(T cacheable) {   
    if (enabled) {      
      try {
        statusConfirmationQueue.put(getDerivedObject(cacheable));
      } catch (InterruptedException e) {
        LOGGER.error("Interrupted while waiting to insert into queue.", e);
      }       
    } else {
      String errorMessage = "Updated notification received with listener disabled."; 
      LOGGER.warn(errorMessage);
      throw new IllegalStateException(errorMessage);
    }
  }



  @Override
  public void notifyElementUpdated(T cacheable) {
    if (enabled) {           
      try {
        onUpdateQueue.put(getDerivedObject(cacheable));
      } catch (InterruptedException e) {
        LOGGER.error("Interrupted while waiting to insert key into queue.", e);
      }      
    } else {
      String errorMessage = "Updated notification received with listener disabled."; 
      LOGGER.warn(errorMessage);
      throw new IllegalStateException(errorMessage);
    }
  }

  @Override
  public synchronized boolean isRunning() {
    return enabled;
  }

  /**
   * Synchronized to prevent simultaneous start/stop.
   */
  @Override
  public synchronized void start() {
    if (!enabled) {
      LOGGER.debug("Starting BufferedCacheListener");
      new Thread(new Runnable() {
        
        @Override
        public void run() {
          while (enabled || !onUpdateQueue.isEmpty() || !statusConfirmationQueue.isEmpty()) {
            long millisStart = System.currentTimeMillis();
            
            if (!onUpdateQueue.isEmpty()) {
              LinkedList<S> updateKeys = new LinkedList<S>();
              onUpdateQueue.drainTo(updateKeys, MAX_TO_LISTENER);
              if (!updateKeys.isEmpty()) {
                bufferedKeyTimCacheListener.notifyElementUpdated(updateKeys);
              }
            }
            
            if (!statusConfirmationQueue.isEmpty()) {
              LinkedList<S> confirmationKeys = new LinkedList<S>();
              statusConfirmationQueue.drainTo(confirmationKeys, MAX_TO_LISTENER);
              if (!confirmationKeys.isEmpty()) {
                bufferedKeyTimCacheListener.confirmStatus(confirmationKeys);
              }
            }
            
            while (System.currentTimeMillis() - millisStart < SLEEP_BETWEEN_PULLS) {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                LOGGER.error("Sleep interrupted in BufferedCacheListener thread.");
              }
            }
          }
          
        }
        
      }).start();
      enabled = true;      
    }        
  }

  @Override
  public synchronized void stop() {
    if (enabled) {
      LOGGER.debug("Shutting down BufferedKeyCacheListener");
      enabled = false;
      while (!onUpdateQueue.isEmpty() || !statusConfirmationQueue.isEmpty()) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          LOGGER.error("Interrupted while shutting down BufferedKeyCacheListener", e);
        }
      }
    }     
  }


  
}
