package cern.c2mon.cache.actions.datatag;

import cern.c2mon.cache.actions.BaseEquipmentServiceImpl;
import cern.c2mon.cache.actions.tag.TagCacheObjectFactory;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.DataTagUpdate;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Properties;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Slf4j
public class DataTagCacheObjectFactory extends TagCacheObjectFactory<DataTag> {

  @Inject
  public DataTagCacheObjectFactory(BaseEquipmentServiceImpl coreAbstractEquipmentService) {
    super(coreAbstractEquipmentService);
  }

  @Override
  public DataTag createCacheObject(Long id) {
    return new DataTagCacheObject(id);
  }

  @Override
  public void validateConfig(DataTag dataTag) throws ConfigurationException {
    validateTagConfig(dataTag);

    new MicroValidator<>(dataTag)
      //DataTag must have equipment or subequipment id set
      .not(dataTagObj -> dataTagObj.getEquipmentId() == null && dataTagObj.getSubEquipmentId() == null,
        "Equipment/SubEquipment id not set for DataTag with id " + dataTag.getId() + " - unable to configure it.")
      .optType(DataTag::getMinValue, dataTag.getDataType(), "\"minValue\"")
      .optType(DataTag::getMaxValue, dataTag.getDataType(), "\"maxValue\"")
      .notNull(DataTag::getAddress, "address");

    dataTag.getAddress().validate();
  }


  @Override
  public Change configureCacheObject(DataTag tag, Properties properties) throws IllegalAccessException {
    DataTagCacheObject dataTagCacheObject = (DataTagCacheObject) tag;
    DataTagUpdate dataTagUpdate = setCommonProperties(dataTagCacheObject, properties);

    super.configureCacheObject(tag, properties);

    return dataTagUpdate;
  }
}
