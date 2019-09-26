package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.process.ProcessCacheObject;

public class ProcessCacheObjectTest extends CacheObjectTest<ProcessCacheObject> {

  private static ProcessCacheObject sample = new ProcessCacheObject(1L);

  public ProcessCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(ProcessCacheObject cloneObject) {
      cloneObject.setDescription("People over processes");
  }
}
