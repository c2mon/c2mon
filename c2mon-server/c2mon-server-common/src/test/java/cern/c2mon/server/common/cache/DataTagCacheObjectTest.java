package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.datatag.DataTagCacheObject;

public class DataTagCacheObjectTest extends CacheObjectTest<DataTagCacheObject> {

  private static DataTagCacheObject sample = new DataTagCacheObject(1L);

  public DataTagCacheObjectTest() {
    super(sample);
  }
}
