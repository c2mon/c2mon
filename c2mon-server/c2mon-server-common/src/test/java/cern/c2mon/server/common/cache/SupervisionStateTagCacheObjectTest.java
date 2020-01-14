package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.status.SupervisionStateTag;

import java.sql.Timestamp;

public class SupervisionStateTagCacheObjectTest extends CacheableTest<SupervisionStateTag> {

  private static SupervisionStateTag sample = new SupervisionStateTag(0L, null, "EQ", null, null);

  public SupervisionStateTagCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(SupervisionStateTag cloneObject) {
    cloneObject.setSupervision(cloneObject.getSupervisionStatus(), cloneObject.getStatusDescription(), new Timestamp(12));
  }
}
