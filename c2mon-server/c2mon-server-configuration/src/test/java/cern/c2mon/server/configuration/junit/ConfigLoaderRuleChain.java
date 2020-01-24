package cern.c2mon.server.configuration.junit;

import cern.c2mon.server.test.CachePopulationRule;
import cern.c2mon.server.test.junit.AbstractRuleChainCreator;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ConfigLoaderRuleChain extends AbstractRuleChainCreator {

  @Inject
  public ConfigLoaderRuleChain(CloseRuleTagCacheListeners closeRuleTagCacheListeners,
                               CachePopulationRule loadDbAndCache,
                               ConfigurationDatabasePopulationRule loadConfigCache,
                               StartTestProcessRule startTestProcessRule) {
    super(closeRuleTagCacheListeners, loadDbAndCache, loadConfigCache, startTestProcessRule);
  }
}
