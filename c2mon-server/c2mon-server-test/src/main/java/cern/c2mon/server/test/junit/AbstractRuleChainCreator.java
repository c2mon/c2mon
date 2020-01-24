package cern.c2mon.server.test.junit;

import cern.c2mon.server.common.util.KotlinAPIs;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class AbstractRuleChainCreator implements TestRule {

  private final RuleChain ruleChain;

  protected AbstractRuleChainCreator(TestRule outerMost, TestRule... testRules) {
    ruleChain = KotlinAPIs.let(RuleChain.outerRule(outerMost), internalRuleChain -> {
      for (TestRule testRule : testRules) {
        internalRuleChain = internalRuleChain.around(testRule);
      }
      return internalRuleChain;
    });
  }

  @Override
  public Statement apply(Statement statement, Description description) {
    return ruleChain.apply(statement, description);
  }
}
