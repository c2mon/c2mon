package cern.c2mon.server.cache.test.factory;

import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;

import java.sql.Timestamp;

public final class SourceDataTagValueFactory {

  private SourceDataTagValueFactory() {
  }

  public static SourceDataTagValue sampleAlive() {
    return SourceDataTagValue
      .builder()
      .id(1221L)
      .name("test alive")
      .controlTag(true)
      .value(0L)
      .quality(new SourceDataTagQuality())
      .timestamp(new Timestamp(System.currentTimeMillis()))
      .priority(4)
      .guaranteedDelivery(false)
      .valueDescription("description")
      .timeToLive(10000)
      .build();
  }

  public static SourceDataTagValue sampleCommFault(long updateTime) {
    return SourceDataTagValue
      .builder()
      .id(1223L)
      .name("test commfault")
      .controlTag(true)
      .value(Boolean.TRUE)
      .quality(new SourceDataTagQuality())
      .timestamp(new Timestamp(updateTime))
      .priority(4)
      .guaranteedDelivery(false)
      .valueDescription("description")
      .timeToLive(10000)
      .build();
  }
}
