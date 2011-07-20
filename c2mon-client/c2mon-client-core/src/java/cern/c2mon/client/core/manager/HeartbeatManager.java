/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2011 CERN
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.core.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.C2monSupervisionManager;
import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.shared.client.supervision.Heartbeat;

/**
 * The heartbeat manager checks whether the C2MON server heartbeat has expired or not.
 * All registered listeners are informed about all occuring events (update, expiration, resumed heartbeat).
 * The <code>HeartbeatManager</code> belongs to the {@link C2monSupervisionManager} service and get
 * listener registration requests delegated by the <code>SupervisionManager</code> 
 *
 * @author Matthias Braeger
 * @see SupervisionManager
 */
@Service
class HeartbeatManager extends TimerTask implements cern.c2mon.client.jms.HeartbeatListener, HeartbeatListenerManager {

  /** Logger instance of this class */
  private static final Logger LOG = Logger.getLogger(HeartbeatManager.class);
  
  /** TimerTask thread synchronization Object */
  private final Object timerSync = new Object();

  /** Lock for the heartbeatListeners list */
  private final ReentrantReadWriteLock listenerLock = new ReentrantReadWriteLock();
  
  /**
   * Collection of all heartbeat listeners.
   */
  private List<HeartbeatListener> heartbeatListeners = new ArrayList<HeartbeatListener>();

  /**
   * Time stamp (in current millis) set when the last heartbeat was received.
   */
  private long lastHeartbeatReceived;
  
  /** Last heartbeat event received from the server */
  private Heartbeat lastHeartbeatEvent = null;
  
  /**
   * Heartbeat delay tolerance. The tolerance is set in the init() method
   * to 1/2 of the default hearbeat interval time.
   */
  private static final long TOLERANCE_IN_MILLIS = Heartbeat.getHeartbeatInterval() / 2;

  /**
   * Timer for periodically checking whether the last heartbeat received is
   * still valid.
   */
  private Timer heartbeatTimer;
  
  /** Is set to <code>true</code>, if the hearbeat is expired */
  private boolean heartbeatExpired = false;

  /** Reference to the JmsProxy singleton instance */
  private final JmsProxy jmsProxy;
  
  
  /**
   * Constructor
   */
  @Autowired
  protected HeartbeatManager(final JmsProxy pJmsProxy) {
    jmsProxy = pJmsProxy;
  }
  
  /**
   * Called by Spring after having created the service
   */
  @PostConstruct
  private void init() {
    synchronized (timerSync) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("init() : Initialising heartbeat mechanism");
      }
      
      // Initialise the last heartbeat received
      this.lastHeartbeatReceived = System.currentTimeMillis();
      this.lastHeartbeatEvent = new Heartbeat();
      
      // Create a Timer for periodically checking whether the last Heartbeat has
      // expired
      this.heartbeatTimer = new Timer();
      this.heartbeatTimer.scheduleAtFixedRate(this, Heartbeat.getHeartbeatInterval(), Heartbeat.getHeartbeatInterval());
      if (LOG.isDebugEnabled()) {
        LOG.debug("init() : Heartbeat mechanism started.");
      }
      
      // Register HeartbeatManager at JMS proxy
      jmsProxy.registerHeartbeatListener(this);
    }
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.core.manager.HeartbeatListenerManager#addHeartbeatListener(cern.c2mon.client.core.listener.HeartbeatListener)
   */
  public void addHeartbeatListener(final HeartbeatListener pListener) {
    listenerLock.writeLock().lock();
    try {
      if (!this.heartbeatListeners.contains(pListener)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("addHeartbeatListener() : new listener added.");
        }
        this.heartbeatListeners.add(pListener);
      }
      else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("addHeartbeatListener() : listener already in the list.");
        }
      }
    }
    finally {
      listenerLock.writeLock().unlock();
    }
  }

  /* (non-Javadoc)
   * @see cern.c2mon.client.core.manager.HeartbeatListenerManager#removeHeartbeatListener(cern.c2mon.client.core.listener.HeartbeatListener)
   */
  public void removeHeartbeatListener(final HeartbeatListener pListener) {
    listenerLock.writeLock().lock();
    try {
      this.heartbeatListeners.remove(pListener);
      if (LOG.isDebugEnabled()) {
        LOG.debug("addHeartbeatListener() : listener removed.");
      }
    }
    finally {
      listenerLock.writeLock().unlock();
    }
  }

  /**
   * run() method periodically called by the Timer.
   */
  public void run() {
    synchronized (timerSync) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("run() : Checking HeartbeatTimer.");
      }
  
      // If the heartbeat has expired. Don't do anything
      // until the next hearbeat is received.
      if (heartbeatExpired) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("run() : No Heartbeat received since timer expiration.");
        }
        return;
      }
      
      boolean hasExpired = System.currentTimeMillis() 
                           - lastHeartbeatReceived 
                           - TOLERANCE_IN_MILLIS 
                           > Heartbeat.getHeartbeatInterval(); 
      if (hasExpired) {
        this.heartbeatExpired = true;
        // If the last heartbeat received from the server has expired, invalidate 
        // all data tags.
        if (LOG.isInfoEnabled()) {
          LOG.info("run() : HeartbeatTimer has expired, notifying listeners");
        }
  
        // Notify listeners
        listenerLock.readLock().lock();
        try {
          for (HeartbeatListener listener : heartbeatListeners) {
            try {
              listener.onHeartbeatExpired(lastHeartbeatEvent);
            }
            catch (Exception e) {
              LOG.error("run() : error notifying listener of heartbeat expiration.", e);
            }
          }
        }
        finally {
          listenerLock.readLock().unlock();
        }
      }
    }
  }

  @Override
  public void onHeartbeat(final Heartbeat heartbeat) {
    synchronized (timerSync) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("onMessageReceived() : Heartbeat received. Notifying listeners.");
      }
  
      this.lastHeartbeatReceived = System.currentTimeMillis();
      this.lastHeartbeatEvent = heartbeat;
  
      // Check whether the heartbeat was expired
      if (heartbeatExpired) {
        heartbeatExpired = false;
        
        listenerLock.readLock().lock();
        try {
          if (LOG.isDebugEnabled()) {
            LOG.debug("onHeartbeat() - Heartbeat received after timer expiration. Notifying listeners .");
          }
          // Notify listeners of heartbeat reception
          for (HeartbeatListener listener : heartbeatListeners) {
            try {
              listener.onHeartbeatResumed(heartbeat);
            }
            catch (Exception e) {
              LOG.error("onHeartbeat() - error notifying a listener of heartbeat resume", e);
            }
          }
        }
        finally {
          listenerLock.readLock().unlock();
        }
      
      }
      else {
        // Notify listeners of heartbeat reception
        listenerLock.readLock().lock();
        try {
          for (HeartbeatListener listener : heartbeatListeners) {
            try {
              listener.onHeartbeatReceived(heartbeat);
            }
            catch (Exception e) {
              LOG.error("onHeartbeat() - error notify a listener of heartbeat reception", e);
            }
          }
        }
        finally {
          listenerLock.readLock().unlock();
        }
      }
    }
  }
}
