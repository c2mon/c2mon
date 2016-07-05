package cern.c2mon.server;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ServerStartup.class)
public class ServerStartupIntegrationTest {

  @Test
  @Ignore("This test sporadically hangs for some reason...")
  public void startup() {}
}
