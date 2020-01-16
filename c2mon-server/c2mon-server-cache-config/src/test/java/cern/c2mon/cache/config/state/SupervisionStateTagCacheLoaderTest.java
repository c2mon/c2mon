package cern.c2mon.cache.config.state;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheLoaderTest;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.cache.dbaccess.SupervisionStateTagMapper;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.test.cache.SupervisionStateTagFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class SupervisionStateTagCacheLoaderTest extends AbstractCacheLoaderTest<SupervisionStateTag> {

  private final SupervisionStateTagFactory stateTagFactory = new SupervisionStateTagFactory();
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
    return stateTagFactory.sampleBase();
  }

  @Override
  protected C2monCache<SupervisionStateTag> getCache() {
    return stateTagCacheRef;
  }
}
