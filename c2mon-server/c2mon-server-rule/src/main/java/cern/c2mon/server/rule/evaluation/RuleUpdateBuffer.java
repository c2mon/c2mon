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
package cern.c2mon.server.rule.evaluation;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;

/**
 * This temporary buffer is used to filter out intermediate rule evaluation results.
 * This can happen, if the same rule got updates of different data tags at the same
 * time or within a very short time interval. The <code>RuleUpdateBuffer</code> acts
 * in this cases like a time-deadband which sends only the latest value after it did
 * not receive any further evaluations after the last cacheUpdate intervals.
 *
 * In C2MON, instantiated as a Spring singleton using annotations.
 *
 * @author Matthias Braeger
 */
@Slf4j
@Service
public final class RuleUpdateBuffer {
  
  /** The initial buffer size */
  static final int INITIAL_BUFFER_SIZE = 1000;
  
  /** The schedule interval for the <code>CacheUpdaterTask</code> */
  static final int BUFFER_TIMER_MILLIS = 75; // we chose this value due to the JMS message delivering delay
  
  /** thread synchronization object */
  static final Object BUFFER_LOCK = new Object();
  
  /** The internal buffer used for the */
  static final Map<Long, RuleBufferObject> RULE_OBJECT_BUF = new Hashtable<>(INITIAL_BUFFER_SIZE);
  
  /** 
   * Map containing the flags which indicates that an update was received
   * within the last cache updater cycle.
   */
  static final Map<Long, Boolean> UPDATE_RECEIVED_FLAGS  = new Hashtable<>(INITIAL_BUFFER_SIZE);
  
  /** Timer instance that schedules the <code>CacheUpdaterTask</code> */
  private final Timer timer;

  /**
   * Reference required to update the items back into the cache
   */
  private C2monCache<RuleTag> ruleTagCache;

  /**
   * Constructor 
   */
  @Autowired
  private RuleUpdateBuffer(C2monCache<RuleTag> ruleTagCache) {
    this.ruleTagCache = ruleTagCache;
    this.timer = new Timer("RuleUpdater");
  }

  /**
   * Updates the internal rule buffer
   * @param pId data tag id
   * @param pValue the object value
   * @param pValueDesc the Value description
   * @param pTimestamp the timestamp of the rule evaluation.
   */
  public void update(final Long pId, final Object pValue, final String pValueDesc, final Timestamp pTimestamp) {
    final RuleBufferObject bufferObj;
    
    log.trace(pId + " entering update()");
    synchronized (BUFFER_LOCK) {
      if (!RULE_OBJECT_BUF.containsKey(pId)) {
        bufferObj = new RuleBufferObject(pId, pValue, pValueDesc, pTimestamp);
        RULE_OBJECT_BUF.put(pId, bufferObj);
      }
      else {
        bufferObj = RULE_OBJECT_BUF.get(pId);
        bufferObj.update(pValue, pValueDesc, pTimestamp);
      }
      scheduleCacheUpdaterTask(pId);
    }
    log.trace(pId + " leaving update()");
  }

  /**
   * Updates the internal rule buffer with an invalidation message
   * @param pId rule data tag id
   * @param pReason quality flag
   * @param pDescription error description
   * @param pTimestamp the timestamp of the rule evaluation
   */
  public void invalidate(final Long pId, final TagQualityStatus pReason, final String pDescription, final Timestamp pTimestamp) {
    final RuleBufferObject bufferObj;
    
    log.trace(pId + " entering invalidate()");
    synchronized (BUFFER_LOCK) {
      if (!RULE_OBJECT_BUF.containsKey(pId)) {
        bufferObj = new RuleBufferObject(pId, null, pReason, pDescription, null, pTimestamp);
        RULE_OBJECT_BUF.put(pId, bufferObj);
      }
      else {
        bufferObj = RULE_OBJECT_BUF.get(pId);
        bufferObj.invalidate(pReason, pDescription, pTimestamp);
      }
      scheduleCacheUpdaterTask(pId);
    }
    log.trace(pId + " leaving invalidate()");
  }

  /**
   * Registers an update for the given rule id and triggers
   * the start of the <code>CacheUpdaterTask</code>, if not
   * yet done.
   * @param pId the rule process id that has been updated. 
   */
  private void scheduleCacheUpdaterTask(final Long pId) {
    UPDATE_RECEIVED_FLAGS.put(pId, Boolean.TRUE);
    if (!CacheUpdaterTask.isCacheUpdaterRunning) {
      try {
        log.trace(pId + " scheduleCacheUpdaterTask() - Initialize new cache updater task");
        timer.schedule(new CacheUpdaterTask(ruleTagCache), BUFFER_TIMER_MILLIS, BUFFER_TIMER_MILLIS);
        CacheUpdaterTask.isCacheUpdaterRunning = true;
      } catch (IllegalStateException ise) {
        log.error(pId + "scheduleCacheUpdaterTask() - Catched illegal state exception", ise);
      }      
    }
  }

}
