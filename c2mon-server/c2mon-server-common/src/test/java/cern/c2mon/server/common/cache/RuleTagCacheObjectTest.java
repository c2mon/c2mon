package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.rule.RuleTagCacheObject;

public class RuleTagCacheObjectTest extends CacheObjectTest<RuleTagCacheObject> {

  private static RuleTagCacheObject sample = new RuleTagCacheObject(1L);

  public RuleTagCacheObjectTest() {
    super(sample);
  }
}
