package cern.c2mon.cache.config.control;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheLoaderTest;
import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Alexandros Papageorgiou
 */
public class ControlCacheLoaderTest extends AbstractCacheLoaderTest<ControlTag> {

  @Inject
  private C2monCache<ControlTag> controlTagCacheRef;

  @Inject
  private ControlTagMapper controlTagMapper;

  @Override
  protected LoaderMapper<ControlTag> getMapper() {
    return controlTagMapper;
  }

  @Override
  protected void customCompare(List<ControlTag> mapperList, Map<Long, ControlTag> cacheList) {
    for (ControlTag currentTag : mapperList) {
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached ControlTag should have the same name as in DB",
        currentTag.getName(), (cacheList.get(currentTag.getId())).getName());
    }
  }

  @Override
  protected ControlTag getSample() {
    return new ControlTagCacheObject();
  }

  @Override
  protected Long getExistingKey() {
    return 1220L;
  }

  @Override
  protected C2monCache<ControlTag> getCache() {
    return controlTagCacheRef;
  }
}
