package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.status.SupervisionStateTag;

import java.sql.Timestamp;

public class SupervisionStateTagCacheObjectTest extends CacheableTest<SupervisionStateTag> {

  private static SupervisionStateTag sample = new SupervisionStateTag(1240, 160, "EQ", null, null);

  public SupervisionStateTagCacheObjectTest() {
    super(sample);
  }

  @Override
  public void hasDefaultEmptyCtor() {
    // Do nothing,
  }

  @Override
  protected void mutateObject(SupervisionStateTag cloneObject) {
    cloneObject.setSupervision(cloneObject.getSupervisionStatus(), cloneObject.getStatusDescription(), new Timestamp(12));
  }
}
