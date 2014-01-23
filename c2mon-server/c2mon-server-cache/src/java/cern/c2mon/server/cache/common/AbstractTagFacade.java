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

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.C2monCacheWithListeners;
import cern.c2mon.server.cache.CommonTagFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.alarm.TagWithAlarmsImpl;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.SimpleTypeReflectionHandler;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.shared.common.datatag.DataTagValueDictionary;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.type.TagDataType;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.daq.config.DataTagAddressUpdate;
import cern.c2mon.shared.daq.config.DataTagUpdate;
import cern.c2mon.shared.daq.config.HardwareAddressUpdate;

/**
 * Common implementation of the Tag facade logic.
 * 
 * <p>Public methods in this class should notify
 * the appropriate cache listeners. Protected
 * methods can leave this up to the implementation.
 * 
 * <p>Tags are characterized by the following properties:
 * <ul>
 * <li>they have a value
 * <li>rules can be based on them
 * <li>they can be updated and invalidated
 * <li>they are intended to be displayed in views on the client application
 * </ul>
 * 
 * @param <T> the Tag type on which this facade acts 
 * @author Mark Brightwell
 */
public abstract class AbstractTagFacade<T extends Tag> extends AbstractFacade<T> implements CommonTagFacade<T> {
    
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AbstractTagFacade.class);
  
  /**
   * Mail logger.
   */
  private static final Logger EMAILLOGGER = Logger.getLogger("AdministratorEmail");

  /**
   * The cache for objects of type T.
   */
  protected C2monCacheWithListeners<Long, T> tagCache;
  
  /**
   * Reference to the Alarm facade.
   */
  private AlarmFacade alarmFacade;
  
  /**
   * Reference to the alarm cache.
   */
  private AlarmCache alarmCache;
  
  /**
   * Unique constructor.
   * @param tagCache the particular tag cache needs passing in from the facade implementation
   * @param alarmFacade the alarm facade bean
   * @param alarmCache the alarm cache
   */
  protected AbstractTagFacade(final C2monCacheWithListeners<Long, T> tagCache, final AlarmFacade alarmFacade, final AlarmCache alarmCache) {
    super();
    this.tagCache = tagCache;
    this.alarmFacade = alarmFacade;
    this.alarmCache = alarmCache;
  }
  
  protected abstract void invalidateQuietly(T tag, TagQualityStatus statusToAdd, String statusDescription, Timestamp timestamp);
  /**
   * As for the other invalidate method, but the Tag is passed instead of this id.
   * 
   * <p>If the invalidation causes no changes, the cache object is not updated (see filterout method).
   * 
   * @param tag the Tag to invalidate
   * @param statusToAdd status flag to add
   * @param statusDescription description associated to this flag; leave as null if no description is required
   * @param timestamp time of the invalidation
   */
  private void invalidate(T tag, final TagQualityStatus statusToAdd, final String statusDescription, Timestamp timestamp) {  
    if (!filteroutInvalidation(tag, statusToAdd, statusDescription, timestamp)) {              
      invalidateQuietly(tag, statusToAdd, statusDescription, timestamp);
      tagCache.notifyListenersOfUpdate(tag);      
    } else {
      if (LOGGER.isTraceEnabled()){
        LOGGER.trace("Filtering out repeated invalidation for tag " + tag.getId());
      }      
    }    
  }

  @Override
  public void invalidate(Long tagId, final TagQualityStatus statusToAdd, final String statusDescription, Timestamp timestamp) {
    tagCache.acquireWriteLockOnKey(tagId);
    try {
      T tag = tagCache.get(tagId);  
      invalidate(tag, statusToAdd, statusDescription, timestamp);
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.error("Unable to locate tag in cache (id " + tagId + ") - no invalidation performed (cache is " + tagCache.getClass() + ")");
    } finally {
      tagCache.releaseWriteLockOnKey(tagId);
    }
  }

  @Override
  public List<Alarm> evaluateAlarms(final T tag) {
    List<Alarm> linkedAlarms = new ArrayList<Alarm>();
    tagCache.acquireReadLockOnKey(tag.getId());
    try {
      for (Long alarmId : tag.getAlarmIds()) {
        linkedAlarms.add(alarmFacade.update(alarmId, tag.getId()));          
      }
    } finally {
      tagCache.releaseReadLockOnKey(tag.getId());
    }   
    return linkedAlarms;   
  }
  
  
  /**
   * TODO set JMS client topic still needs doing
   * 
   * Sets the fields of the AbstractTagCacheObject from the Properties object.
   * Notice only non-null properties are set, the others staying unaffected
   * by this method.
   * 
   * @param tag
   * @param properties
   * @return the returned update object with changes that need sending to the 
   *              DAQ (only used when reconfiguring a Data/ControlTag, not rules)
   *              IMPORTANT: the change id and equipment id still needs setting on the returned object
   *                         in the DataTag-specific facade
   * @throws ConfigurationException
   */
  protected DataTagUpdate setCommonProperties(AbstractTagCacheObject tag, Properties properties) 
                                                                             throws ConfigurationException {
    DataTagUpdate dataTagUpdate = new DataTagUpdate();
    dataTagUpdate.setDataTagId(tag.getId());
    tagCache.acquireWriteLockOnKey(tag.getId());
    try {
      String tmpStr = null;
      // TAG ID (not used so far as Id stored as entitypkey)
//      if ((tmpStr = properties.getProperty("id")) != null) {
//        try {
//          tag.setId(Long.valueOf(tmpStr));
//        }
//        catch (NumberFormatException e) {
//          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: "
//              + "Unable to convert parameter \"id\" to Long: " + tmpStr);
//        }
//      }
      
      // TAG name and topic derived from name
      if ((tmpStr = properties.getProperty("name")) != null) {
        tag.setName(tmpStr);
        dataTagUpdate.setName(tmpStr);
        //this.topic = getTopicForName(this.name);
      }
      
      // TAG description
      if ((tmpStr = properties.getProperty("description")) != null) {
        tag.setDescription(tmpStr);
        dataTagUpdate.setName(tmpStr);
      }
      

      // TAG data type
      if ((tmpStr = properties.getProperty("dataType")) != null) {
        tag.setDataType(tmpStr);
        dataTagUpdate.setDataType(tmpStr);
      }
      
      
      // TAG mode
      if ((tmpStr = properties.getProperty("mode")) != null) {
        try {
          tag.setMode(Short.parseShort(tmpStr));
          dataTagUpdate.setMode(Short.parseShort(tmpStr));
        }
        catch (NumberFormatException e) {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"mode\" to short: " + tmpStr);
        }
      }
      
      // TAG log flag
      if ((tmpStr = properties.getProperty("isLogged")) != null) {
        tag.setLogged(tmpStr.equalsIgnoreCase("true"));        
      }    
      
      // TAG unit
      tmpStr = properties.getProperty("unit");
      if (tmpStr != null) {
        tag.setUnit(checkAndSetNull(tmpStr));        
      }
      
      
      // TAG value dictionary 
      if (properties.getProperty("valueDictionary") != null) {
        tag.setValueDictionary(new DataTagValueDictionary());
        tmpStr = properties.getProperty("valueDictionary");
        if (tmpStr != null) {
          StringTokenizer dictionaryTokens = new StringTokenizer(tmpStr, ";");
          String[] valueDescriptions = null;
          while (dictionaryTokens.hasMoreTokens()) { 
           valueDescriptions = dictionaryTokens.nextToken().split("=");
            if (valueDescriptions.length == 2) {
              tag.getValueDictionary().addDescription(
                TypeConverter.cast(valueDescriptions[0], tag.getDataType()),
                valueDescriptions[1]
              );
            }
          }
        }
      }
     
      
      // DIP address
      if (properties.getProperty("dipAddress") != null) {
        tag.setDipAddress(checkAndSetNull(properties.getProperty("dipAddress")));
      }
      
      // JAPC address
      if (properties.getProperty("japcAddress") != null) {
        tag.setJapcAddress(checkAndSetNull(properties.getProperty("japcAddress")));
      }    
    } finally {
      tagCache.releaseWriteLockOnKey(tag.getId());
    }
    
    return dataTagUpdate;
  }
  
  /**
   * Checks that the AbstractTagCacheObject passes all validation tests for
   * being included in TIM. This method should be called during runtime
   * reconfigurations for instance.
   * 
   * TODO commented out desc and dictionary null checks below (as test server does not satisfy these) - introduce them again for operation?
   * 
   * @param tag the tag to validate
   * @throws ConfigurationException if a validation test fails
   */
  protected void validateTagConfig(final T tag) throws ConfigurationException {
    try {
      tagCache.acquireReadLockOnKey(tag.getId());
      if (tag.getId() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
      }
      if (tag.getName() == null) { 
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
      }
      if (tag.getName().length() == 0 ) { //|| tag.getName().length() > 60
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be empty");
      }
//      if (tag.getDescription() == null) { 
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" cannot be null");
//      }
//      if (tag.getDescription().length() == 0 || tag.getDescription().length() > 100) {
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" must be 1 to 100 characters long");
//      }
      if (tag.getDataType() == null) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"dataType\" cannot be null");
      }
      if (!TagDataType.isValidDataType(tag.getDataType())) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"dataType\" must be Boolean, String, Integer, Float, Double or Long");
      }
//      if (tag.getValueDictionary() == null) {
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"valueDictionary\" cannot be null");
//      }
      if (tag.getMode() != DataTagConstants.MODE_OPERATIONAL && tag.getMode() != DataTagConstants.MODE_TEST && tag.getMode() != DataTagConstants.MODE_MAINTENANCE) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Invalid value for parameter \"mode\".");
      }
      //TODO setting client topic still needs doing...
//      if (tag.getTopic() == null) {
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"topic\" cannot be null");
//      }
      if (tag.getUnit() != null) {
        if (tag.getUnit().length() > 20) {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"unit\" must be 0 to 20 characters long");
        }
      }
    } finally {
      tagCache.releaseReadLockOnKey(tag.getId());
    }
    
  }

  /**
   * Sets default values of the fields that change on incoming
   * values.
   * 
   * @param tag
   */
  protected void setDefaultRuntimeProperties(AbstractTagCacheObject tag) {
    try {
      tagCache.acquireWriteLockOnKey(tag.getId());
      tag.setValue(null);    
      tag.setValueDescription("");
      tag.setSimulated(false);    
    } finally {
      tagCache.releaseWriteLockOnKey(tag.getId());
    }    
  }
    
  /**
   * Adds the rule to the list of those that need evaluating when
   * this tag is updated.
   * 
   * <p>Note also adjust text field of cache object.
   * 
   * @param tagId
   * @param ruleTagId
   */
  @Override
  public void addDependentRuleToTag(final T tag, final Long ruleTagId) {
    AbstractTagCacheObject cacheObject = (AbstractTagCacheObject) tag;
    cacheObject.getRuleIds().add(ruleTagId);
    StringBuilder bld = new StringBuilder();      
    for (Long id : cacheObject.getRuleIds()) {
      bld.append(id).append(", ");
    }    
    cacheObject.setRuleIdsString(bld.toString().substring(0, bld.length() - 2)); //remove ", "     
  }
  
  /**
   * Removes this rule from the list of those that need evaluating when
   * this tag is updated.
   * 
   * <p>Note also adjusts text field of cache object.
   * 
   * @param tag the tag used in the rule (directly, not via another rule)
   * @param ruleTagId the id of the rule
   */
  @Override
  public void removeDependentRuleFromTag(final T tag, final Long ruleTagId) {
    tagCache.acquireWriteLockOnKey(tag.getId());
    try {
      AbstractTagCacheObject cacheObject = (AbstractTagCacheObject) tag;
      cacheObject.getRuleIds().remove(ruleTagId);
      StringBuilder bld = new StringBuilder();      
      for (Long id : cacheObject.getRuleIds()) {
        bld.append(id).append(",");
      }      
      if (bld.length() > 0) {
        cacheObject.setRuleIdsString(bld.toString().substring(0, bld.length() - 1)); //remove ", "     
      }
    } finally {
      tagCache.releaseWriteLockOnKey(tag.getId());
    }
  }
  
  
//  /**
//   * Change the status of the Tag. This method notifies the listeners that
//   * a change has been made. TODO notify here or not? - yes, but make sure status
//   * is also forwarded to client...
//   * @param tag
//   * @param status
//   */
//  public void setStatus(final T tag, final DataTagConstants.Status status) {
//    //T cacheObject = (AbstractTagCacheObject) tag;
//    try {     
//      tagCache.acquireWriteLockOnKey(tag.getId());
//      ((AbstractTagCacheObject) tag).setStatus(Status.RECONFIGURATION_ERROR); 
//      tagCache.put(tag.getId(), tag);
//    } finally {
//      tagCache.releaseWriteLockOnKey(tag.getId());
//    }
//  }
   
  @Override
  public void addAlarm(final T tag, final Long alarmId) {
    tagCache.acquireWriteLockOnKey(tag.getId());
    try {     
      tag.getAlarmIds().add(alarmId);      
    } finally {
      tagCache.releaseWriteLockOnKey(tag.getId());
    }
  }
  
  @Override
  public void setQuality(final Long tagId, final Collection<TagQualityStatus> flagsToAdd, final Collection<TagQualityStatus> flagsToRemove, 
                         final Map<TagQualityStatus, String> qualityDescription, final Timestamp timestamp) {
    tagCache.acquireWriteLockOnKey(tagId);
    try {
      T tag = tagCache.get(tagId);
      doSetQuality(tag, flagsToAdd, flagsToRemove, qualityDescription, timestamp);
      tagCache.put(tag.getId(), tag);
    } finally {
      tagCache.releaseWriteLockOnKey(tagId);
    }    
  }
  
  /**
   * Locking of the tag is handled within the public wrapper methods. 
   */
  private void doSetQuality(final T tag,
                            final Collection<TagQualityStatus> flagsToAdd,
                            final Collection<TagQualityStatus> flagsToRemove, 
                            final Map<TagQualityStatus, String> qualityDescription,
                            final Timestamp timestamp) {
    if (flagsToRemove == null && flagsToAdd == null) {
      LOGGER.warn("Attempting to set quality in TagFacade with no Quality flags to remove or set!");
    }
    
    if (flagsToRemove != null) {
      for (TagQualityStatus status : flagsToRemove) {
        tag.getDataTagQuality().removeInvalidStatus(status);
      }
    }
    if (flagsToAdd != null) {
      for (TagQualityStatus status : flagsToAdd) {
        tag.getDataTagQuality().addInvalidStatus(status, qualityDescription.get(status));
      }
    }      
    ((AbstractTagCacheObject) tag).setCacheTimestamp(timestamp);
  }
  
  /**
   * Accesses and locks Tag in cache, fetches associated
   * alarms (since Alarm evaluation is on the same thread as
   * the Tag cache update, these correspond to the Tag value
   * and cannot be modified during this method).
   */
  public TagWithAlarms getTagWithAlarms(Long id) {    
    tagCache.acquireReadLockOnKey(id);
    try {
      T tag = tagCache.get(id);
      Collection<Alarm> alarms = new LinkedList<Alarm>(); 
      for (Long alarmId : tag.getAlarmIds()) {
        alarms.add(alarmCache.getCopy(alarmId));
      }
      return new TagWithAlarmsImpl(tag, alarms);
    } finally {
      tagCache.releaseReadLockOnKey(id);
    }
  }
  
  /**
   * Sets the DataTagAddress part of an update from the XML String. 
   * @param dataTagAddress the new address
   * @param dataTagUpdate the update object for which the address needs setting
   * @throws IllegalAccessException 
   * @throws IllegalArgumentException 
   */
  protected void setUpdateDataTagAddress(final DataTagAddress dataTagAddress, final DataTagUpdate dataTagUpdate) throws IllegalArgumentException, IllegalAccessException {   
    DataTagAddressUpdate dataTagAddressUpdate = new DataTagAddressUpdate();
    dataTagUpdate.setDataTagAddressUpdate(dataTagAddressUpdate);
    dataTagAddressUpdate.setGuaranteedDelivery(dataTagAddress.isGuaranteedDelivery());
    dataTagAddressUpdate.setPriority(dataTagAddress.getPriority());
    if (dataTagAddress.getTimeToLive() != DataTagAddress.TTL_FOREVER) {
      dataTagAddressUpdate.setTimeToLive(dataTagAddress.getTimeToLive());
    }
    if (dataTagAddress.getValueDeadbandType() != DataTagDeadband.DEADBAND_NONE) {
      dataTagAddressUpdate.setValueDeadbandType(dataTagAddress.getValueDeadbandType());
      dataTagAddressUpdate.setValueDeadband(dataTagAddress.getValueDeadband());
    } else {
      dataTagAddressUpdate.addFieldToRemove("valueDeadbandType");
      dataTagAddressUpdate.addFieldToRemove("valueDeadband");
    }
    if (dataTagAddress.getTimeDeadband() != DataTagDeadband.DEADBAND_NONE) {
      dataTagAddressUpdate.setTimeDeadband(dataTagAddress.getTimeDeadband());
    } else {
      dataTagAddressUpdate.addFieldToRemove("timeDeadband");        
    }
    if (dataTagAddress.getHardwareAddress() != null) {
      HardwareAddressUpdate hardwareAddressUpdate = new HardwareAddressUpdate(dataTagAddress.getHardwareAddress().getClass().getName());
      dataTagAddressUpdate.setHardwareAddressUpdate(hardwareAddressUpdate);
      SimpleTypeReflectionHandler reflectionHandler = new SimpleTypeReflectionHandler();
      for (Field field : reflectionHandler.getNonTransientSimpleFields(dataTagAddress.getHardwareAddress().getClass())) {
        field.setAccessible(true);
        hardwareAddressUpdate.getChangedValues().put(field.getName(), field.get(dataTagAddress.getHardwareAddress()));
      }
    }    
  }
  /**
   * Checks if the new Tag value should be filtered out or updated.
   * Is filtered out if value, value description and quality are
   * the same.
   * 
   * @param timestamp the new timestamp 
   * @param valueDescription the new description
   * @param value the new value
   * @param tag the tag that is updated
   * @param statusToAdd the tag quality status to add; leave null if the tag is to be validated
   * @param statusDescription the new status description; leave null if the tag is to be validated
   * @return true if it should be filtered out
   * @throws NullPointerException if called with null tag parameter
   * @throws IllegalArgumentException if status description is not null but statusToAdd is (does not make any sense!) or the same for the value
   */
  public boolean filterout(Tag tag, Object value, String valueDescription, 
                            TagQualityStatus statusToAdd, String statusDescription, Timestamp timestamp) { 
    if (statusToAdd == null && statusDescription != null) {
      throw new IllegalArgumentException("Filterout method called with non-null status description but null status");
    }
    if (value == null && valueDescription != null) {
      throw new IllegalArgumentException("Filterout method called with non-null value description but null value");
    }
    boolean sameValue;
    if (tag.getValue() != null){
      sameValue = tag.getValue().equals(value);
    } else {
      sameValue = (value == null);
    }
    if (!sameValue) {
      return false;
    }
    
    boolean sameDescription;
    if (tag.getValueDescription() != null){
      sameDescription = tag.getValueDescription().equalsIgnoreCase(valueDescription);
    } else {
      sameDescription = (valueDescription == null);
    }
    if (!sameDescription) {
      return false;
    }
    
    boolean sameQuality;
    if (statusToAdd == null){
      sameQuality = tag.getDataTagQuality().isValid();
    } else {
      sameQuality = (tag.getDataTagQuality() != null 
          && tag.getDataTagQuality().isInvalidStatusSetWithSameDescription(statusToAdd, statusDescription));
    }
    return sameQuality;      
  }
  
  /**
   * As for general filterout method, but for invalidation only.
   * @param tag the current tag
   * @param statusToAdd the status to add
   * @param statusDescription the status description to use
   * @param timestamp the invalidation time
   * @return true if should be filtered
   */
  public boolean filteroutInvalidation(T tag, TagQualityStatus statusToAdd, String statusDescription, Timestamp timestamp) {
    return filterout(tag, tag.getValue(), tag.getValueDescription(), statusToAdd, statusDescription, timestamp);
  }
  
  /**
   * As for general filterout method, but for valid updates only.
   * @param tag the current tag
   * @param value the new value
   * @param valueDescription the new value description
   * @param timestamp the update time
   * @return true if should be filtered
   */
  public boolean filteroutValid(T tag, Object value, String valueDescription, Timestamp timestamp) {
    return filterout(tag, value, valueDescription, null, null, timestamp);
  }
  
}
