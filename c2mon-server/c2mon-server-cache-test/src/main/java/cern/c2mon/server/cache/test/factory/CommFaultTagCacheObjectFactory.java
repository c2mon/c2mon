package cern.c2mon.server.cache.test.factory;

import cern.c2mon.server.common.commfault.CommFaultTag;

public class CommFaultTagCacheObjectFactory extends AbstractCacheObjectFactory<CommFaultTag> {

  @Override
  public CommFaultTag sampleBase() {
    return new CommFaultTag(
      1223L,
      150L,
      "E_TESTHANDLER_TESTHANDLER03",
      "EQ",
      1222L,
      1224L
    );
  }
}
