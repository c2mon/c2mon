package cern.c2mon.server;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerStartup.class)
@Ignore("This test sporadically hangs for some reason...")
public class ServerStartupIntegrationTest {

  @Test
  public void startup() {}
}
