package cern.c2mon.server.supervision.process;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.test.DatabasePopulationRule;
import org.junit.Rule;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.SQLException;

@Named
public class ProcessCacheResetRule extends DatabasePopulationRule {

  @Rule
  @Inject
  public DatabasePopulationRule cachePopulationRule;

  @Inject
  private C2monCache<Process> processCache;

  @Inject
  private C2monCache<SupervisionStateTag> stateTagCache;

  @Override
  protected void before() throws SQLException {
    processCache.clear();
    stateTagCache.clear();
    super.before();
    processCache.init();
    stateTagCache.init();
  }
}
