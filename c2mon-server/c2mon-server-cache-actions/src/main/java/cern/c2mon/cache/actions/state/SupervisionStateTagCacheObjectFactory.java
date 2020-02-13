package cern.c2mon.cache.actions.state;

import cern.c2mon.cache.actions.ControlTagCacheObjectFactory;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.daq.config.Change;

import javax.inject.Named;
import java.util.Properties;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;

@Named
public class SupervisionStateTagCacheObjectFactory extends ControlTagCacheObjectFactory<SupervisionStateTag> {

  @Override
  public SupervisionStateTag createCacheObject(Long id) {
    return new SupervisionStateTag(id);
  }

  @Override
  public Change configureCacheObject(SupervisionStateTag supervisionStateTag, Properties properties) {

    apply(new PropertiesAccessor(properties), accessor ->
      accessor.getLong("supervisedId").ifPresent(supervisionStateTag::setSupervisedId)
        .getLong("aliveTagId").ifPresent(supervisionStateTag::setAliveTagId)
        .getLong("commFaultTagId").ifPresent(supervisionStateTag::setCommFaultTagId));

    return super.configureCacheObject(supervisionStateTag, properties);
  }
}
