package cern.c2mon.cache.datatag;

import java.util.List;
import java.util.Map;

import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.common.datatag.DataTag;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class DataTagCacheLoaderTest extends AbstractCacheLoaderTest<DataTag> {

  @Autowired
  private C2monCache<DataTag> dataTagCacheRef;

  @Autowired
  private DataTagMapper dataTagMapper;

  @Override
  protected LoaderMapper<DataTag> getMapper() {
    return dataTagMapper;
  }

  @Override
  protected void compareLists(List<DataTag> mapperList, Map<Long, DataTag> cacheList) {
    for (DataTag currentTag : mapperList) {
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached DataTag should have the same name as in DB",
        currentTag.getName(), (cacheList.get(currentTag.getId())).getName());
    }
  }

  @Override
  protected DataTag getSample() {
    return new DataTagCacheObject();
  }

  @Override
  protected Long getExistingKey() {
    return 200000L;
  }

  @Override
  protected C2monCache<DataTag> getCache() {
    return dataTagCacheRef;
  }
}
