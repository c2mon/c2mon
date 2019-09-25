package cern.c2mon.cache.commfault;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.CommFaultTagMapper;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.commfault.CommFaultTagCacheObject;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class CommfaultCacheLoaderTest extends AbstractCacheLoaderTest<CommFaultTag> {

  @Autowired
  private C2monCache<CommFaultTag> commFaultTagCacheRef;

  @Autowired
  private CommFaultTagMapper commFaultTagMapper;

  @Override
  protected LoaderMapper<CommFaultTag> getMapper() {
    return commFaultTagMapper;
  }

  @Override
  protected void compareLists(List<CommFaultTag> mapperList, Map<Long, CommFaultTag> cacheList) {
    for (CommFaultTag aCommFaultList : mapperList) {
      CommFaultTagCacheObject currentTag = (CommFaultTagCacheObject) aCommFaultList;
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached CommfaultTag should have the same name as in DB",
        currentTag.getEquipmentId(), ((cacheList.get(currentTag.getId())).getEquipmentId()));
    }
  }

  @Override
  protected CommFaultTag getSample() {
    return new CommFaultTagCacheObject();
  }

  @Override
  protected Long getExistingKey() {
    return 60000L;
  }

  @Override
  protected C2monCache<CommFaultTag> getCache() {
    return commFaultTagCacheRef;
  }
}
