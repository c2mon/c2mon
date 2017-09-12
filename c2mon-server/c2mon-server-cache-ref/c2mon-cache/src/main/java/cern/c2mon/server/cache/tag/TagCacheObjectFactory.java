package cern.c2mon.server.cache.tag;

import java.util.Properties;

import cern.c2mon.cache.api.factory.CacheObjectFactory;
import cern.c2mon.server.common.TempTag;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * Creates {@link TempTag} cache object
 * {@link TempTag} contains common interface for DataTag and ControlTag
 * apparently they are differ only by class name, methods definitions are the same
 *
 * @author Szymon Halastra
 */
public class TagCacheObjectFactory extends CacheObjectFactory<TempTag> {

  @Override
  public TempTag createCacheObject(Long id) {
    return null;
  }

  @Override
  public Change configureCacheObject(TempTag cacheable, Properties properties) {
    return null;
  }

  @Override
  public void validateConfig(TempTag cacheable) throws ConfigurationException {

  }
}
