package cern.c2mon.server.cache.tag;

import java.util.Properties;

import cern.c2mon.cache.api.factory.CacheObjectFactory;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * Creates {@link DataTag} cache object
 * {@link DataTag} contains common interface for DataTag and ControlTag
 * apparently they are differ only by class name, methods definitions are the same
 *
 * @author Szymon Halastra
 */
public class TagCacheObjectFactory extends CacheObjectFactory<DataTag> {

  @Override
  public DataTag createCacheObject(Long id) {
    return null;
  }

  @Override
  public Change configureCacheObject(DataTag cacheable, Properties properties) {
    return null;
  }

  @Override
  public void validateConfig(DataTag cacheable) throws ConfigurationException {

  }
}
