package cern.c2mon.server.jcacheref;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import sun.security.krb5.Config;

import cern.c2mon.server.jcacheref.prototype.C2monCacheModule;

/**
 * @author Szymon Halastra
 */
//
@ContextConfiguration(classes = C2monCacheModule.class,
        loader = AnnotationConfigContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class IgniteBaseTestingSetup {

  @BeforeClass
  public static void init() {
    initializeLogger();
    Ignition.start();

  }

  @AfterClass
  public static void clean() {
    Ignition.stopAll(true);
  }

  public abstract void setup();

  private static void initializeLogger() {
    IgniteLogger logger = new Slf4jLogger();

    IgniteConfiguration configuration = new IgniteConfiguration();
    configuration.setGridLogger(logger);
  }
}
