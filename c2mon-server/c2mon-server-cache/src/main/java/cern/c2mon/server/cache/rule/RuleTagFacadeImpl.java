/******************************************************************************
 * Copyright (C) 2010-2017 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.cache.rule;

import java.sql.Timestamp;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.RuleTagFacade;
import cern.c2mon.server.cache.common.AbstractTagFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.rule.RuleExpression;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;

/**
 * Facade object for manipulating server rules.
 *
 * @author Mark Brightwell
 *
 */
@Service
@Slf4j
public class RuleTagFacadeImpl extends AbstractTagFacade<RuleTag> implements RuleTagFacade {

  /**
   * Used for tracking nb of rule evaluations in testing. TODO remove once no longer necessary
   */
  private volatile int updateCount = 0; //not completely exact as no locking

  /**
   * Logger for logging updates made to rules.
   */
  private static final Logger RULELOG = LoggerFactory.getLogger("RuleTagLogger");

  /**
   * Reference to the low level Rule Facade bean.
   */
  private RuleTagCacheObjectFacade ruleTagCacheObjectFacade;

  /**
   * Reference to the DataTag cache.
   */
  private DataTagCache dataTagCache;

  /**
   * Constructor.
   *
   * @param ruleTagCache the RuleTag cache
   * @param ruleTagCacheObjectFacade the low level Rule Facade
   * @param alarmFacade the Alarm Facade
   * @param alarmCache the Alarm cache
   * @param dataTagCache the DataTag cache
   */
  @Autowired
  public RuleTagFacadeImpl(final RuleTagCache ruleTagCache,
                           final RuleTagCacheObjectFacade ruleTagCacheObjectFacade,
                           final AlarmFacade alarmFacade,
                           final AlarmCache alarmCache,
                           final DataTagCache dataTagCache) {
    super(ruleTagCache, alarmFacade, alarmCache);
    this.ruleTagCacheObjectFacade = ruleTagCacheObjectFacade;
    this.dataTagCache = dataTagCache;
  }

  @Override
  public void setParentSupervisionIds(final Long ruleTagId) {
    tagCache.acquireWriteLockOnKey(ruleTagId);
    try {
      RuleTag ruleTag = tagCache.get(ruleTagId);
      setParentSupervisionIds(ruleTag);
      tagCache.putQuiet(ruleTag);
    } finally {
      tagCache.releaseWriteLockOnKey(ruleTagId);
    }
  }

  /**
   * Sets the parent process and equipment fields for RuleTags.
   * Please notice that the caller method should first make a write lock
   * on the RuleTag reference.
   *
   * @param ruleTag the RuleTag for which the fields should be set
   */
  @Override
  public void setParentSupervisionIds(final RuleTag ruleTag) {
    ((RuleTagCache) tagCache).setParentSupervisionIds(ruleTag);
  }

  /**
   * Logs the rule in the specific log4j log, using the log4j renderer in the configuration file
   * (done after every update).
   * @param ruleTagCacheObject the cache object to log
   * TODO not used
   */
  private void log(final RuleTagCacheObject ruleTagCacheObject) {
    if (RULELOG.isInfoEnabled()) {
      RULELOG.info(ruleTagCacheObject.toString());
    } else if (updateCount % 10000 == 0) {
      RULELOG.warn("Total rule updates to the cache so far: " + updateCount);
    }
  }

//  @Override
//  public void invalidate(Long id, DataTagQuality dataTagQuality, Timestamp timestamp) {
//    try {
//      RuleTag ruleTag = (RuleTag) tagCache.get(id);
//      ruleTag.getWriteLock().lock();
//      try {
//        ruleTagCacheObjectFacade.invalidate(ruleTag, dataTagQuality, timestamp);
//        tagCache.put(ruleTag.getId, ruleTag);
//        updateCount++;
//        log((RuleTagCacheObject) ruleTag);
//      } finally {
//        ruleTag.getWriteLock().unlock();
//      }
//    } catch (CacheElementNotFoundException cacheEx) {
//      LOGGER.error("Unable to locate rule in cache (id " + id + ") - no invalidation performed.", cacheEx);
//    }
//  }

  @Override
  public void updateAndValidate(final Long id, final Object value, final String valueDescription, final Timestamp timestamp) {
    tagCache.acquireWriteLockOnKey(id);
    try {
      RuleTag ruleTag = tagCache.get(id);
      if (!filterout(ruleTag, value, valueDescription, null, null, timestamp)) {
        ruleTagCacheObjectFacade.validate(ruleTag);
        ruleTagCacheObjectFacade.update(ruleTag, value, valueDescription, timestamp);
        tagCache.put(id, ruleTag);
        updateCount++;
        log((RuleTagCacheObject) ruleTag);
      } else {
        if (log.isTraceEnabled()) {
          log.trace("Filtering out repeated update for rule " + id);
        }
      }
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("Unable to locate rule in cache (id " + id + ") - no update performed.", cacheEx);
    } finally {
      tagCache.releaseWriteLockOnKey(id);
    }
  }

  /**
   * For rules, sets the rule text field (which in turn parses the rule expression and
   * set the corresponding field). Also sets the parent equipments and processes for this
   * rule.
   *
   * @param ruleTag fields are modified in this RuleTag
   * @param properties the properties used to set the fields.
   * @return always returns null as no changes to rules need propagating to the DAQ
   * @throws ConfigurationException if an exception occurs during reconfiguration
   */
  @Override
  public Change configureCacheObject(final RuleTag ruleTag, final Properties properties) throws ConfigurationException {
    setCommonProperties((RuleTagCacheObject) ruleTag, properties);
    // TAG rule text
    String tmpStr = properties.getProperty("ruleText");
    if (tmpStr != null) {
      ((RuleTagCacheObject) ruleTag).setRuleText(tmpStr); //also sets rule expression
      setParentSupervisionIds(ruleTag);
    }

    return null;
  }

  @Override
  public RuleTagCacheObject createCacheObject(final Long id, final Properties properties) throws ConfigurationException {
    RuleTagCacheObject ruleTag = new RuleTagCacheObject(id);
    setCommonProperties(ruleTag, properties);
    configureCacheObject(ruleTag, properties);
    setDefaultRuntimeProperties(ruleTag);
    validateConfig(ruleTag);
    return ruleTag;
  }

  /**
   * Checks that a RuleTagCacheObject has a valid configuration. Is
   * used after creating or reconfiguring a tag.
   *
   * @param ruleTag the RuleTag that needs validating
   * @throws ConfigurationException if an error occurs during validation
   */
  @Override
  public void validateConfig(final RuleTag ruleTag) throws ConfigurationException {
    validateTagConfig(ruleTag);
    if (ruleTag.getRuleText() != null) {
      if (ruleTag.getRuleText().length() > 4000) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"ruleText\" must less than 4000 characters long");
      }
      RuleExpression exp;
      try {
        exp = ruleTag.getRuleExpression();
      } catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"ruleText\" is not a gramatically correct rule expression");
      }
      if (exp == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"ruleText\" is not a gramatically correct rule expression (Expression is null)");
      }
    } else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"ruleText\" is null for rule " + ruleTag.getId() + " - unable to configure it correctly.");
    }
  }

  @Override
  public Collection<RuleTag> findByRuleInputTagId(Long id) {
    return ((RuleTagCache)this.tagCache).findByRuleInputTagId(id);
  }

  @Override
  protected void invalidateQuietly(final RuleTag tag, final TagQualityStatus statusToAdd, final String statusDescription,
      final Timestamp timestamp) {
    ruleTagCacheObjectFacade.invalidate((RuleTag) tag, statusToAdd, statusDescription, timestamp);
  }
}
