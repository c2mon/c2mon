package cern.c2mon.server.test.cache;

import cern.c2mon.server.common.status.SupervisionStateTag;

public class SupervisionStateTagFactory extends AbstractCacheObjectFactory<SupervisionStateTag> {

  @Override
  public SupervisionStateTag sampleBase() {
    return new SupervisionStateTag(
      1240L,
      160L,
      "EQ",
      null,
      1241L
    );
  }
}
