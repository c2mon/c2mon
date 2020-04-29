package cern.c2mon.server.cache.test.factory;

import cern.c2mon.server.common.supervision.SupervisionStateTag;

public class SupervisionStateTagFactory extends AbstractCacheObjectFactory<SupervisionStateTag> {

  @Override
  public SupervisionStateTag sampleBase() {
    return sampleBase(1222L);
  }

  public SupervisionStateTag sampleBase(long id) {
    return new SupervisionStateTag(
      id,
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
