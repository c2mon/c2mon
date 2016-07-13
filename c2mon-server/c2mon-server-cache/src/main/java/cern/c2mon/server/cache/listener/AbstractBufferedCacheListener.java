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
package cern.c2mon.server.cache.listener;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;
import org.springframework.beans.factory.annotation.Value;

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
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBufferedCacheListener.class);

  /**
   * Max number of objects passed to the listener.
   */
  private static final int MAX_TO_LISTENER = 20000;
  
  /**
   * Max sleep time between pulls (could be longer if previous task is longer)
   */
  private int frequency;
  
  /**
   * Queues keeping the keys for supported methods.
   */
  private LinkedBlockingQueue<S> onUpdateQueue = new LinkedBlockingQueue<S>();  
  private LinkedBlockingQueue<S> statusConfirmationQueue = new LinkedBlockingQueue<S>();
  
  /**
   * Wrapped listener.
   */
  private BufferedTimCacheListener<S> bufferedCacheListener;
  
  /**
   * Indicates if the listener is enabled (if not, notifications are ignored and exception is thrown).
   */
  private volatile boolean enabled;
  
  /**
   * Notice that duplicate policy is ignored if finite capacity is set (duplicate.OK is set) - TODO rewrite/adapt SynchroBuffer for this...

   * @param bufferedCacheListener listener expecting collections of cache objects
   * @param frequency the frequency (in ms) at which the buffer should be emptied
   */
  public AbstractBufferedCacheListener(BufferedTimCacheListener<S> bufferedCacheListener, int frequency) {
    this.bufferedCacheListener = bufferedCacheListener;
    this.frequency = frequency;
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
                bufferedCacheListener.notifyElementUpdated(updateKeys);
              }
            }
            
            if (!statusConfirmationQueue.isEmpty()) {
              LinkedList<S> confirmationKeys = new LinkedList<S>();
              statusConfirmationQueue.drainTo(confirmationKeys, MAX_TO_LISTENER);
              if (!confirmationKeys.isEmpty()) {
                bufferedCacheListener.confirmStatus(confirmationKeys);
              }
            }
            
            while (System.currentTimeMillis() - millisStart < frequency) {
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