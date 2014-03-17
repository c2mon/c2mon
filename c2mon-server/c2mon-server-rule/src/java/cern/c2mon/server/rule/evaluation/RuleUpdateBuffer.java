/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.rule.evaluation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.RuleTagFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.shared.common.datatag.TagQualityStatus;


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
@Service
public final class RuleUpdateBuffer {

  /** LOG4J logger instance */
  private static final Logger LOG = Logger.getLogger(RuleUpdateBuffer.class);    
  
  /** The initial buffer size */
  private static final int INITIAL_BUFFER_SIZE = 1000;
  
  /** The schedule interval for the <code>CacheUpdaterTask</code> */
  private static final int BUFFER_TIMER_MILLIS = 75; // we chose this value due to the JMS message delivering delay
  
  /** 
   * The maximum amount of check cycles that the cache updater shall 
   * wait before forcing a cache update for a particular rule.
   */
  private static final int MAX_CYCLES_WAIT = 6; // 6 * 75 = 450 ms <== max delay
  
  /** thread synchronization object */
  private static final Object BUFFER_LOCK = new Object();
  
  /**
   * Reference to the local home interface of the
   * <code>DataTagFacade</code> session bean.
   */
  private static RuleTagFacade ruleTagFacade;
  
  /** The internal buffer used for the */
  private static final Map<Long, RuleBufferObject> RULE_OBJECT_BUF = new Hashtable<Long, RuleBufferObject>(INITIAL_BUFFER_SIZE);
  
  /** 
   * Map containing the flags which indicates that an update was received
   * within the last cache updater cycle.
   */
  private static final Map<Long, Boolean> UPDATE_RECEIVED_FLAGS  = new Hashtable<Long, Boolean>(INITIAL_BUFFER_SIZE);
  
  /**
   * The counters for checking the cycles that a specific rule is already been buffered.
   * When the counter exceeds the MAX_CYCLES_WAIT the <code>CacheUpdaterTask</code> forces
   * a cache update.
   */
  private static final Map<Long, Integer> CYCLE_COUNTERS = new Hashtable<Long, Integer>(INITIAL_BUFFER_SIZE);
  
  /** Timer instance that schedules the <code>CacheUpdaterTask</code> */
  private final Timer timer;
  
  /** will be set to true, once the timer is running */ 
  private static boolean isCacheUpdaterRunning = false;
  
  /**
   * Constructor 
   */
  @Autowired
  private RuleUpdateBuffer(RuleTagFacade ruleTagFacade) {
    this.timer = new Timer("RuleUpdateBuffer");
    RuleUpdateBuffer.ruleTagFacade = ruleTagFacade;
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
    
    LOG.trace(pId + " entering update()");
    synchronized (BUFFER_LOCK) {
      if (!RULE_OBJECT_BUF.containsKey(pId)) {
        bufferObj = new RuleBufferObject(pId, pValue, pValueDesc, pTimestamp);
        RULE_OBJECT_BUF.put(pId, bufferObj);
      }
      else {
        bufferObj = (RuleBufferObject) RULE_OBJECT_BUF.get(pId);
        bufferObj.update(pValue, pValueDesc, pTimestamp);
      }
      scheduleCacheUpdaterTask(pId);
    }
    LOG.trace(pId + " leaving update()");
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
    
    LOG.trace(pId + " entering invalidate()");
    synchronized (BUFFER_LOCK) {
      if (!RULE_OBJECT_BUF.containsKey(pId)) {
        bufferObj = new RuleBufferObject(pId, null, pReason, pDescription, null, pTimestamp);
        RULE_OBJECT_BUF.put(pId, bufferObj);
      }
      else {
        bufferObj = (RuleBufferObject) RULE_OBJECT_BUF.get(pId);
        bufferObj.invalidate(pReason, pDescription, pTimestamp);
      }
      scheduleCacheUpdaterTask(pId);
    }
    LOG.trace(pId + " leaving invalidate()");
  }


  /**
   * Registers an update for the given rule id and triggers
   * the start of the <code>CacheUpdaterTask</code>, if not
   * yet done.
   * @param pId the rule process id that has been updated. 
   */
  private void scheduleCacheUpdaterTask(final Long pId) {
    UPDATE_RECEIVED_FLAGS.put(pId, Boolean.TRUE);
    if (!isCacheUpdaterRunning) {
      try {
        if (LOG.isTraceEnabled())
          LOG.trace(pId + " scheduleCacheUpdaterTask() - Initialize new cache updater task");
        timer.schedule(new CacheUpdaterTask(), BUFFER_TIMER_MILLIS, BUFFER_TIMER_MILLIS);
        isCacheUpdaterRunning = true;
      } catch (IllegalStateException ise) {
        LOG.error(pId + "scheduleCacheUpdaterTask() - Catched illegal state exception", ise);
      }      
    }
  }

  
  /**
   * Inner class which is used to store the rule update
   * information for the cache of the given rule data tag.
   *
   * @author Matthias Braeger
   */
  private static final class RuleBufferObject {
    /** Rule data tag id */
    private Long id = null;
    /** rule result object */
    private Object value = null;
    /** quality flag */
    private HashSet<TagQualityStatus> qualityCollection = new HashSet<TagQualityStatus>();
    /** quality flag description */
    private HashMap<TagQualityStatus, String> qualityDescriptions = new HashMap<TagQualityStatus, String>();
    /** value description */
    private String valueDesc = null; 
    /** rule evaluation timestamp */
    private Timestamp timestamp = null;


    /**
     * Copy Constructor
     * @param rbo The object to be copied
     */
    @SuppressWarnings("unchecked")
    private RuleBufferObject(final RuleBufferObject rbo) {
      this.id = rbo.id;
      this.value = rbo.value;
      this.qualityCollection = (HashSet<TagQualityStatus>) rbo.qualityCollection.clone();
      this.qualityDescriptions = (HashMap<TagQualityStatus, String>) rbo.qualityDescriptions.clone();
      this.valueDesc = rbo.valueDesc;     
      this.timestamp = new Timestamp(rbo.timestamp.getTime());
    }


    /**
     * Constructor
     * @param pId rule data tag id
     * @param pValue rule result
     * @param pValueDesc description
     * @param pTimestamp rule evaluation timestamp
     */
    private RuleBufferObject(final Long pId, final Object pValue, final String pValueDesc, final Timestamp pTimestamp) {
      this(pId, pValue, null, null, pValueDesc, pTimestamp);
    }


    /**
     * Constructor
     * @param pId rule data tag id
     * @param pValue rule result
     * @param pQuality error quality flag
     * @param pValueDesc description
     * @param pTimestamp rule evaluation timestamp
     */
    private RuleBufferObject(final Long pId, final Object pValue, final TagQualityStatus pStatus, final String pQualityDesc, final String pValueDesc, final Timestamp pTimestamp) {
        this.id = pId;
        this.value = pValue;
        if (pStatus != null) {
          this.qualityCollection.add(pStatus);
          if (pQualityDesc != null) {
            this.qualityDescriptions.put(pStatus, pQualityDesc);
          }          
        }        
        this.valueDesc = pValueDesc;
        this.timestamp = pTimestamp;
    }


    /**
     * Updates the values of this <code>BufferObject</code> instance (all invalid status' are removed)
     * @param pValue rule result
     * @param pValueDesc description
     * @param pTimestamp rule evaluation timestamp
     * @return <code>true</code>, if object was updated, else <code>false</code>
     */
    private boolean update(final Object pValue, final String pValueDesc, final Timestamp pTimestamp) {
      boolean retval = false;
      synchronized (BUFFER_LOCK) {
        if (this.timestamp.before(pTimestamp) || this.timestamp.equals(pTimestamp)) {
          this.value = pValue;
          this.qualityCollection.clear();
          this.qualityDescriptions.clear();
          this.valueDesc = pValueDesc;
          this.timestamp = pTimestamp;
          retval = true;
        }
      }      
      return retval;
    }


    /**
     * Updates the values of this <code>BufferObject</code> instance
     * @param pQuality the error quality code
     * @param pDescription error description
     * @param pTimestamp rule evaluation timestamp
     * @return <code>true</code>, if object was updated, else <code>false</code>
     */
    private boolean invalidate(final TagQualityStatus pQuality, final String pDescription, final Timestamp pTimestamp) {
      if (pQuality == null) {
        throw new IllegalArgumentException("invalidate(..) method called with null TagQualityStatus argument.");
      }
      boolean retval = false;
      if (this.timestamp.before(pTimestamp) || this.timestamp.equals(pTimestamp)) {
        this.qualityCollection.add(pQuality);
        this.qualityDescriptions.put(pQuality, pDescription);        
        this.timestamp = pTimestamp;
        retval = true;
      }      
      return retval;
    }
  } // end of RuleBufferObject class


  /**
   * This class extends the Java <code>TimerTask</code> and
   * takes care of updating the oc4j cache via the <code>DataTagFacadeBean</code>.
   *
   * @author Matthias Braeger
   */
  private static class CacheUpdaterTask extends TimerTask {
    /**
     * This method is executed when it gets triggered
     * by its <code>Timer</code> instance. It then takes
     * care of the cache update for all rules that haven't 
     * been updated since the last check.
     */
    public void run() {
      //keep logic in try clause as exception will kill the timer thread here
      try {        
        Collection<RuleBufferObject> rulesToUpdate = null;
  
        // create first a copy of all rule objects that needs to be updated
        synchronized (BUFFER_LOCK) {
          rulesToUpdate = new ArrayList<RuleBufferObject>(); // List of rules where the cache shall be updated
          Integer actCounter = null; // actual cycle counter
          boolean hasJustBeenUpdated = false; // flag indicating, if the actual rule was updated since the last check
          boolean forceCacheUpdate = false; // true, if a cache update shall be forced due to an exceed of the MAX_CYCLE_WAIT
          for (Long actTagId : UPDATE_RECEIVED_FLAGS.keySet()) {
            hasJustBeenUpdated = ((Boolean) UPDATE_RECEIVED_FLAGS.get(actTagId)).booleanValue();
            actCounter = (Integer) CYCLE_COUNTERS.get(actTagId);
            forceCacheUpdate = (actCounter != null && actCounter.intValue() >= MAX_CYCLES_WAIT);
              
            if (!hasJustBeenUpdated || forceCacheUpdate) {
              // False ==> then we update the cache since there was no recent update of that rule
              // OR we the buffer was updated more than MAX_CYCLES_WAIT cycle in a row ==> force update 
              RuleBufferObject rbo = new RuleBufferObject((RuleBufferObject) RULE_OBJECT_BUF.get(actTagId));
              rulesToUpdate.add(rbo);
              if (forceCacheUpdate) {
                LOG.debug("CacheUpdaterTask() - Forcing a cache update for rule "
                    + actTagId + " since it was already delayed by "
                    + MAX_CYCLES_WAIT * BUFFER_TIMER_MILLIS + " ms.");
              }
            }
            else {
              // Set the flag to FALSE in order indicate a cache update at the next check
              UPDATE_RECEIVED_FLAGS.put(actTagId, Boolean.FALSE);
              if (actCounter == null) {
                CYCLE_COUNTERS.put(actTagId, new Integer(1));
              }
              else { // Increasing the counter
                CYCLE_COUNTERS.put(actTagId, new Integer(actCounter.intValue() + 1));
              }
            }
          }
          
          // Cleaning the buffer from all objects that are going to be put into the cache.
          Long actTagId;
          for (RuleBufferObject rbo : rulesToUpdate) {
            actTagId = rbo.id;
            UPDATE_RECEIVED_FLAGS.remove(actTagId);
            RULE_OBJECT_BUF.remove(actTagId);
            CYCLE_COUNTERS.remove(actTagId);
          }
          
          if (UPDATE_RECEIVED_FLAGS.size() == 0) {
            if (LOG.isTraceEnabled())
              LOG.trace("CacheUpdaterTask() - Canceling next cache updater check, because there are no more updates registered.");
            this.cancel();
            isCacheUpdaterRunning = false;
          }
        } // end of synchronization
        
        if (rulesToUpdate.size() > 0) {
          // Updating the cache
          for (RuleBufferObject rbo : rulesToUpdate) {
            if (rbo.qualityCollection.isEmpty()) {
              if (LOG.isTraceEnabled())
                LOG.trace("CacheUpdaterTask() - updating cache for rule id " + rbo.id 
                          + ": value=" + rbo.value
                          + ", description=" + rbo.valueDesc
                          + ", timestamp=" + rbo.timestamp);
              try {
                ruleTagFacade.updateAndValidate(rbo.id, rbo.value, rbo.valueDesc, rbo.timestamp);            
              } catch (CacheElementNotFoundException cacheEx) {
                LOG.warn("Unable to update rule (can happen during rule reconfiguration)", cacheEx);
              } catch (Exception exception) {
                LOG.warn("Unexpected error during rule evaluation", exception);
              }
            }
            else {
              if (LOG.isTraceEnabled())
                LOG.trace("CacheUpdaterTask() - invalidating cache for rule id " + rbo.id 
                    + ": reasons=" + rbo.qualityCollection 
                    + ", descriptions=" + rbo.qualityDescriptions
                    + ", timestamp=" + rbo.timestamp);
              try {                
                ruleTagFacade.setQuality(rbo.id, rbo.qualityCollection, null, rbo.qualityDescriptions, rbo.timestamp);
              } catch (CacheElementNotFoundException cacheEx) {
                LOG.warn("Unable to update rule as could not be located in cache (normal during rule reconfiguration)", cacheEx);
              }                          
            }
          } // end for                    
        }      
      } catch (Exception ex) {
        LOG.error("Exception caught during rule update - should not be ignored!", ex);
      }
    }
  } // end of CacheUpdaterTask class
}
