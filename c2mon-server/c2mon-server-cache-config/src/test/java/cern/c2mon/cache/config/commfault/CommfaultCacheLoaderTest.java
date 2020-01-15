package cern.c2mon.cache.config.commfault;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheLoaderTest;
import cern.c2mon.server.cache.dbaccess.CommFaultTagMapper;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.common.commfault.CommFaultTag;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Alexandros Papageorgiou
 */
public class CommfaultCacheLoaderTest extends AbstractCacheLoaderTest<CommFaultTag> {

  @Inject
  private C2monCache<CommFaultTag> commFaultTagCacheRef;

  @Inject
  private CommFaultTagMapper commFaultTagMapper;

  @Override
  protected LoaderMapper<CommFaultTag> getMapper() {
    return commFaultTagMapper;
  }

  @Override
  protected void customCompare(List<CommFaultTag> mapperList, Map<Long, CommFaultTag> cacheList) {
    for (CommFaultTag aCommFaultList : mapperList) {
      CommFaultTag currentTag = (CommFaultTag) aCommFaultList;
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached CommfaultTag should have the same name as in DB",
        currentTag.getSupervisedId(), ((cacheList.get(currentTag.getId())).getSupervisedId()));
    }
  }

  @Override
  protected CommFaultTag getSample() {
    return new CommFaultTag(1L, 2L, null, "EQ",null, null);
  }

  @Override
  protected Long getExistingKey() {
    return 1223L;
  }

  @Override
  protected C2monCache<CommFaultTag> getCache() {
    return commFaultTagCacheRef;
  }
}
