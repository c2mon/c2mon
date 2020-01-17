package cern.c2mon.server.test.cache;

import cern.c2mon.server.common.alive.AliveTag;

public class AliveTagCacheObjectFactory extends AbstractCacheObjectFactory<AliveTag> {

  @Override
  public AliveTag sampleBase() {
    return withCustomId(1L);
  }

  public AliveTag withCustomId(long id) {
    return new AliveTag(
      id,
      1224L,
      "E_TESTHANDLER_TESTHANDLER03",
      "EQ",
      1223L,
      1222L,
      60000
    );
  }
}
