package cern.c2mon.cache.actions.statetag;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Slf4j
@Service
public class SupervisionStateTagCacheService extends AbstractCacheServiceImpl<SupervisionStateTag> {

  @Inject
  public SupervisionStateTagCacheService(C2monCache<SupervisionStateTag> cache) {
    super(cache, new SupervisionStateTagCacheFlow());
  }
}
