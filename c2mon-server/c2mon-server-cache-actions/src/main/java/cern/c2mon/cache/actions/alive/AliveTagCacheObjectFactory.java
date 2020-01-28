package cern.c2mon.cache.actions.alive;

import cern.c2mon.cache.actions.ControlTagCacheObjectFactory;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;

import javax.inject.Named;
import java.util.Properties;

import static cern.c2mon.server.common.util.KotlinAPIs.let;

@Named
public class AliveTagCacheObjectFactory extends ControlTagCacheObjectFactory<AliveTag> {

  @Override
  public Change configureCacheObject(AliveTag cacheable, Properties properties) {
    AliveTag aliveTag = let(new PropertiesAccessor(properties), accessor -> new AliveTag(
      accessor.getLong("id").getNullableValue(),
      accessor.getLong("supervisedId").getNullableValue(),
      accessor.getString("supervisedName").getNullableValue(),
      accessor.getString("aliveType").getNullableValue(), // TODO (Alex) Update this to whatever the clients use
      accessor.getLong("commFaultTagId").getNullableValue(),
      accessor.getLong("stateTagId").getNullableValue(),
      accessor.getInteger("aliveInterval").getNullableValue()
    ));

    // TODO (Alex) Also check for address changes?

    return super.configureCacheObject(aliveTag, properties);
  }

  @Override
  public void validateConfig(AliveTag aliveTag) throws ConfigurationException {
    super.validateConfig(aliveTag);
    new MicroValidator<>(aliveTag)
      .notNull(AliveTag::getStateTagId, "stateTagId")
      .notNull(AliveTag::getAliveInterval, "aliveInterval")
      .between(AliveTag::getAliveInterval, 0 ,Integer.MAX_VALUE, "aliveInterval");
  }
}
