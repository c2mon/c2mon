/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
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
 ******************************************************************************/
package cern.c2mon.server.laser.publication;

import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;

import ch.cern.tim.cache.CacheNotInitialisedException;
import ch.cern.tim.cache.alarm.AlarmCache;
import ch.cern.tim.server.common.ServiceLocator;
import ch.cern.tim.server.common.ServiceLocatorException;
import ch.cern.tim.shared.alarm.Alarm;

/**
 * The ActiveListManager is responsible for periodically building up a list
 * of all active alarms in the TIM system and sending it to LASER.
 *
 * As the ActiveListManager starts its own thread, the shutdown() method must
 * be called to stop it.
 *
 * @author J. Stowisek
 */
public class

ActiveListManager extends Thread {
  /**
   * Log4j Logger for the ActiveListManager class.
   */
  private static final Logger LOG = Logger.getLogger(ActiveListManager.class);

  /**
   * Singleton instance of the ActiveListManager
   */
  private static ActiveListManager instance = null;

  /**
   * Reference to an AlarmPublisher object. The AlarmPublisher is used to
   * actually send a list of active fault states (i.e. the "active list") to
   * LASER.
   */
  private AlarmPublisher publisher = null;
  
  /**
   * Local home interface of the AlarmClientPublisher session bean
   */
  private static AlarmClientPublisherLocalHome clientPublisherHome = null;
  

  /** As long as this variable is set to <code>true</code>, the process keeps on running */
  private volatile boolean running = false;

  /**
   * Interval in milliseconds (10 minutes should do)
   */
  private static final long INTERVAL = 600000;

  /**
   * Reference to the AlarmCache that is used to build up the list of active
   * alarms.
   */
  private AlarmCache alarmCache = null;


  /**
   * Check whether the ActiveListManager has been initialised.
   */
  public static final boolean isInitialised() {
    return (instance != null);
  }

  /**
   * Check whether the ActiveListManager has been initialised.
   */
  public static final void initialise(ActiveListManager m) throws AlarmHandlingException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("initialise() called.");
    }
    
    if (m != null) {
      try {
        final ServiceLocator locator = new ServiceLocator();
        clientPublisherHome = 
          (AlarmClientPublisherLocalHome) locator.getLocalHome(ServiceLocator.ALARM_CLIENT_PUBLISHER_LOCAL);
      } catch (ServiceLocatorException sle) {
        throw new EJBException("initialise() : Unable to retrieve all resources required by AlarmFacadeBean.", sle);
      } catch (Exception e) {
        throw new EJBException("initialise() : Unexpected error retrieving all resources required by AlarmFacadeBean.", 
                               e);
      }
      
      ActiveListManager.instance = m;
      if (ActiveListManager.instance.running == false) {
        ActiveListManager.instance.running = true;
        ActiveListManager.instance.start();
      }
    } else {
      throw new AlarmHandlingException("Null reference to ActiveListManager received - unable to initialise ActiveListManage singleton.");
    }
  }

  /**
   * Constructor.
   */
  public ActiveListManager(AlarmPublisher p, long interval) throws AlarmHandlingException {
    if (p != null) {
      this.publisher = p;
    } else {
      throw new AlarmHandlingException("Null reference to AlarmPublisher received.");
    }

    try {
      this.alarmCache = AlarmCache.getCache();
    } catch (CacheNotInitialisedException ce) {
      throw new AlarmHandlingException("Error obtaining reference to AlarmCache.", ce);
    }
  }

  /**
   * Worker method: As long as the thread is running, the ActiveListManager
   * periodically (~ 10 minutes) builds up the list of active fault states
   * and sends them to LASER and to all TIM clients on the <code>tim.alarm</code>
   * Topic.
   */
  public void run() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("run() : active list management started.");
    }

    long duration = 0;
    long lastExecution = 0;
    Collection activeList = null;

    while (this.running) {
      // Initialise the calculation of how long it takes to build and send
      // the active list with the current time
      duration = System.currentTimeMillis();
      if ((System.currentTimeMillis() - lastExecution) >= (INTERVAL - 100)) {
        // Initialise the calculation of how long it takes to build and send
        // the active list with the current time
        lastExecution = System.currentTimeMillis();
        try {
          activeList = this.alarmCache.getActiveList();
          
          this.publisher.sendActiveList(activeList); // to LASER
          publishAlarmsToClients(activeList); // to tim.alarm TOPIC
          
          LOG.info(new StringBuffer("Active list containing ").append(activeList.size()).append(" fault states sent to LASER and to "
              + Alarm.CLIENT_ALARM_TOPIC + " Topic. [").append(System.currentTimeMillis() - lastExecution).append(" ms]"));
        } catch (Exception e) {
          LOG.error("run() : unexpected error pushing list of active fault states to LASER.", e);
        }
        // Calculate how long it took to built the active list
      }
      duration = System.currentTimeMillis() - duration + 10;
      // Sleep if the active list sending took less than 5 seconds
      if (duration < 5000) {
        try {
          Thread.sleep(5000 - duration);
        } catch (InterruptedException ie) {
        } catch (Exception e) {
        }
      }
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("run() : active list management stopped.");
    }
  }
  
  /**
   * Private method for publishing (every 10 minutes) all active alarms
   * to the TIM clients.
   * <UL>
   * <LI>This method DOES NOT CHECK whether the alarm passed as a parameter is null.
   * The caller has to ensure that this method is always called with a
   * non-null parameter.
   * </UL>
   * @param activeAlarmsList List of active alarms
   */
  private void publishAlarmsToClients(final Collection activeAlarmsList) {
    try {
      AlarmClientPublisherLocal pub = this.clientPublisherHome.create();
      pub.publish(activeAlarmsList);
      pub.remove();
    } catch (CreateException ce) {
      LOG.error("publishAlarmsToClients() : Unable to create instance of AlarmClientPublisherLocal.", ce);
    } catch (RemoveException re) {
      LOG.error("publishAlarmsToClients() : Unable to remove instance of AlarmClientPublisherLocal.", re);
    } catch (Exception e) {
      LOG.error("publishAlarmsToClients() : Error publishing active alarms to clients.", e);
    }
  }

  /**
   * The finalizer has to make sure that all acquired resources are released
   * before the object is removed from memory.
   */
  protected void finalize() {
    this.running = false;
  }

  /**
   * This method shall be called for initiating the shutdown of this class.
   * The shutdown might take around 5 seconds.
   */
  public static void shutdown() {
    if (isInitialised()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("shutdown() : initiating shutdown of ActiveListManager ...");
      }
      ActiveListManager.instance.running = false;
    }
  }

}
