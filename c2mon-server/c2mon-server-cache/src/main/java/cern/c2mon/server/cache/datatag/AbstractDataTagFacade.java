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
package cern.c2mon.server.cache.datatag;

import java.sql.Timestamp;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import cern.c2mon.server.common.expression.Evaluator;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.C2monCacheWithListeners;
import cern.c2mon.server.cache.ControlTagFacade;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.common.AbstractTagFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.tag.CommonTagObjectFacade;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.thread.Event;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.daq.config.DataTagUpdate;

import static cern.c2mon.shared.common.type.TypeConverter.isKnownClass;

/**
 * {@link DataTagFacade} and {@link ControlTagFacade} have some functionality
 * in common which have been joined into this abstract class. But also in the
 * future this class may be useful for all facades which provides methods for
 * objects that implement the {@link DataTag} interface.
 *
 * @author Matthias Braeger
 *
 * @param <T> Class extending the {@link DataTag} interface
 */
@Slf4j
public abstract class AbstractDataTagFacade<T extends DataTag> extends AbstractTagFacade<T> {

  /**
   * Interface to cache module.
   */
  private final DataTagCacheObjectFacade dataTagCacheObjectFacade;

  /** Reference to the Equipment facade */
  private EquipmentFacade equipmentFacade = null;

  /** Reference to the SubEquipment facade */
  private SubEquipmentFacade subEquipmentFacade = null;

  /**
   * Reference to qualityConverter bean.
   */
  private final QualityConverter qualityConverter;

  /**
   * Unique constructor.
   *
   * @param tagCache the particular tag cache needs passing in from the facade
   *                 implementation
   * @param alarmFacade the alarm facade bean
   * @param alarmCache the alarm cache
   * @param commonTagObjectFacade Interface exposing common methods for
   *                              modifying Tag objects(DataTags, ControlTags and RuleTags)
   * @param dataTagCacheObjectFacade the object that acts directly on the cache object
   * @param qualityConverter the bean managing how the quality changes on incoming values
   */
  protected AbstractDataTagFacade(final C2monCacheWithListeners<Long, T> tagCache,
                                  final AlarmFacade alarmFacade,
                                  final AlarmCache alarmCache,
                                  final CommonTagObjectFacade<T> commonTagObjectFacade,
                                  final DataTagCacheObjectFacade dataTagCacheObjectFacade,
                                  final QualityConverter qualityConverter) {
    super(tagCache, alarmFacade, alarmCache);

    this.dataTagCacheObjectFacade = dataTagCacheObjectFacade;
    this.qualityConverter = qualityConverter;
  }

  /**
   * Sets the reference to the equipment facade which is needed for the
   * configuration of the cache object
   * @param equipmentFacade Reference to the equipment facade
   */
  protected final void setEquipmentFacade(final EquipmentFacade equipmentFacade) {
    this.equipmentFacade = equipmentFacade;
  }

  /**
   * Sets the reference to the subequipment facade which is needed for the
   * configuration of the cache object
   * @param subEquipmentFacade Reference to the subequipment facade
   */
  public void setSubEquipmentFacade(SubEquipmentFacade subEquipmentFacade) {
    this.subEquipmentFacade = subEquipmentFacade;
  }

  /**
   * Updates configuration fields of the cache object with those contained
   * in the properties object. By "configuration fields" we are referring
   * to fields open to reconfiguration during runtime. In particular, values
   * and timestamps are not modified by this method: updates due to incoming
   * data should be processed using the other update and invalidate methods.
   *
   * <p>This method is thread-safe (it performs the required synchronization
   * on the cache object residing in the cache.
   *
   * <p>This method should preferably be performed on an object outside the
   * cache before being applied to the object residing in the cache, since
   * changes cannot be rolled back if the validation fails.
   *
   * <p>The returned change object can be used to inform the data when an
   * update is performed (not used during DataTag creation).
   *
   * throws ConfigurationException if the reconfigured cache object fails
   * validation checks (unchecked) throws IllegalArgumentException thrown
   * when creating DAQ change event for HardwareAddress (unchecked)
   *
   * @param dataTag the cache object to reconfigure (the object is modified by
   *                this method)
   * @param properties the properties that need reconfiguring
   * @return a DataTagUpdate object with the changes needed to be passed to
   * the DAQ
   * @throws IllegalAccessException thrown when creating DAQ change event for
   * HardwareAddress
   */
  @Override
  public final DataTagUpdate configureCacheObject(final T dataTag, final Properties properties)
      throws ConfigurationException, IllegalArgumentException, IllegalAccessException {

    DataTagCacheObject dataTagCacheObject = (DataTagCacheObject) dataTag;
    DataTagUpdate dataTagUpdate = setCommonProperties(dataTagCacheObject, properties);
    String tmpStr;

    // TAG equipment identifier
    // Ignore the equipment id for control tags as control tags are INDIRECTLY
    // referenced via the equipment's aliveTag and commFaultTag fields
    if (equipmentFacade != null && !(dataTagCacheObject instanceof ControlTag)) {
      if ((tmpStr = properties.getProperty("equipmentId")) != null) {
        try {
          dataTagCacheObject.setEquipmentId(Long.valueOf(tmpStr));
          dataTagCacheObject.setProcessId(equipmentFacade.getProcessIdForAbstractEquipment(dataTagCacheObject.getEquipmentId()));
        }
        catch (NumberFormatException e) {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: "
              +  "Unable to convert parameter \"equipmentId\" to Long: " + tmpStr);
        }
      }

      // TIMS-951: Allow attachment of DataTags to SubEquipments
      else if ((tmpStr = properties.getProperty("subEquipmentId")) != null) {
        try {
          dataTagCacheObject.setSubEquipmentId(Long.valueOf(tmpStr));
          dataTagCacheObject.setProcessId(subEquipmentFacade.getProcessIdForAbstractEquipment(dataTagCacheObject.getSubEquipmentId()));
        }
        catch (NumberFormatException e) {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: "
              +  "Unable to convert parameter \"subEquipmentId\" to Long: " + tmpStr);
        }
      }
    }

    if ((tmpStr = properties.getProperty("minValue")) != null) {
      if (tmpStr.equals("null")) {
        dataTagCacheObject.setMinValue(null);
        dataTagUpdate.setMinValue(null);
      } else {
        Comparable comparableMin = (Comparable) TypeConverter.cast(tmpStr, dataTagCacheObject.getDataType());
        dataTagCacheObject.setMinValue(comparableMin);
        dataTagUpdate.setMinValue((Number) comparableMin);
      }
    }

    if ((tmpStr = properties.getProperty("maxValue")) != null) {
      if (tmpStr.equals("null")) {
        dataTagCacheObject.setMaxValue(null);
        dataTagUpdate.setMaxValue(null);
      } else {
        Comparable comparableMax = (Comparable) TypeConverter.cast(tmpStr, dataTagCacheObject.getDataType());
        dataTagCacheObject.setMaxValue(comparableMax);
        dataTagUpdate.setMaxValue((Number) comparableMax);
      }
    }

    // TAG address
    tmpStr = properties.getProperty("address");
    if (tmpStr != null) {
      DataTagAddress dataTagAddress = DataTagAddress.fromConfigXML(tmpStr);
      dataTagCacheObject.setAddress(dataTagAddress);
      setUpdateDataTagAddress(dataTagAddress, dataTagUpdate);
    }

    if (dataTag.getEquipmentId() != null)
      dataTagUpdate.setEquipmentId(dataTag.getEquipmentId());

    return dataTagUpdate;
  }

  /**
   * Method containing all the logic for filtering out incoming datatag updates
   * before any updates are attempted. Call within synchronized block.
   * @param dataTag
   * @param sourceDataTagValue
   * @return true if the update should be filtered out, false if it should be kept
   */
  private boolean filterout(DataTag dataTag, SourceDataTagValue sourceDataTagValue) {

    //NOW checked in SourceUpdateManager; source values are NOT filtered out now, but tag is invalidated (unless filtered out for timestamp
    //reasons).
//    if (sourceDataTagValue.getValue() == null) {
//      log.warn("Attempted to update DataTag " + sourceDataTagValue.getId() + " with a null value.");
//      return true;
//    }

    //set the timestamps to compare:
    //(1)if both daq timestamps are set, compare these
    //(2)if not, use the source timestamps
    //TODO change here once the source value has both DAQ and src timestamps
    Timestamp dataTagTimestamp, sourceTagTimestamp;

    if (dataTag.getDaqTimestamp() != null && sourceDataTagValue.getDaqTimestamp() != null) {

      dataTagTimestamp = dataTag.getDaqTimestamp();
      sourceTagTimestamp = sourceDataTagValue.getDaqTimestamp();

    } else if (dataTag.getSourceTimestamp() != null && sourceDataTagValue.getTimestamp() != null) {

      //only for backwards compatibility until all DAQs are sending DAQ timestamps
      dataTagTimestamp = dataTag.getSourceTimestamp();
      sourceTagTimestamp = sourceDataTagValue.getTimestamp();

    } else {

      return false; //since only server timestamp is set on dataTag, all incoming source values should be accepted
    }

    //neither timestamps should be null from here

    /*
     * Do NOT update the tag if the new timestamp is OLDER.
     * EXCEPTION:
     * If the datatag is currently marked as INACCESSIBLE, we can override the value BUT
     * the timestamp will be the current time.
     * EXCEPTION2:
     * If the user sets the forceUpdate flag, we perform the update regardless of whether all other
     * conditions are met. This flag must be used with great care.
     */
    //removed forceUpdate below
    if (sourceTagTimestamp.before(dataTagTimestamp)) {

      if (dataTag.getDataTagQuality() == null || dataTag.getDataTagQuality().isAccessible()) {

        log.debug("update() : older timestamp and not inaccessible -> reject update");
        return true;

      }
      else {
        log.debug("update() : older timestamp but tag currently inaccessible -> update with older timestamp");
      }
    }

    /*
     * If the timestamp of the new value is the same as the old timestamp, only
     * perform an update if the values are different (and valid). The values are considered
     * to be different by default if the old value is null. EXCEPTION: If the
     * user sets the forceUpdate flag, we perform the update regardless of
     * whether all other conditions are met. This flag must be used with great
     * care.
     */
    if (sourceTagTimestamp.equals(dataTagTimestamp) && dataTag.getValue() != null
        && sourceDataTagValue.getValue().equals(dataTag.getValue()) && dataTag.getDataTagQuality().isValid()
        && sourceDataTagValue.getQuality() != null  && sourceDataTagValue.getQuality().isValid()) {

      log.debug("update() : values and timestamps are equal, so nothing to update -> reject update");
      return true;
    }

    //false means allow the update to proceed
    return false;
  }


  /**
   * To be called internally only within a dataTag synchronized block (if object in cache).
   * Does not notify listeners. Only cache timestamp is set (others are null). Should not be made public.
   *
   * @param dataTag The tag which shall be invalidated
   * @param statusToAdd The quality status to be added to the data tag
   * @param description The description of the change of quality
   * @param timestamp the cache timestamp to set (others left unchanged)
   */
  @Override
  protected void invalidateQuietly(final T dataTag,
                                   final TagQualityStatus statusToAdd,
                                   final String description,
                                   final Timestamp timestamp) {
    dataTagCacheObjectFacade.addQualityFlag(dataTag, statusToAdd, description);
    dataTagCacheObjectFacade.setTimestamps(dataTag, null, null, timestamp);
  }


  /**
   * Updates the tag object if the value is not filtered out. Contains the logic on when a
   * DataTagCacheObject should be updated with new values and when not (in particular
   * timestamp restrictions).
   *
   * <p>Also notifies the listeners if an update was performed.
   *
   * <p>Notice the tag is not put back in the cache here.
   *
   * @param dataTag is modified by the method
   * @param sourceDataTagValue the source value received from the DAQ
   * @return true if an update was performed (i.e. the value was not filtered out)
   */
  protected final Event<Boolean> updateFromSource(final T dataTag, final SourceDataTagValue sourceDataTagValue) {
    long eventTime = 0;
    Boolean updated = Boolean.FALSE;
    if (sourceDataTagValue != null) {
      if (!filterout(dataTag, sourceDataTagValue)) {
        if (sourceDataTagValue.getValue() == null) {

          if (sourceDataTagValue.isValid()) {
            if (log.isDebugEnabled()) {
              log.debug("Null value received from source for datatag " + sourceDataTagValue.getId() + " - invalidating with quality UNKNOWN_REASON");
            }
            invalidateQuietly(dataTag, TagQualityStatus.UNKNOWN_REASON,
                                "Null value received from DAQ",
                                new Timestamp(System.currentTimeMillis()));
          } else {

            DataTagQuality newTagQuality = qualityConverter.convert(sourceDataTagValue.getQuality());
            dataTagCacheObjectFacade.setQuality(dataTag, newTagQuality, new Timestamp(System.currentTimeMillis()));
          }

        } else {

          if (sourceDataTagValue.isValid()) {

            updateAndValidateQuietly(dataTag, sourceDataTagValue.getValue(), sourceDataTagValue.getValueDescription(),
                sourceDataTagValue.getTimestamp(), sourceDataTagValue.getDaqTimestamp(), new Timestamp(System.currentTimeMillis()));

          } else {

            DataTagQuality newTagQuality = qualityConverter.convert(sourceDataTagValue.getQuality()); //TODO redesign so no object creation here
            dataTagCacheObjectFacade.updateAndInvalidate(dataTag, sourceDataTagValue.getValue(), sourceDataTagValue.getValueDescription(),
                sourceDataTagValue.getTimestamp(), sourceDataTagValue.getDaqTimestamp(), new Timestamp(System.currentTimeMillis()), newTagQuality);
          }

        }
        updated = true;
      } else {
        if (log.isTraceEnabled()) {
          log.trace("Filtering out source update for tag " + dataTag.getId());
        }
      }
    } else {
      log.error("Attempting to update a dataTag with a null source value - ignoring update.");
    }

    eventTime = dataTag.getCacheTimestamp().getTime();
    return new Event<Boolean>(eventTime, updated);
  }

  /**
   * Updates the DataTag in the cache from the passed SourceDataTagValue.
   * The method notifies any cache listeners if an update is made.
   *
   * <p>The cache timestamp is set to the current time. The DAQ and source
   * timestamps are set to the values received in the SourceDataTagValue.
   *
   * @param dataTagId id of DataTag
   * @param sourceDataTagValue the value received from the data acquisition layer
   * @return true if the tag was indeed updated (that is, the cache was modified
   * , i.e. the update was not filtered out for some reason), together with the
   * cache timestamp of this update
   * @throws CacheElementNotFoundException if the Tag cannot be found in the cache
   */
  public final Event<Boolean> updateFromSource(final Long dataTagId, final SourceDataTagValue sourceDataTagValue) {
    tagCache.acquireWriteLockOnKey(dataTagId);
    try {
      T dataTag = tagCache.getCopy(dataTagId);

      // Before updating the new value to the cache convert the value to the correct type.
      // In the process of the deserialization the dataType can still divert from the defined dataType.
      // If the dataType is an arbitrary object do nothing because the server don't work with this kind of values at all.
      if(sourceDataTagValue != null
          && sourceDataTagValue.getValue() != null
          && isKnownClass(dataTag.getDataType())){
        Object convertedValue = TypeConverter.cast(sourceDataTagValue.getValue(), dataTag.getDataType());
        sourceDataTagValue.setValue(convertedValue);
      }

      Event<Boolean> returnEvent = updateFromSource(dataTag, sourceDataTagValue);

      if (returnEvent.getReturnValue()) {
        dataTag = Evaluator.evaluate(dataTag);
        tagCache.put(dataTagId, dataTag);
      }
      return returnEvent;
    } finally {
      tagCache.releaseWriteLockOnKey(dataTagId);
    }
  }

  /**
   * To be called internally only within a dataTag synchronized block. Should not be made public.
   */
  private void updateAndValidate(final T dataTag, final Object value, final String valueDescription, final Timestamp timestamp) {
    if (!filteroutValid(dataTag, value, valueDescription, timestamp)) {
      updateAndValidateQuietly(dataTag, value, valueDescription, null, null, timestamp);
    } else {
      if (log.isTraceEnabled()) {
        log.trace("Filtering out repeated update for datatag " + dataTag.getId());
      }
    }
  }

  /**
   * To be called internally only within a dataTag synchronized block. Does not notify
   * listeners. Should not be made public.
   */
  private void updateAndValidateQuietly(final T dataTag,
                                        final Object value,
                                        final String valueDescription,
                                        final Timestamp sourceTimestamp,
                                        final Timestamp daqTimestamp,
                                        final Timestamp cacheTimestamp) {
    dataTagCacheObjectFacade.validate(dataTag);
    dataTagCacheObjectFacade.update(dataTag, value, valueDescription, sourceTimestamp, daqTimestamp, cacheTimestamp);
  }


  /**
   * Same as other updateAndValidate method but takes a tag id as parameter and
   * does the cache lookup in the method.
   *
   * <p>The cache timestamp is set to the current time and the DAQ and source
   * timestamps are reset to null.
   *
   * <p>If the update causes no changes, the cache object is not updated
   * (see filterout method in AbstracTagFacade).
   *
   * <p>Notifies registered listeners if an update takes place.
   *
   * @param dataTagId the id of the tag to update
   * @param value the new tag value
   * @param valueDescription the description of the new value (if any)
   * @param timestamp the time of the update
   */
  public final void updateAndValidate(final Long dataTagId, final Object value, final String valueDescription, final Timestamp timestamp) {
    if (tagCache.hasKey(dataTagId)) {
      tagCache.acquireWriteLockOnKey(dataTagId);
      try {
        T dataTag = tagCache.get(dataTagId);
        updateAndValidate(dataTag, value, valueDescription, timestamp);
        tagCache.put(dataTag.getId(), dataTag);
      } catch (CacheElementNotFoundException cacheEx) {
        log.error("Unable to locate tag in cache (id " + dataTagId + ") - no update performed.", cacheEx);
      } finally {
        tagCache.releaseWriteLockOnKey(dataTagId);
      }
    }
    else {
      log.error("Unable to locate tag in conrolTag and dataTag cache (id " + dataTagId + ") - no update performed.");
    }
  }

  /**
   * Generates a {@link SourceDataTag} object from the given data tag
   * @param dataTag The data tag which shall be converted
   * @return The resulting source data tag
   */
  public final SourceDataTag generateSourceDataTag(final T dataTag) {
    SourceDataTag sourceDataTag = new SourceDataTag(dataTag.getId(), dataTag.getName(), (dataTag instanceof ControlTag) ? true : false);
    sourceDataTag.setDataType(dataTag.getDataType());
    sourceDataTag.setMode(dataTag.getMode());
    sourceDataTag.setMinValue((Number) dataTag.getMinValue());
    sourceDataTag.setMaxValue((Number) dataTag.getMaxValue());
    if (dataTag.getAddress() != null) {
      sourceDataTag.setAddress(dataTag.getAddress());
    }
    return sourceDataTag;
  }

}
