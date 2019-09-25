package cern.c2mon.cache.control;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.common.control.ControlTag;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class ControlCacheLoaderTest extends AbstractCacheLoaderTest<ControlTag> {

  @Autowired
  private C2monCache<ControlTag> controlTagCacheRef;

  @Autowired
  private ControlTagMapper controlTagMapper;

  @Override
  protected LoaderMapper<ControlTag> getMapper() {
    return controlTagMapper;
  }

  @Override
  protected void compareLists(List<ControlTag> mapperList, Map<Long, ControlTag> cacheList) {
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
    return 0L;
  }

  @Override
  protected C2monCache<ControlTag> getCache() {
    return controlTagCacheRef;
  }
}
