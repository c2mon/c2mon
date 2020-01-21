package cern.c2mon.server.test.factory;

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

  public AliveTag ofProcess() {
    return new AliveTag(
      1261L,
      51L,
      "P_TESTHANDLER04",
      "PROC",
      null,
      1260L,
      60000);
  }

}
