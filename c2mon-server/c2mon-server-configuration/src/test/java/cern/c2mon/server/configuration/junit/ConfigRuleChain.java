package cern.c2mon.server.configuration.junit;

import cern.c2mon.server.test.CachePopulationRule;
import cern.c2mon.server.test.junit.AbstractRuleChainCreator;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ConfigRuleChain extends AbstractRuleChainCreator {

  @Inject
  public ConfigRuleChain(CloseRuleTagCacheListeners closeRuleTagCacheListeners,
                         CachePopulationRule loadDbAndCache,
                         ConfigurationDatabasePopulationRule loadConfigCache) {
    super(closeRuleTagCacheListeners, loadDbAndCache, loadConfigCache);
  }

}
