package cern.c2mon.cache.actions.datatag;

import cern.c2mon.cache.actions.BaseEquipmentServiceImpl;
import cern.c2mon.cache.actions.tag.TagCacheObjectFactory;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.DataTagUpdate;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Properties;

import static cern.c2mon.shared.common.type.TypeConverter.getType;

/**
 * @author Szymon Halastra
 */
@Slf4j
public class DataTagCacheObjectFactory extends TagCacheObjectFactory<DataTag> {

  @Inject
  public DataTagCacheObjectFactory(C2monCache<DataTag> dataTagCacheRef,
                                   BaseEquipmentServiceImpl coreAbstractEquipmentService) {
    super(dataTagCacheRef, coreAbstractEquipmentService);
  }

  @Override
  public DataTag createCacheObject(Long id) {
    DataTagCacheObject dataTagCacheObject = new DataTagCacheObject(id);
    return dataTagCacheObject;
  }

  @Override
  public void validateConfig(DataTag dataTag) throws ConfigurationException {
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
    }
    else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "No address provided for DataTag - unable to configure it.");
    }
  }


  @Override
  public Change configureCacheObject(DataTag tag, Properties properties) throws IllegalAccessException {
    DataTagCacheObject dataTagCacheObject = (DataTagCacheObject) tag;
    DataTagUpdate dataTagUpdate = setCommonProperties(dataTagCacheObject, properties);

    super.configureCacheObject(tag, properties);

    if (tag.getEquipmentId() != null)
      dataTagUpdate.setEquipmentId(tag.getEquipmentId());

    return dataTagUpdate;
  }
}
