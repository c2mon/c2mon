package cern.c2mon.server.supervision.process;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.server.supervision.impl.event.ProcessEvents;
import cern.c2mon.server.test.CachePopulationRule;
import cern.c2mon.server.test.DatabasePopulationRule;
import cern.c2mon.shared.daq.process.ProcessRequest;
import cern.c2mon.shared.daq.process.XMLConverter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.function.Function;

import static cern.c2mon.server.common.util.Java9Collections.listOf;
import static org.junit.Assert.assertNotNull;

/**
 * @author Alexandros Papageorgiou
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheActionsModuleRef.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  CacheLoadingModuleRef.class,
  SupervisionModule.class,
  CachePopulationRule.class,
  C2monIgniteConfiguration.class
})
public abstract class AbstractProcessEventsTest<REQ extends ProcessRequest, RES> {

  protected static final String GOOD_PROCESSNAME = "P_TESTHANDLER03";

  protected static final String BAD_PROCESSNAME = "P_TESTNACHO";

  @Inject
  protected ProcessEvents processEvents;

  @Rule
  @Inject
  public DatabasePopulationRule cachePopulationRule;

  @Inject
  private C2monCache<Process> processCache;

  @Inject
  private C2monCache<SupervisionStateTag> stateTagCache;

  @Before
  public void resetCaches() {
    listOf(stateTagCache, processCache).forEach(cache -> {
      cache.clear();
      cache.init();
    });
  }

  protected XMLConverter xmlConverter = new XMLConverter();

  protected void doAndVerify(REQ request, Function<REQ,String> action, Consumer<RES> tests) throws Exception {
    String xmlProcessConfigurationResponse = action.apply(request);
    assertNotNull(xmlProcessConfigurationResponse);

    Object resultObj = xmlConverter.fromXml(xmlProcessConfigurationResponse);

    assertNotNull(resultObj);

    tests.accept((RES) resultObj);
  }
}
