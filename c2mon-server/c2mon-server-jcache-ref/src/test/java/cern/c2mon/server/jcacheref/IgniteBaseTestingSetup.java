package cern.c2mon.server.jcacheref;

import org.apache.ignite.Ignition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

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

  }
}
