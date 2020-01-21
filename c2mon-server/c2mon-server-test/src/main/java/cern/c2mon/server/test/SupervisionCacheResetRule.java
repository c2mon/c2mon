package cern.c2mon.server.test;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import org.junit.rules.ExternalResource;

import javax.cache.Cache;
import javax.inject.Named;

import static cern.c2mon.server.common.util.Java9Collections.listOf;

@Named
public class SupervisionCacheResetRule extends ExternalResource {

  private final CachePopulationRule cachePopulationRule;
  private C2monCache<AliveTag> aliveTagCache;
  private C2monCache<CommFaultTag> commFaultCache;
  private C2monCache<SupervisionStateTag> stateTagCache;

  public SupervisionCacheResetRule(C2monCache<AliveTag> aliveTagCache, C2monCache<CommFaultTag> commFaultCache, C2monCache<SupervisionStateTag> stateTagCache, CachePopulationRule cachePopulationRule) {
    this.aliveTagCache = aliveTagCache;
    this.commFaultCache = commFaultCache;
    this.stateTagCache = stateTagCache;
    this.cachePopulationRule = cachePopulationRule;
  }

  @Override
  protected void before() throws Throwable {
    listOf(stateTagCache, commFaultCache, aliveTagCache).forEach(Cache::clear);
    super.before();
    cachePopulationRule.before();
  }
}
