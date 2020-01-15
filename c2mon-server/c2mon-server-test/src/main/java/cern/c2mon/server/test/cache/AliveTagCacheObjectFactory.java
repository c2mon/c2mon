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
      100L,
      "TEST_EQ",
      "EQ",
      null,
      null,
      100
    );
  }
}
