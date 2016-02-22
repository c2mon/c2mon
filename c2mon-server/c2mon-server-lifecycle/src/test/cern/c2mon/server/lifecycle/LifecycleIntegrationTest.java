package cern.c2mon.server.lifecycle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ServerStartup.class)
@TestPropertySource(properties = {"c2mon.home=/tmp", "c2mon.modules.location='<?xml version=\"1.0\"?>'"})
public class LifecycleIntegrationTest {

  @Test
  public void startup() {}
}
