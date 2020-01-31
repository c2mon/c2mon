package cern.c2mon.server.cache.test;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.collections.ControlCacheCollection;
import org.junit.rules.ExternalResource;

import javax.cache.Cache;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class SupervisionCacheResetRule extends ExternalResource {

  private ControlCacheCollection controlCacheCollection;
  private final CachePopulationRule cachePopulationRule;

  @Inject
  public SupervisionCacheResetRule(ControlCacheCollection controlCacheCollection, CachePopulationRule cachePopulationRule) {
    this.controlCacheCollection = controlCacheCollection;
    this.cachePopulationRule = cachePopulationRule;
  }

  @Override
  protected void before() throws Throwable {
    controlCacheCollection.forEachCache(Cache::clear);
    super.before();
    controlCacheCollection.forEachCache(C2monCache::init);
  }
}
