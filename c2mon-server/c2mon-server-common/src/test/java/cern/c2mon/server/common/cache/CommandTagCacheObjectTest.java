package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.command.CommandTagCacheObject;

public class CommandTagCacheObjectTest extends CacheableTest<CommandTagCacheObject> {

  private static CommandTagCacheObject sample = new CommandTagCacheObject(1L);

  public CommandTagCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(CommandTagCacheObject cloneObject) {
      cloneObject.setDescription("Test description");
  }
}
