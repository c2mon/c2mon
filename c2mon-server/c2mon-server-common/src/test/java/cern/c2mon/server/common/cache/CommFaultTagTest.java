package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.commfault.CommFaultTag;

public class CommFaultTagTest extends CacheableTest<CommFaultTag> {

  private static CommFaultTag sample = new CommFaultTag(1L, 2L, "", "EQ",null, null);

  public CommFaultTagTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(CommFaultTag cloneObject) {
    cloneObject.setStateTagId(1234L);
  }
}
