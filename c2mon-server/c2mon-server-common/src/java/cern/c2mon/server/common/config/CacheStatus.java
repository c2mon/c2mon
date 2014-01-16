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
package cern.c2mon.server.common.config;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

/**
 * Shared object used to record the status of the caches
 * across a cluster of servers and provide distributed locks
 * for the cache loading and persistence.
 * 
 * @author Mark Brightwell
 *
 */
public class CacheStatus {
  
  /**
   * Class logger
   */
  private static final Logger LOGGER = Logger.getLogger(CacheStatus.class);
  
  /**
   * Flag recording if the DataTagCache
   * is loaded (or in the process of loading).
   */
  private Boolean dataTagCacheLoaded;
  
  private Boolean controlTagCacheLoaded;
  
  private Boolean ruleTagCacheLoaded;
  
  private Boolean aliveTimerCacheLoaded;
  
  private Boolean commFaultTagCacheLoaded;
  
  private Boolean alarmCacheLoaded;
  
  private Boolean processCacheLoaded;
  
  private Boolean equipmentCacheLoaded;
  
  private Boolean subEquipmentCacheLoaded;
  
  private Boolean commandTagCacheLoaded;
  
  /**
   * Has the RuleTagPostLoaderProcessor run?
   * (runs once on initial server start-up).
   */
  private Boolean ruleCachePostProcessed;
  
  /**
   * Lock used to synchronized the servers when checking and setting the
   * status of the various caches.
   */
  private ReentrantReadWriteLock cacheStatusLock = new ReentrantReadWriteLock();
  
  /**
   * Lock used to synchronize the servers when persisting a cache. Currently only one
   * server can persist ANY cache at any one time - may wish to allow persistence of 
   * DIFFERENT caches at the same time in the future (persistence of a given cache
   * must be synchronized to prevent race conditions during the commit phase).
   */
  private ReentrantReadWriteLock cachePersistenceLock = new ReentrantReadWriteLock();
  
  /**
   * Method run only when the Terracotta servers are started
   * for the first time and no TIM server has been started.
   * Indicates that the caches will need loading from the DB.
   */
  void initialize() {
    LOGGER.info("Initializing cache status properties.");
    cacheStatusLock.writeLock().lock();
    try {
      dataTagCacheLoaded = Boolean.FALSE;
      controlTagCacheLoaded = Boolean.FALSE;
      setRuleTagCacheLoaded(Boolean.FALSE);    
      aliveTimerCacheLoaded = Boolean.FALSE;
      commFaultTagCacheLoaded = Boolean.FALSE;
      alarmCacheLoaded = Boolean.FALSE;
      processCacheLoaded = Boolean.FALSE;
      equipmentCacheLoaded = Boolean.FALSE;
      subEquipmentCacheLoaded = Boolean.FALSE;
      commandTagCacheLoaded = Boolean.FALSE;
      alarmCacheLoaded = Boolean.FALSE;
      ruleCachePostProcessed = Boolean.FALSE;
    } finally {
      cacheStatusLock.writeLock().unlock();
    }    
  }

  /**
   * @return the dataTagCacheLoaded
   */
  public Boolean getDataTagCacheLoaded() {
    return dataTagCacheLoaded;
  }

  /**
   * @param dataTagCacheLoaded the dataTagCacheLoaded to set
   */
  public void setDataTagCacheLoaded(Boolean dataTagCacheLoaded) {
    this.dataTagCacheLoaded = dataTagCacheLoaded;
  }

  public void setControlTagCacheLoaded(Boolean controlTagCacheLoaded) {
    this.controlTagCacheLoaded = controlTagCacheLoaded;
  }

  public Boolean getControlTagCacheLoaded() {
    return controlTagCacheLoaded;
  }

  /**
   * @return the processCacheLoaded
   */
  public Boolean getProcessCacheLoaded() {
    return processCacheLoaded;
  }

  /**
   * @param processCacheLoaded the processCacheLoaded to set
   */
  public void setProcessCacheLoaded(Boolean processCacheLoaded) {
    this.processCacheLoaded = processCacheLoaded;
  }

  public void setAliveTimerCacheLoaded(Boolean aliveTimerCacheLoaded) {
    this.aliveTimerCacheLoaded = aliveTimerCacheLoaded;
  }

  public Boolean getAliveTimerCacheLoaded() {
    return aliveTimerCacheLoaded;
  }

  public void setCommFaultTagCacheLoaded(Boolean commFaultTagCacheLoaded) {
    this.commFaultTagCacheLoaded = commFaultTagCacheLoaded;
  }

  public Boolean getCommFaultTagCacheLoaded() {
    return commFaultTagCacheLoaded;
  }

  public void setEquipmentCacheLoaded(Boolean equipmentCacheLoaded) {
    this.equipmentCacheLoaded = equipmentCacheLoaded;
  }

  public Boolean getEquipmentCacheLoaded() {
    return equipmentCacheLoaded;
  }

  public void setSubEquipmentCacheLoaded(Boolean subEquipmentCacheLoaded) {
    this.subEquipmentCacheLoaded = subEquipmentCacheLoaded;
  }

  public Boolean getSubEquipmentCacheLoaded() {
    return subEquipmentCacheLoaded;
  }

  public void setRuleTagCacheLoaded(Boolean ruleTagCacheLoaded) {
    this.ruleTagCacheLoaded = ruleTagCacheLoaded;
  }

  public Boolean getRuleTagCacheLoaded() {
    return ruleTagCacheLoaded;
  }

  /**
   * @return the cacheStatusLock
   */
  public ReentrantReadWriteLock getCacheStatusLock() {
    return cacheStatusLock;
  }

  public ReentrantReadWriteLock getCachePersistenceLock() {
    return cachePersistenceLock;
  }

  public Boolean getCommandTagCacheLoaded() {
    return commandTagCacheLoaded;
  }

  /**
   * @param commandTagCacheLoaded the commandTagCacheLoaded to set
   */
  public void setCommandTagCacheLoaded(Boolean commandTagCacheLoaded) {
    this.commandTagCacheLoaded = commandTagCacheLoaded;
  }

  /**
   * @return the alarmCacheLoaded
   */
  public Boolean getAlarmCacheLoaded() {
    return alarmCacheLoaded;
  }

  /**
   * @param alarmCacheLoaded the alarmCacheLoaded to set
   */
  public void setAlarmCacheLoaded(Boolean alarmCacheLoaded) {
    this.alarmCacheLoaded = alarmCacheLoaded;
  }

  public Boolean getRuleCachePostProcessed() {
    return ruleCachePostProcessed;
  }

  /**
   * @param ruleCachePostProcessed the ruleCachePostProcessed to set
   */
  public void setRuleCachePostProcessed(Boolean ruleCachePostProcessed) {
    this.ruleCachePostProcessed = ruleCachePostProcessed;
  }

}
