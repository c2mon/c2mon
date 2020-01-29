package cern.c2mon.cache.actions;

import cern.c2mon.cache.actions.tag.AbstractTagCacheObjectFactory;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.shared.daq.config.Change;

import java.util.Properties;

public abstract class ControlTagCacheObjectFactory<CONTROL extends ControlTag> extends AbstractTagCacheObjectFactory<CONTROL> {

  /**
   * @implNote Expect that the passed object is always null, as specified above
   */
  @Override
  public Change configureCacheObject(CONTROL cacheable, Properties properties) {
    return super.configureCacheObject(cacheable, properties);
  }
}
