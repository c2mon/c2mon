package cern.c2mon.server.supervision.impl;

import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.supervision.SupervisionManager;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.shared.daq.process.ProcessRequest;
import cern.c2mon.shared.daq.process.XMLConverter;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertNotNull;

/**
 * @author Alexandros Papageorgiou
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  SupervisionModule.class
})
abstract class AbstractSupervisionManagerProcessTest<REQ extends ProcessRequest, RES> {

  protected static final String GOOD_PROCESSNAME = "P_TESTHANDLER03";

  protected static final String BAD_PROCESSNAME = "P_TESTNACHO";

  @Autowired
  protected static SupervisionManager supervisionManager;
  protected XMLConverter xmlConverter = new XMLConverter();
  private Function<REQ, String> action;

  protected AbstractSupervisionManagerProcessTest(Function<REQ, String> action) {
    this.action = action;
  }

  protected void doAndVerify(REQ request, Consumer<RES> tests) throws Exception {
    String xmlProcessConfigurationResponse = action.apply(request);
    assertNotNull(xmlProcessConfigurationResponse);

    Object resultObj = xmlConverter.fromXml(xmlProcessConfigurationResponse);

    assertNotNull(resultObj);

    tests.accept((RES) resultObj);
  }
}
