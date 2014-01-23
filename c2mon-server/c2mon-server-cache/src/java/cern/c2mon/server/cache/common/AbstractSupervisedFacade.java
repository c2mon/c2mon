/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
package cern.c2mon.server.cache.common;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.C2monCacheWithListeners;
import cern.c2mon.server.cache.SupervisedFacade;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * Implementation of SupervisedFacade, with the intention
 * this should be used as basis for Process, Equipment
 * and SubEquipment Facade implementations.
 * 
 * @author Mark Brightwell
 *
 * @param <T>
 */
public abstract class AbstractSupervisedFacade<T extends Supervised> extends AbstractFacade<T> implements SupervisedFacade<T> {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AbstractSupervisedFacade.class);
  
  /**
   * Reference to the cache.
   */
  private C2monCacheWithListeners<Long, T> c2monCache;
  
  /**
   * Alive timers
   */
  private AliveTimerCache aliveTimerCache;
  
  /**
   * For accessing alive timers.
   */
  private AliveTimerFacade aliveTimerFacade;
  
  /**
   * Constructor.
   * @param c2monCache the cache this facade pertains to
   * @param aliveTimerCache cache with alive timers
   * @param aliveTimerFacade the alive timer facade
   */
  public AbstractSupervisedFacade(final C2monCacheWithListeners<Long, T> c2monCache, final AliveTimerCache aliveTimerCache, 
                                                final AliveTimerFacade aliveTimerFacade) {
    super();
    this.c2monCache = c2monCache;
    this.aliveTimerCache = aliveTimerCache;
    this.aliveTimerFacade = aliveTimerFacade;
  }
  
  /**
   * Must return the corresponding supervision entity for this facade object.
   * @return a Supervision Entity constant
   */
  protected abstract SupervisionEntity getSupervisionEntity();
  
  /**
   * Sets the status of the Supervised object to STARTUP,
   * with associated message.
   * 
   * <p>Starts the alive timer if not already running.
   * 
   * @param supervised supervised object
   * @param timestamp time of the start
   */
  protected final void start(final T supervised, final Timestamp timestamp) {    
    if (supervised.getAliveTagId() != null) {
      aliveTimerFacade.start(supervised.getAliveTagId());
    }      
    supervised.setSupervisionStatus(SupervisionStatus.STARTUP);      
    supervised.setStatusDescription(supervised.getSupervisionEntity() + " " + supervised.getName() + " was started");
    supervised.setStatusTime(new Timestamp(System.currentTimeMillis()));              
  }
  
  @Override
  public void start(final Long id, final Timestamp timestamp) {
    c2monCache.acquireWriteLockOnKey(id);
    try {
      T supervised = c2monCache.getCopy(id);
      start(supervised, timestamp);
      c2monCache.put(id, supervised);
    } finally {
      c2monCache.releaseWriteLockOnKey(id);
    }
  }
  
  protected void stop(final T supervised, final Timestamp timestamp) {    
    if (supervised.getAliveTagId() != null) {
      aliveTimerFacade.stop(supervised.getAliveTagId());
    }      
    supervised.setSupervisionStatus(SupervisionStatus.DOWN);
    supervised.setStatusTime(timestamp);
    supervised.setStatusDescription(supervised.getSupervisionEntity() + " " + supervised.getName() + " was stopped");            
  }
  
  @Override
  public void stop(final Long id, final Timestamp timestamp) {
    c2monCache.acquireWriteLockOnKey(id);
    try {
      T supervised = c2monCache.getCopy(id);
      stop(supervised, timestamp);
      c2monCache.put(id, supervised);
    } finally {
      c2monCache.releaseWriteLockOnKey(id);
    }
  }

  private void resume(final T supervised, final Timestamp timestamp, final String message) {      
    supervised.setSupervisionStatus(SupervisionStatus.RUNNING);
    supervised.setStatusTime(timestamp);
    supervised.setStatusDescription(message);
  }
  
  @Override
  public void resume(final Long id, final Timestamp timestamp, final String message) {      
    c2monCache.acquireWriteLockOnKey(id);
    try {      
      T supervised = c2monCache.get(id);
      resume(supervised, timestamp, message);
      c2monCache.put(id, supervised);      
    } finally {
      c2monCache.releaseWriteLockOnKey(id);
    }
  }
  
  private void suspend(final T supervised, final Timestamp timestamp, final String message) {
    supervised.setSupervisionStatus(SupervisionStatus.DOWN);
    supervised.setStatusDescription(message);
    supervised.setStatusTime(timestamp);       
  }
  
  @Override
  public final void suspend(final Long id, final Timestamp timestamp, final String message) {
    c2monCache.acquireWriteLockOnKey(id);
    try {
      T supervised = c2monCache.get(id);
      suspend(supervised, timestamp, message);
      c2monCache.put(id, supervised);
    } finally {
      c2monCache.releaseWriteLockOnKey(id);
    }
  }
  
  @Override
  public boolean isRunning(final T supervised) {
    c2monCache.acquireReadLockOnKey(supervised.getId());
    try {      
      return supervised.getSupervisionStatus() != null 
            && (supervised.getSupervisionStatus().equals(SupervisionStatus.STARTUP) 
            || supervised.getSupervisionStatus().equals(SupervisionStatus.RUNNING));
    } finally {
      c2monCache.releaseReadLockOnKey(supervised.getId());
    }
  }
  
  @Override
  public boolean isRunning(final Long id) {
    c2monCache.acquireReadLockOnKey(id);
    try {  
      return isRunning(c2monCache.get(id));
    } finally {
      c2monCache.releaseReadLockOnKey(id);
    }
  }
  
  @Override
  public boolean isUncertain(final T supervised) {
    c2monCache.acquireReadLockOnKey(supervised.getId());
    try {    
      return supervised.getSupervisionStatus() != null 
            && supervised.getSupervisionStatus().equals(SupervisionStatus.UNCERTAIN);
    } finally {
      c2monCache.releaseReadLockOnKey(supervised.getId());
    }
  }
  
  @Override
  public SupervisionEvent getSupervisionStatus(final Long id) {
    c2monCache.acquireReadLockOnKey(id);
    try {
      T supervised = c2monCache.get(id);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Getting supervision status: " + getSupervisionEntity() + " " + supervised.getName() + " is " + supervised.getSupervisionStatus());
      } 
      Timestamp supervisionTime;
      String supervisionMessage;
      if (supervised.getStatusTime() != null) {
        supervisionTime = supervised.getStatusTime();
      } else {
        supervisionTime = new Timestamp(System.currentTimeMillis());
      }
      if (supervised.getStatusDescription() != null) {
        supervisionMessage = supervised.getStatusDescription();
      } else {
        supervisionMessage = getSupervisionEntity() + " " + supervised.getName() + " is " + supervised.getSupervisionStatus();
      }
      return new SupervisionEventImpl(getSupervisionEntity(), id, supervised.getSupervisionStatus(), 
                                                                    supervisionTime, supervisionMessage);
    } finally {
      c2monCache.releaseReadLockOnKey(id);
    }
  }
  
  @Override
  public void refreshAndnotifyCurrentSupervisionStatus(final Long id) {
    c2monCache.acquireWriteLockOnKey(id);
    Timestamp refreshTime = new Timestamp(System.currentTimeMillis());
    try {
      T supervised = c2monCache.get(id);
      supervised.setStatusTime(refreshTime);
      c2monCache.put(supervised.getId(), supervised);
    } finally {
      c2monCache.releaseWriteLockOnKey(id);
    }
  }
  
  @Override
  public void removeAliveTimer(final Long supervisedId) {   
    T supervised = c2monCache.get(supervisedId);
    Long aliveId = supervised.getAliveTagId(); 
    if (aliveId != null) {      
      aliveTimerFacade.stop(aliveId);
      aliveTimerCache.remove(aliveId);
    }        
  }
  
  @Override
  public void removeAliveDirectly(final Long aliveId) {
    if (aliveId != null) {      
      aliveTimerFacade.stop(aliveId);
      aliveTimerCache.remove(aliveId);
    } else {
      throw new NullPointerException("Called method with null alive id");
    }
  }
  
  @Override
  public void loadAndStartAliveTag(final Long supervisedId) {
    T supervised = c2monCache.get(supervisedId);
    Long aliveId = supervised.getAliveTagId();
    aliveTimerCache.loadFromDb(aliveId);
    if (aliveId != null) {            
      aliveTimerFacade.start(aliveId);
    }
  }

}
