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

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.expression.Evaluator;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Properties;

import static cern.c2mon.shared.common.type.TypeConverter.getType;

/**
 * Manages the update logic to DataTag cache objects (including ControlTags, which currently
 * extend DataTags - could be separated in the future if this changes).
 *
 * <p>IMPORTANT: this bean is the one responsible for notifying listeners if a DataTag has
 * been updated; be careful not to notify listeners twice if calling other methods within
 * this class
 *
 * <p>NOTE: to notify listeners of updates use the provided private method
 * (notifyListenersOfUpdate(DataTag)) to determine which cache needs notifying.
 *
 * <p>ALSO: all methods that modify the DataTag cache should log this to the
 * DataTagCacheUpdate.log file.
 *
 * @author Mark Brightwell
 */
@Slf4j
@Service
public class DataTagFacadeImpl extends AbstractDataTagFacade<DataTag> implements DataTagFacade {

  /**
   * Logger for logging updates to tags.
   */
  private static final Logger TAGLOG = LoggerFactory.getLogger("DataTagLogger");

  /**
   * Interface to cache module.
   */
  private final DataTagCacheObjectFacade dataTagCacheObjectFacade;

  /**
   * Constructor.
   * @param dataTagCacheObjectFacade the object that acts directly on the cache object
   * @param dataTagCache the cache containing the DataTags
   * @param qualityConverter the bean managing how the quality changes on incoming values
   * @param alarmFacade the alarm facade bean
   * @param alarmCache the alarm cache bean
   * @param equipmentFacade Interface of the bean used to interact with the EquipmentCacheObject
   * @param subEquipmentFacade Interface of the bean used to interact with the SubEquipmentCacheObject
   */
  @Autowired
  public DataTagFacadeImpl(final DataTagCacheObjectFacade dataTagCacheObjectFacade,
                           final DataTagCache dataTagCache,
                           final QualityConverter qualityConverter,
                           final AlarmFacade alarmFacade,
                           final AlarmCache alarmCache,
                           final EquipmentFacade equipmentFacade,
                           final SubEquipmentFacade subEquipmentFacade,
                           final Evaluator evaluator) {
    super(dataTagCache, alarmFacade, alarmCache, dataTagCacheObjectFacade, dataTagCacheObjectFacade, qualityConverter, evaluator);
    super.setEquipmentFacade(equipmentFacade);
    super.setSubEquipmentFacade(subEquipmentFacade);
    this.dataTagCacheObjectFacade = dataTagCacheObjectFacade;
  }

  /**
   * To be called internally only within a dataTag synchronized block (if object in cache).
   * Does not notify listeners. Only cache timestamp is set (others are null). Should not be made public.
   *
   * @param dataTag
   * @param dataTagQuality
   * @param timestamp the cache timestamp to set (others left unchanged)
   */
  @Override
  protected final void invalidateQuietly(final DataTag tag, final TagQualityStatus statusToAdd, final String description, final Timestamp timestamp) {
    dataTagCacheObjectFacade.addQualityFlag(tag, statusToAdd, description);
    ((DataTagCacheObject) tag).setCacheTimestamp(timestamp);
  }

  /**
   * Generates the DAQ configuration XML structure for a single data tag (not control tag!)
   * @param id the id of the data tag
   * @return returns the configuration XML as a String;
   * if the tag could not be located in the cache, logs an error and returns the empty String
   */
  @Override
  public String getConfigXML(final Long id) {
    String returnValue = "";
    tagCache.acquireReadLockOnKey(id);
    try {
      DataTag dataTag = tagCache.get(id);
      returnValue = generateSourceXML((DataTagCacheObject) dataTag);//old version: SourceDataTag.toConfigXML(tag);
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("getConfigXML(): failed to retrieve data tag with id " + id + " from the cache (returning empty String config).", cacheEx);
    } finally {
      tagCache.releaseReadLockOnKey(id);
    }
    return returnValue;
  }

  /**
   * Public method returning the configuration XML string for a given {@link DataTagCacheObject}
   * (was previously static in SourceDataTag class). Currently used for DAQ start up: TODO switch to generateSourceDataTag method.
   * @param dataTagCacheObject the cache object
   * @return the XML string
   */
  @Override
  public final String generateSourceXML(final DataTag dataTag) {
    tagCache.acquireReadLockOnKey(dataTag.getId());
    try {
      DataTagCacheObject dataTagCacheObject = (DataTagCacheObject) dataTag;
      StringBuffer str = new StringBuffer("    <DataTag id=\"");

      str.append(dataTagCacheObject.getId());
      str.append("\" name=\"");
      str.append(dataTagCacheObject.getName());
      if (dataTagCacheObject instanceof ControlTag) {
        str.append("\" control=\"true\">\n");
      } else {
        str.append("\" control=\"false\">\n");
      }

      // <mode> ... </mode>
      if (!dataTagCacheObject.isInOperation()) {
        str.append("      <mode>");
        str.append(dataTagCacheObject.getMode());
        str.append("</mode>\n");
      }

      // <data-type> ... </data-type>
      str.append("      <data-type>");
      str.append(dataTagCacheObject.getDataType());
      str.append("</data-type>\n");

      if (dataTagCacheObject.getMinValue() != null) {
        str.append("        <min-value data-type=\"");
        str.append(dataTagCacheObject.getMinValue().getClass().getName().substring(10));
        str.append("\">");
        str.append(dataTagCacheObject.getMinValue());
        str.append("</min-value>\n");
      }

      if (dataTagCacheObject.getMaxValue() != null) {
        str.append("        <max-value data-type=\"");
        str.append(dataTagCacheObject.getMaxValue().getClass().getName().substring(10));
        str.append("\">");
        str.append(dataTagCacheObject.getMaxValue());
        str.append("</max-value>\n");
      }


      // <HardwareAddress> ... </HardwareAddress>
      if (dataTagCacheObject.getAddress() != null) {
        str.append(dataTagCacheObject.getAddress().toConfigXML());
      }

      str.append("    </DataTag>\n");
      return str.toString();
    } finally {
      tagCache.releaseReadLockOnKey(dataTag.getId());
  }

  }

  /**
   * Log the cache object to the log file.
   * @param dataTagCacheObject the cache object
   */
  @Override
  public void log(final DataTagCacheObject dataTagCacheObject) {
    tagCache.acquireReadLockOnKey(dataTagCacheObject.getId());
    try {
      TAGLOG.info(dataTagCacheObject.toString());
    } finally {
      tagCache.releaseReadLockOnKey(dataTagCacheObject.getId());
  }
  }

  @Override
  public boolean isUnconfiguredTag(DataTag dataTag) {
    tagCache.acquireReadLockOnKey(dataTag.getId());
    try {
      return (dataTag.isInUnconfigured());
    } finally {
      tagCache.releaseReadLockOnKey(dataTag.getId());
    }
  }

  @Override
  public DataTag createCacheObject(Long id, Properties properties) throws IllegalAccessException {
    DataTag dataTag = new DataTagCacheObject(id);
    configureCacheObject(dataTag, properties);

    validateConfig(dataTag);
    return dataTag;
  }

  /**
   * Checks that a DataTagCacheObject has a valid configuration. Is
   * used after creating or reconfiguring a tag.
   *
   * <p>IMPORTANT: Call within synch block necessary (instumentalize in TC also necessary!)
   * so should not usually be used outside server-core
   *
   * @throws ConfigurationException
   */
  @Override
  public void validateConfig(final DataTag dataTag) {
    DataTag dataTagCacheObject = (DataTagCacheObject) dataTag;
    validateTagConfig(dataTagCacheObject);
    //DataTag must have equipment or subequipment id set
    if (dataTagCacheObject.getEquipmentId() == null && dataTagCacheObject.getSubEquipmentId() == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Equipment/SubEquipment id not set for DataTag with id " + dataTag.getId() + " - unable to configure it.");
    }
    // Make sure that the minValue is of the right class if not null
    if (dataTagCacheObject.getMinValue() != null) {
      try {
        Class<?> minValueClass = getType(dataTagCacheObject.getDataType());
        if (!minValueClass.isInstance(dataTagCacheObject.getMinValue())) {
          throw new ConfigurationException(
              ConfigurationException.INVALID_PARAMETER_VALUE,
              "Parameter \"minValue\" must be of type " + dataTagCacheObject.getDataType() + " or null");
        }
      }
      catch (Exception e) {
        throw new ConfigurationException(
            ConfigurationException.INVALID_PARAMETER_VALUE,
            "Error validating parameter \"minValue\": " + e.getMessage());
      }
    }
    // Make sure that the maxValue is of the right class if not null
    if (dataTagCacheObject.getMaxValue() != null) {
      try {
        Class<?> maxValueClass = getType(dataTagCacheObject.getDataType());
        if (!maxValueClass.isInstance(dataTagCacheObject.getMaxValue())) {
          throw new ConfigurationException(
              ConfigurationException.INVALID_PARAMETER_VALUE,
              "Parameter \"maxValue\" must be of type " + dataTagCacheObject.getDataType() + " or null.");
        }
      }
      catch (Exception e) {
        throw new ConfigurationException(
            ConfigurationException.INVALID_PARAMETER_VALUE,
            "Error validating parameter \"maxValue\": " + e.getMessage());
      }
    }
    if (dataTagCacheObject.getAddress() != null) {
      dataTagCacheObject.getAddress().validate();
    } else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "No address provided for DataTag - unable to configure it.");
    }
  }

  @Override
  public void addDependentRule(final Long dataTagId, final Long ruleTagId) {
    tagCache.acquireWriteLockOnKey(dataTagId);
    try {
      DataTag dataTag = tagCache.get(dataTagId);
      addDependentRuleToTag(dataTag, ruleTagId);
      tagCache.put(dataTagId, dataTag);
    } finally {
      tagCache.releaseWriteLockOnKey(dataTagId);
    }
  }
}
