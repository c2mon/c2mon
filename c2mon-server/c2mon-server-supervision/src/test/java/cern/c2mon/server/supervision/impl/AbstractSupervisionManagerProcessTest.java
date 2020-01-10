package cern.c2mon.server.supervision.impl;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.supervision.SupervisionManager;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.server.test.CachePopulationRule;
import cern.c2mon.shared.daq.process.ProcessRequest;
import cern.c2mon.shared.daq.process.XMLConverter;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.function.Consumer;

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
public abstract class AbstractSupervisionManagerProcessTest<REQ extends ProcessRequest, RES> {

  protected static final String GOOD_PROCESSNAME = "P_TESTHANDLER03";

  protected static final String BAD_PROCESSNAME = "P_TESTNACHO";

  @Inject
  protected SupervisionManager supervisionManager;

  @Rule
  @Inject
  public CachePopulationRule cachePopulationRule;

  protected XMLConverter xmlConverter = new XMLConverter();

  protected void doAndVerify(REQ request, Consumer<RES> tests) throws Exception {
    String xmlProcessConfigurationResponse = action(request);
    assertNotNull(xmlProcessConfigurationResponse);

    Object resultObj = xmlConverter.fromXml(xmlProcessConfigurationResponse);

    assertNotNull(resultObj);

    tests.accept((RES) resultObj);
  }

  protected abstract String action(REQ request);
}
