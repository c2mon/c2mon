package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.rule.RuleTagCacheObject;

public class RuleTagCacheObjectTest extends CacheableTest<RuleTagCacheObject> {

  private static RuleTagCacheObject sample = new RuleTagCacheObject(1L);

  public RuleTagCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(RuleTagCacheObject cloneObject) {
      cloneObject.setRuleText("Working software over comprehensive documentation");
  }
}
