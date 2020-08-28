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
package cern.c2mon.server.cache.alarm.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.common.AbstractFacade;
import cern.c2mon.server.cache.util.MetadataUtils;
import cern.c2mon.server.common.alarm.*;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.alarm.condition.AlarmCondition;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * Implementation of the AlarmFacade.l
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
   * Reference to the Alarm cache.
   */
  private final AlarmCache alarmCache;

  /**
   * Used to validate the alarm object at runtime configuration.
   */
  private int maxFaultFamily = MAX_FAULT_FAMILY_LENGTH;

  /**
   * Used to validate the alarm object at runtime configuration.
   */
  private int maxFaultMemberLength = MAX_FAULT_MEMBER_LENGTH;

  private final TagLocationService tagLocationService;

  private final AlarmCacheUpdater alarmCacheUpdater;
  
  private final AlarmAggregatorNotifier alarmAggregatorNotifier;

  @Autowired
  public AlarmFacadeImpl(final AlarmCache alarmCache, 
                         final TagLocationService tagLocationService, 
                         final AlarmCacheUpdater alarmCacheUpdater, 
                         final AlarmAggregatorNotifier alarmAggregatorNotifier) {
    super();
    this.alarmCache = alarmCache;
    this.tagLocationService = tagLocationService;
    this.alarmCacheUpdater = alarmCacheUpdater;
    this.alarmAggregatorNotifier = alarmAggregatorNotifier;
  }

  /**
   * @return the maximum allowed length for the fault member
   */
  public int getMaxFaultMemberLength() {
    return maxFaultMemberLength;
  }

  /**
   * @param maxFaultMemberLength
   *          the maximum allowed length for the fault member
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
   * @param maxFaultFamily
   *          the maximum allowed length for the fault family
   */
  public void setMaxFaultFamily(int maxFaultFamily) {
    this.maxFaultFamily = maxFaultFamily;
  }

  /**
   * Create an AlarmCacheObject from a collection of named properties. The
   * following properties are expected in the collection:
   * <ul>
   * <li>id</li>
   * <li>dataTagId</li>
   * <li>faultMember</li>
   * <li>faultFamily</li>
   * <li>faultCode</li>
   * <li>alarmCondition</li>
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
   * @param id
   *          the id of the alarm object
   * @param properties
   *          the properties containing the values for the alarm fields
   * @return the alarm object created
   */
  @Override
  public Alarm createCacheObject(final Long id, final Properties properties) {
    AlarmCacheObject alarm = new AlarmCacheObject(id);
    configureCacheObject(alarm, properties);

    // Initialise run-time parameters with default values
    alarm.setActive(false);
    alarm.setInternalActive(false);
    alarm.setTimestamp(new Timestamp(0));
    alarm.setSourceTimestamp(new Timestamp(0));
    alarm.setInfo("");

    validateConfig(alarm);
    return alarm;
  }

  /**
   * Given an alarm object, reset some of its fields according to the passed
   * properties.
   *
   * @param alarmProperties
   *          the properties object containing the fields
   * @param alarm
   *          the alarm object to modify (is modified by this method)
   * @return always returns null, as no alarm change needs propagating to the
   *         DAQ layer
   * @throws ConfigurationException
   *           if cannot configure the Alarm from the properties
   */
  @Override
  protected Change configureCacheObject(final Alarm alarm, final Properties alarmProperties) throws ConfigurationException {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;
    String tmpStr = null;
    if ((tmpStr = alarmProperties.getProperty("dataTagId")) != null) {
      try {
        alarmCacheObject.setDataTagId(Long.valueOf(tmpStr));
      } catch (NumberFormatException e) {
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
      } catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"faultCode\" to int: " + tmpStr);
      }
    }

    if ((tmpStr = alarmProperties.getProperty("alarmCondition")) != null) {
      try {
        alarmCacheObject.setCondition(AlarmCondition.fromConfigXML(tmpStr));
      } catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            "Exception: Unable to create AlarmCondition object from parameter \"alarmCondition\": \n" + tmpStr);
      }
    }

    // ALARM metadata
    cern.c2mon.server.common.metadata.Metadata newMetadata = MetadataUtils.parseMetadataConfiguration(alarmProperties, alarmCacheObject.getMetadata());
    alarmCacheObject.setMetadata(newMetadata);

    return null;
  }

  @Override
  public Alarm update(final Long alarmId, final Tag tag) {
    alarmCache.acquireWriteLockOnKey(alarmId);
    try {
      Alarm alarm = alarmCache.getCopy(alarmId);
      // Notice, in this case the update() method is putting the changes back
      // into the cache
      return alarmCacheUpdater.update(alarm, tag);
    } finally {
      alarmCache.releaseWriteLockOnKey(alarmId);
    }
  }
  
  @Override
  public void notifyOnAlarmRemoval(AlarmCacheObject removedAlarm) {
    removedAlarm.setActive(false);
    removedAlarm.setInfo("Alarm was removed");
    removedAlarm.setTimestamp(new Timestamp(System.currentTimeMillis()));

    alarmCache.notifyListenersOfUpdate(removedAlarm);
    
    TagWithAlarms tagWithAlarms = getTagWithAlarms(removedAlarm.getDataTagId());
    List<Alarm> alarmList = new ArrayList<Alarm>(tagWithAlarms.getAlarms());
    alarmList.add(removedAlarm);
    alarmAggregatorNotifier.notifyOnUpdate(tagWithAlarms.getTag(), alarmList);
  }

  @Override
  public void evaluateAlarm(Long alarmId) {
    alarmCache.acquireWriteLockOnKey(alarmId);
    Tag tagCopy;
    try {
      Alarm alarm = alarmCache.getCopy(alarmId);
      tagCopy = tagLocationService.getCopy(alarm.getTagId());
      
      alarmCacheUpdater.update(alarm, tagCopy);
    } finally {
      alarmCache.releaseWriteLockOnKey(alarmId);
    }
    
    alarmAggregatorNotifier.notifyOnUpdate(tagCopy, getAlarms(tagCopy));
  }
  
  @Override
  public void notifyOnAlarmOscillationReset(final Tag tag) {
    alarmAggregatorNotifier.notifyOnUpdate(tag, getAlarms(tag));
  }

  /**
   * Perform a series of consistency checks on the AlarmCacheObject. This method
   * should be invoked if an AlarmCacheObject was created from a list of named
   * properties.
   *
   * @param alarm
   *          the alarm object to validate
   * @throws ConfigurationException
   *           if one of the consistency checks fails
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
  
  /**
   * Return the Tag with associated evaluated Alarms corresponding
   * to the Tag value. A frozen copy is returned.
   * 
   * @param id the Tag id
   * @return Tag and Alarms, with corresponding values (no longer residing in cache)
   */
  private TagWithAlarms getTagWithAlarms(Long tagId) {
    Tag tag = tagLocationService.getCopy(tagId);
    List<Alarm> alarms = getAlarms(tag);
    
    return new TagWithAlarmsImpl(tag, alarms);
  }
  
  /**
   * Given a tag, get it's alarms.
   *
   * @param tag The tag.
   * @return A list of alarms
   */
  private List<Alarm> getAlarms(Tag tag) {
    if (tag.getAlarmIds() == null || tag.getAlarmIds().isEmpty()) {
      return new ArrayList<Alarm>();
    }
    
    return tag.getAlarmIds().stream().map(id -> alarmCache.get(id)).collect(Collectors.toList());
  }
}
