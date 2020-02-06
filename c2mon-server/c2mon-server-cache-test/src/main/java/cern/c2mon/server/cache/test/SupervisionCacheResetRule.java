package cern.c2mon.server.cache.test;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.collections.ControlCacheCollection;
import cern.c2mon.server.test.DatabasePopulationRule;

import javax.cache.Cache;
import javax.inject.Inject;
import javax.inject.Named;
import java.sql.SQLException;

@Named
public class SupervisionCacheResetRule extends DatabasePopulationRule {

  private ControlCacheCollection controlCacheCollection;

  @Inject
  public SupervisionCacheResetRule(ControlCacheCollection controlCacheCollection) {
    this.controlCacheCollection = controlCacheCollection;
  }

  @Override
  protected void before() throws SQLException {
    super.before();
    controlCacheCollection.getCaches().forEach(Cache::clear);
    controlCacheCollection.getCaches().forEach(C2monCache::init);
  }
}
