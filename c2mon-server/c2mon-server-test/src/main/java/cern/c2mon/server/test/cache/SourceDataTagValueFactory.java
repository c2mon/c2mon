package cern.c2mon.server.test.cache;

import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;

import java.sql.Timestamp;

public final class SourceDataTagValueFactory {

  private SourceDataTagValueFactory(){

  }

  public static SourceDataTagValue sampleAlive(){
    return new SourceDataTagValue(1221L,
      "test alive",
      true,
      0L,
      new SourceDataTagQuality(),
      new Timestamp(System.currentTimeMillis()),
      4,
      false,
      "description",
      10000);
  }

  public static SourceDataTagValue sampleCommFault(long updateTime){
    return new SourceDataTagValue(
      1223L,
      "test commfault",
      true,
      Boolean.TRUE,
      new SourceDataTagQuality(),
      new Timestamp(updateTime),
      4,
      false,
      "description",
      10000);
  }
}
