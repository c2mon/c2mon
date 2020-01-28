package cern.c2mon.cache.actions.state;

import cern.c2mon.cache.actions.ControlTagCacheObjectFactory;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.daq.config.Change;

import javax.inject.Named;
import java.util.Properties;

import static cern.c2mon.server.common.util.KotlinAPIs.let;

@Named
public class SupervisionStateTagCacheObjectFactory extends ControlTagCacheObjectFactory<SupervisionStateTag> {

  @Override
  public Change configureCacheObject(SupervisionStateTag cacheable, Properties properties) {
    SupervisionStateTag commFaultTag = let(new PropertiesAccessor(properties), accessor -> new SupervisionStateTag(
      accessor.getLong("id").getNullableValue(),
      accessor.getLong("supervisedId").getNullableValue(),
      accessor.getString("supervisedType").getNullableValue(), // TODO (Alex) Update this to whatever the clients use
      accessor.getLong("aliveTagId").getNullableValue(),
      accessor.getLong("commFaultTagId").getNullableValue()
    ));

    return super.configureCacheObject(commFaultTag, properties);
  }
}
