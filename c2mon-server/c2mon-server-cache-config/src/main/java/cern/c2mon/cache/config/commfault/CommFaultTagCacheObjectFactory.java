package cern.c2mon.cache.config.commfault;

import cern.c2mon.cache.config.tag.ControlTagCacheObjectFactory;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import cern.c2mon.shared.daq.config.Change;

import javax.inject.Named;
import java.util.Properties;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static cern.c2mon.shared.common.supervision.SupervisionEntity.SUBEQUIPMENT;

@Named
public class CommFaultTagCacheObjectFactory extends ControlTagCacheObjectFactory<CommFaultTag> {

  @Override
  public CommFaultTag createCacheObject(Long id) {
    return new CommFaultTag(id);
  }

  @Override
  public Change configureCacheObject(CommFaultTag commFaultTag, Properties properties) {
    apply(new PropertiesAccessor(properties), accessor -> accessor
      .getLong("equipmentId").ifPresent(id -> {
      commFaultTag.setSupervisedId(id);
      commFaultTag.setSupervisedEntity(SupervisionEntity.EQUIPMENT);
    })
      .getLong("subequipmentId").ifPresent(id -> {
      commFaultTag.setSupervisedId(id);
      commFaultTag.setSupervisedEntity(SUBEQUIPMENT);
    })
      .getString("equipmentName").ifPresent(commFaultTag::setEquipmentName)
      .getLong("stateTagId").ifPresent(commFaultTag::setStateTagId)
      .getLong("aliveTagId").ifPresent(commFaultTag::setAliveTagId));

    return super.configureCacheObject(commFaultTag, properties);
  }
}
