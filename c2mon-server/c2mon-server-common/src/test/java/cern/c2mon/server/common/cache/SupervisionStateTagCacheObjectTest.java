package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.status.SupervisionStateTag;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

import java.sql.Timestamp;

public class SupervisionStateTagCacheObjectTest extends CacheableTest<SupervisionStateTag> {

  private static SupervisionStateTag sample = new SupervisionStateTag(1240L, null, "EQ", null, null);

  public SupervisionStateTagCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(SupervisionStateTag cloneObject) {
    cloneObject.setSupervision(SupervisionConstants.SupervisionStatus.RUNNING, "Whatever", new Timestamp(System.currentTimeMillis()));
  }
}
