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

  @Inject
  public SupervisionCacheResetRule(ControlCacheCollection controlCacheCollection) {
    this.controlCacheCollection = controlCacheCollection;
  }

  @Override
  protected void before() throws Throwable {
    controlCacheCollection.getCaches().forEach(Cache::clear);
    super.before();
    controlCacheCollection.getCaches().forEach(C2monCache::init);
  }
}
