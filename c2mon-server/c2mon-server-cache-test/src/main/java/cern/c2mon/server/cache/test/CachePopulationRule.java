package cern.c2mon.server.cache.test;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.collections.AllCacheCollection;
import cern.c2mon.server.test.DatabasePopulationRule;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.SQLException;

/**
 * Using this rule in a JUnit test will ensure that all caches are preloaded
 * cleanly with test data at the start of each test.
 *
 * @author Alexandros Papageorgiou, Justin Lewis Salmon
 */
@Named
public class CachePopulationRule extends DatabasePopulationRule {

  private AllCacheCollection allCacheCollection;

  @Inject
  public CachePopulationRule(AllCacheCollection allCacheCollection) {
    this.allCacheCollection = allCacheCollection;
  }


  @Override
  protected void before() throws SQLException {
    super.before();
    allCacheCollection.getCaches().forEach(C2monCache::init);
  }

  @Override
  protected void after() {
    super.after();
    allCacheCollection.getCaches().forEach(C2monCache::clear);
  }
}
