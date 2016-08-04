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
package cern.c2mon.server.cache.alarm;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Properties;

import cern.c2mon.shared.common.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.common.AbstractFacade;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCacheObject.AlarmChangeState;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.server.common.alarm.AlarmCondition;

/**
 * Implementation of the AlarmFacade.
 *
 * @author Mark Brightwell
 *
 */
@Service
public class AlarmFacadeImpl extends AbstractFacade<Alarm> implements AlarmFacade {

  /**
   * Default max length for fault family
   */
  public static final int MAX_FAULT_FAMILY_LENGTH = 64;

  /**
   * Default max length for fault member
   */
  public static final int MAX_FAULT_MEMBER_LENGTH = 64;

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AlarmFacadeImpl.class);

  /**
   * Reference to the Alarm cache.
   */
  private AlarmCache alarmCache;

  /**
   * Used to validate the alarm object at runtime configuration.
   */
  private int maxFaultFamily = MAX_FAULT_FAMILY_LENGTH;

  /**
   * Used to validate the alarm object at runtime configuration.
   */
  private int maxFaultMemberLength = MAX_FAULT_MEMBER_LENGTH;

  private TagLocationService tagLocationService;


  /**
   * Autowired constructor.
   * @param alarmCache the alarm cache
   */
  @Autowired
  public AlarmFacadeImpl(final AlarmCache alarmCache, final TagLocationService tagLocationService) {
    super();
    this.alarmCache = alarmCache;
    this.tagLocationService = tagLocationService;
  }

  /**
   * Derives a valid JMS topic name for distributing the alarm's values to
   * clients (currently the same for all alarms, so returns a constant).
   *
   * @param alarm the alarm for which the topic should be provided
   * @return a valid JMS topic name for the alarm
   */
  @Override
  public String getTopicForAlarm(final Alarm alarm) {

    /*
     * StringBuffer str = new StringBuffer("tim.alarm.");
     * str.append(pFaultFamily); str.append("."); str.append(pFaultMember);
     * str.append("."); str.append(pFaultCode); String topic = str.toString();
     * topic = topic.replace('$', 'X'); topic = topic.replace('*', 'X'); topic =
     * topic.replace('#', 'X'); return topic;
     */

    // we decided to distribute all alarms on the same topic in order to reduce
    // the number of topics for SonicMQ, the client has to make the decision if
    // the received alarm is useful for it, otherwise it will discard the alarm
    return "tim.alarm";
  }

  /**
   * @return the maximum allowed length for the fault member
   */
  public int getMaxFaultMemberLength() {
    return maxFaultMemberLength;
  }

  /**
   * @param maxFaultMemberLength the maximum allowed length for the fault member
   */
  public void setMaxFaultMemberLength(int maxFaultMemberLength) {
    this.maxFaultMemberLength = maxFaultMemberLength;
  }

  /**
   * @return the maximum allowed length for the fault family
   */
  public int getMaxFaultFamily() {
    return maxFaultFamily;
  }

  /**
   * @param maxFaultFamily the maximum allowed length for the fault family
   */
  public void setMaxFaultFamily(int maxFaultFamily) {
    this.maxFaultFamily = maxFaultFamily;
  }

/**
   * Create an AlarmCacheObject from a collection of named properties.
   * The following properties are expected in the collection:
   * <ul>
   *   <li>id</li>
   *   <li>dataTagId</li>
   *   <li>faultMember</li>
   *   <li>faultFamily</li>
   *   <li>faultCode</li>
   *   <li>alarmCondition</li>
   * </ul>
   *
   * A ConfigurationException will be thrown if one of the parameters cannot be
   * decoded to the right format. Even if no exception is thrown, it is
   * advisable to call the validate() method on the newly created object, which
   * will perform further consistency checks.
   *
   * Please note that neither this constructor nor the validate method can
   * perform dependency checks. It is up to the user to ensure that the DataTag
   * to which the alarm is attached exists.
   *
   * @param id the id of the alarm object
   * @param properties the properties containing the values for the alarm fields
   * @return the alarm object created
   */
  @Override
  public Alarm createCacheObject(final Long id, final Properties properties) {
    AlarmCacheObject alarm = new AlarmCacheObject(id);
    configureCacheObject(alarm, properties);

    // Initialise run-time parameters with default values
    alarm.setState(AlarmCondition.TERMINATE);
    alarm.setTimestamp(new Timestamp(0));
    alarm.setInfo("");

    validateConfig(alarm);
    return alarm;
  }


  /**
   * Given an alarm object, reset some of its fields according to the passed properties.
   *
   * @param alarmProperties the properties object containing the fields
   * @param alarm the alarm object to modify (is modified by this method)
   * @return always returns null, as no alarm change needs propagating to the DAQ layer
   * @throws ConfigurationException if cannot configure the Alarm from the properties
   */
  @Override
  protected Change configureCacheObject(final Alarm alarm, final Properties alarmProperties) throws ConfigurationException {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;
    String tmpStr = null;
    if ((tmpStr = alarmProperties.getProperty("dataTagId")) != null) {
      try {
        alarmCacheObject.setDataTagId(Long.valueOf(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"dataTagId\" to Long: " + tmpStr);
      }
    }
    if (alarmProperties.getProperty("faultFamily") != null) {
      alarmCacheObject.setFaultFamily(alarmProperties.getProperty("faultFamily"));
    }
    if (alarmProperties.getProperty("faultMember") != null) {
      alarmCacheObject.setFaultMember(alarmProperties.getProperty("faultMember"));
    }

    if ((tmpStr = alarmProperties.getProperty("faultCode")) != null) {
      try {
        alarmCacheObject.setFaultCode(Integer.parseInt(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"faultCode\" to int: " + tmpStr);
      }
    }

    if ((tmpStr = alarmProperties.getProperty("alarmCondition")) != null) {
      try {
        alarmCacheObject.setCondition(AlarmCondition.fromConfigXML(tmpStr));
      }
      catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Exception: Unable to create AlarmCondition object from parameter \"alarmCondition\": \n" + tmpStr);
      }
    }

    // ALARM metadata
    tmpStr = alarmProperties.getProperty("metadata");
    if (tmpStr != null) {
      Metadata metadata = new Metadata().builder().metadata(Metadata.fromJSON(tmpStr)).build();
      alarmCacheObject.setMetadata(metadata);
    }

    // set the JMS topic
    alarmCacheObject.setTopic(getTopicForAlarm(alarmCacheObject));
    return null;
  }

  @Override
  public Alarm update(final Long alarmId, final Tag tag) {
    alarmCache.acquireWriteLockOnKey(alarmId);
    try {
      Alarm alarm = alarmCache.get(alarmId);
      // Notice, in this case the update() method is putting the changes back into the cache
      return update(alarm, tag);
    } finally {
      alarmCache.releaseWriteLockOnKey(alarmId);
    }
  }

  @Override
  public void evaluateAlarm(Long alarmId) {
    alarmCache.acquireWriteLockOnKey(alarmId);
    try {
      Alarm alarm = alarmCache.get(alarmId);
      Tag tag = tagLocationService.getCopy(alarm.getTagId());
      update(alarm, tag);
    } finally {
      alarmCache.releaseWriteLockOnKey(alarmId);
    }
  }

  /**
   * Logic kept the same as in TIM1 (see {@link AlarmFacade}).
   * The locking of the objets is done in the public class.
   * Notice, in this case the update() method is putting the changes back into the cache.
   */
  private Alarm update(final Alarm alarm, final Tag tag) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;
    // Reset previous change state
    alarmCacheObject.setAlarmChangeState(AlarmChangeState.CHANGE_NONE);
    // this time is then used in LASER publication as user timestamp
    Timestamp alarmTime = tag.getCacheTimestamp();
    // not possible to evaluate alarms with associated null tag; occurs during normal operation
    // (may change in future is alarm state depends on quality f.eg.)
    if (tag.getValue() == null) {
      LOGGER.debug("Alarm update called with null Tag value - leaving Alarm status unchanged at " + alarm.getState());

      // change the alarm timestamp if the alarm has never been initialised
      if (alarmCacheObject.getTimestamp().equals(new Timestamp(0)) ){
        alarmCacheObject.setTimestamp(alarmTime);
      }
      return alarmCacheObject;
    }

    if (!tag.getDataTagQuality().isInitialised()) {
      LOGGER.debug("Alarm update called with uninitialised Tag - leaving Alarm status unchanged.");
      return alarm;
    }

    // timestamp should never be null
    if (tag.getTimestamp() == null) {
      LOGGER.warn("update() : tag value or timestamp null -> no update");
      throw new IllegalArgumentException("update method called on Alarm facade with either null tag value or null tag timestamp.");
    }

    // Compute the alarm state corresponding to the new tag value
    String newState = alarmCacheObject.getCondition().evaluateState(tag.getValue());

    // Return immediately if the alarm new state is null
    if (newState == null) {
      LOGGER.error("update() : new state would be NULL -> no update.");
      throw new IllegalStateException("Alarm evaluated to null state!");
    }

    // Return if new state is TERMINATE and old state was also TERMINATE, return original alarm (no need to save in cache)
    if (newState.equals(AlarmCondition.TERMINATE) && alarmCacheObject.getState().equals(AlarmCondition.TERMINATE)) {
      return alarm;
    }

    // Build up a prefix according to the tag value's validity and mode
    String additionalInfo = null;

    switch (tag.getMode()) {
    case DataTagConstants.MODE_MAINTENANCE:
      if (tag.isValid()) {
        additionalInfo = "[M]";
      } else {
        additionalInfo = "[M][?]";
      }
      break;
    case DataTagConstants.MODE_TEST:
      if (tag.isValid()) {
        additionalInfo = "[T]";
      } else {
        additionalInfo = "[T][?]";
      }
      break;
    default:
      if (tag.isValid()) {
        additionalInfo = "";
      } else {
        additionalInfo = "[?]";
      }
    }

    // Add another flag to the info if the value is simulated
    if (tag.isSimulated()) {
      additionalInfo = additionalInfo + "[SIM]";
    }

    // Default case: change the alarm's state
    // (1) if the alarm has never been initialised
    // (2) if tag is VALID and the alarm changes from ACTIVE->TERMINATE or TERMIATE->ACTIVE
    if (alarmCacheObject.getTimestamp().equals(new Timestamp(0))
        || (tag.isValid() && !alarmCacheObject.getState().equals(newState))) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId())
            .append(" changed STATE to ").append(newState).toString());
      }
      alarmCacheObject.setState(newState);
      alarmCacheObject.setTimestamp(alarmTime);
      alarmCacheObject.setInfo(additionalInfo);
      alarmCacheObject.setAlarmChangeState(AlarmChangeState.CHANGE_STATE);
      alarmCacheObject.notYetPublished();
      alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      return alarmCacheObject;
    }

    // Even if the alarm state itself hasn't change, the additional
    // information
    // related to the alarm (e.g. whether the alarm is valid or invalid)
    // might
    // have changed
    if (alarmCacheObject.getInfo() == null) {
      alarmCacheObject.setInfo("");
    }
    if (!alarmCacheObject.getInfo().equals(additionalInfo)) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId())
            .append(" changed INFO to ").append(additionalInfo).toString());
      }
      alarmCacheObject.setInfo(additionalInfo);
      alarmCacheObject.setAlarmChangeState(AlarmChangeState.CHANGE_PROPERTIES);
      alarmCacheObject.setTimestamp(alarmTime);
      if (!alarmCacheObject.getState().equals(AlarmCondition.TERMINATE)) {
        // We only send a notification about a property change
        // to the subscribed alarm listeners, if the alarm is active
        alarmCacheObject.notYetPublished();
        alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      }
      return alarmCacheObject;
    }

    // In all other cases, the value of the alarm related to the DataTag has
    // not changed. No need to publish an alarm change.
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId())
          .append(" has not changed.").toString());
    }
    //no change so no listener notification in this case

    //this.alarmChange = CHANGE_NONE;
    return alarmCacheObject;
  }

  /**
   * Perform a series of consistency checks on the AlarmCacheObject. This method
   * should be invoked if an AlarmCacheObject was created from a list of named
   * properties.
   *
   * @param alarm the alarm object to validate
   * @throws ConfigurationException if one of the consistency checks fails
   */
  @Override
  protected void validateConfig(final Alarm alarm) throws ConfigurationException {
    if (alarm.getId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
    }
    if (alarm.getTagId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"dataTagId\" cannot be null");
    }
    if (alarm.getFaultFamily() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultFamily\" cannot be null");
    }
    if (alarm.getFaultFamily().length() == 0 || alarm.getFaultFamily().length() > MAX_FAULT_FAMILY_LENGTH) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultFamily\" must be 1 to 20 characters long");
    }
    if (alarm.getFaultMember() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultMember\" cannot be null");
    }
    if (alarm.getFaultMember().length() == 0 || alarm.getFaultMember().length() > MAX_FAULT_MEMBER_LENGTH) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultMember\" must be 1 to 64 characters long");
    }
    if (alarm.getFaultCode() < 0) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultCode\" must be >= 0");
    }
    if (alarm.getCondition() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"alarmCondition\" cannot be null");
    }
  }

}
