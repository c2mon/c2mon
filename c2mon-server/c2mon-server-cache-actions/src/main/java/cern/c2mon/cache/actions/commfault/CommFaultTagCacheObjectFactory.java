package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.actions.ControlTagCacheObjectFactory;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;

import javax.inject.Named;
import java.util.Properties;

import static cern.c2mon.server.common.util.KotlinAPIs.let;

@Named
public class CommFaultTagCacheObjectFactory extends ControlTagCacheObjectFactory<CommFaultTag> {

  @Override
  public Change configureCacheObject(CommFaultTag cacheable, Properties properties) {
    CommFaultTag commFaultTag = let(new PropertiesAccessor(properties), accessor -> new CommFaultTag(
      accessor.getLong("id").getNullableValue(),
      accessor.getLong("equipmentId").getNullableValue(),
      accessor.getString("equipmentName").getNullableValue(),
      accessor.getString("equipmentType").getNullableValue(), // TODO (Alex) Update this to whatever the clients use
      accessor.getLong("stateTagId").getNullableValue(),
      accessor.getLong("aliveTagId").getNullableValue()
    ));

    return super.configureCacheObject(commFaultTag, properties);
  }

  @Override
  public void validateConfig(CommFaultTag commFaultTag) throws ConfigurationException {
    super.validateConfig(commFaultTag);

    new MicroValidator<>(commFaultTag)
      .notNull(CommFaultTag::getStateTagId, "stateTagId");
  }
}
