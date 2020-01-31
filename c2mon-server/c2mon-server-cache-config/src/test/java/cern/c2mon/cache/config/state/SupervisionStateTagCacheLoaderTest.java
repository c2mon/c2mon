package cern.c2mon.cache.config.state;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheLoaderTest;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.cache.dbaccess.SupervisionStateTagMapper;
import cern.c2mon.server.common.supervision.SupervisionStateTag;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class SupervisionStateTagCacheLoaderTest extends AbstractCacheLoaderTest<SupervisionStateTag> {

  @Inject
  private C2monCache<SupervisionStateTag> stateTagCacheRef;
  @Inject
  private SupervisionStateTagMapper stateTagMapper;

  @Override
  protected LoaderMapper<SupervisionStateTag> getMapper() {
    return stateTagMapper;
  }

  @Override
  protected void customCompare(List<SupervisionStateTag> mapperList, Map<Long, SupervisionStateTag> cacheList) {
  }

  @Override
  protected Long getExistingKey() {
    return 1222L;
  }

  @Override
  protected SupervisionStateTag getSample() {
    return new SupervisionStateTag(1L);
  }

  @Override
  protected C2monCache<SupervisionStateTag> getCache() {
    return stateTagCacheRef;
  }
}
