package cern.c2mon.server.test.cache;

import cern.c2mon.server.common.commfault.CommFaultTag;

public class CommFaultTagCacheObjectFactory extends AbstractCacheObjectFactory<CommFaultTag> {

  @Override
  public CommFaultTag sampleBase() {
    return new CommFaultTag(
      1223L,
      150L,
      "E_TESTHANDLER_TESTHANDLER03",
      "EQ",
      1224L,
      1222L
    );
  }
}
