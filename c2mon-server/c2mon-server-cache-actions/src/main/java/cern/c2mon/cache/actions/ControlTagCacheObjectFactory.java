package cern.c2mon.cache.actions;

import cern.c2mon.cache.actions.tag.AbstractTagCacheObjectFactory;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;

import java.util.Properties;

public abstract class ControlTagCacheObjectFactory<CONTROL extends ControlTag> extends AbstractTagCacheObjectFactory<CONTROL> {

  /**
   * Return null to allow the parent method to pass it to {@link ControlTagCacheObjectFactory#configureCacheObject(ControlTag, Properties)}
   *
   * We do this because control tag objects have custom constructors (eventually, so should the rest as well)
   * that do not work just with an id
   */
  @Override
  public final CONTROL createCacheObject(Long id) {
    return null;
  }

  /**
   * @implNote Expect that the passed object is always null, as specified above
   */
  @Override
  public Change configureCacheObject(CONTROL cacheable, Properties properties) {
    return super.configureCacheObject(cacheable, properties);
  }

  @Override
  public void validateConfig(CONTROL controlTag) throws ConfigurationException {
    super.validateConfig(controlTag);

    new MicroValidator<>(controlTag)
      .notNull(ControlTag::getSupervisedId, "supervisedId")
      .notNull(ControlTag::getSupervisedEntity, "supervisionEntity");
  }
}
