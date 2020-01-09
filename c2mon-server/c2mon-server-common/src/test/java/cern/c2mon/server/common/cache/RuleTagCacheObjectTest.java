package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.util.Java9Collections;

public class RuleTagCacheObjectTest extends CacheableTest<RuleTagCacheObject> {

  private static RuleTagCacheObject sample = new RuleTagCacheObject(1L);

  public RuleTagCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(RuleTagCacheObject cloneObject) {
    cloneObject.setSubEquipmentIds(Java9Collections.setOf(1L, 2L, 3L));
  }
}
