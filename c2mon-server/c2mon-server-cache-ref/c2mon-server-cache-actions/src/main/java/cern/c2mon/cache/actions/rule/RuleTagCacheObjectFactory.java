package cern.c2mon.cache.actions.rule;

import cern.c2mon.cache.actions.tag.AbstractTagCacheObjectFactory;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.daq.config.Change;

import java.util.Properties;

public class RuleTagCacheObjectFactory extends AbstractTagCacheObjectFactory<RuleTag> {

  @Override
  public RuleTag createCacheObject(Long id) {
    return new RuleTagCacheObject(id);
  }

  @Override
  public Change configureCacheObject(RuleTag cacheable, Properties properties) {
    return null;
  }
}
