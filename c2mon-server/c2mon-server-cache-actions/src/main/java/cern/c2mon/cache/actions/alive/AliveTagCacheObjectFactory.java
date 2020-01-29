package cern.c2mon.cache.actions.alive;

import cern.c2mon.cache.actions.ControlTagCacheObjectFactory;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import cern.c2mon.shared.daq.config.Change;

import javax.inject.Named;
import java.util.Properties;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;

@Named
public class AliveTagCacheObjectFactory extends ControlTagCacheObjectFactory<AliveTag> {

  @Override
  public AliveTag createCacheObject(Long id) {
    return new AliveTag(id);
  }

  @Override
  public Change configureCacheObject(AliveTag aliveTag, Properties properties) {
    apply(new PropertiesAccessor(properties), accessor -> {
      accessor
        .getLong("processId").ifPresent(id -> {
        aliveTag.setSupervisedId(id);
        aliveTag.setSupervisedEntity(SupervisionEntity.PROCESS);
      })
        .getLong("equipmentId").ifPresent(id -> {
        aliveTag.setSupervisedId(id);
        aliveTag.setSupervisedEntity(SupervisionEntity.EQUIPMENT);
      })
        .getLong("subequipmentId").ifPresent(id -> {
        aliveTag.setSupervisedId(id);
        aliveTag.setSupervisedEntity(SupervisionEntity.SUBEQUIPMENT);
      })
        .getInteger("aliveInterval").ifPresent(aliveTag::setAliveInterval)
        .getLong("commFaultTagId").ifPresent(aliveTag::setCommFaultTagId)
        .getLong("statusTagId").ifPresent(aliveTag::setStateTagId)
        .getString("supervisedName").ifPresent(aliveTag::setSupervisedName);
    });

    // TODO (Alex) Also check for address changes?

    return super.configureCacheObject(aliveTag, properties);
  }
}
