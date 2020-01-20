package cern.c2mon.server.test.cache;

import cern.c2mon.server.common.supervision.SupervisionStateTag;

public class SupervisionStateTagFactory extends AbstractCacheObjectFactory<SupervisionStateTag> {

  @Override
  public SupervisionStateTag sampleBase() {
    return new SupervisionStateTag(
      1222L,
      150L,
      "EQ",
      1224L,
      1223L
    );
  }

  public SupervisionStateTag ofProcess() {
    return new SupervisionStateTag(
      1260L,
      51L,
      "PROC",
      1261L,
      null
    );
  }
}
