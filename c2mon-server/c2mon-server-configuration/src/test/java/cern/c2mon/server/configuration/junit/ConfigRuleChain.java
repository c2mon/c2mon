package cern.c2mon.server.configuration.junit;

import cern.c2mon.server.test.CachePopulationRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ConfigRuleChain implements TestRule {

  private final RuleChain ruleChain;

  @Inject
  public ConfigRuleChain(CloseRuleTagCacheListeners closeRuleTagCacheListeners,
                         CachePopulationRule loadDbAndCache,
                         ConfigurationDatabasePopulationRule loadConfigCache) {
    ruleChain = RuleChain
      .outerRule(closeRuleTagCacheListeners)
      .around(loadDbAndCache)
      .around(loadConfigCache);
  }

  @Override
  public Statement apply(Statement statement, Description description) {
    return ruleChain.apply(statement, description);
  }

}
