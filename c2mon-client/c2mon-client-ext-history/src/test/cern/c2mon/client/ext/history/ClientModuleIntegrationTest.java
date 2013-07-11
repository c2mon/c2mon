package cern.c2mon.client.ext.history;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test of client module with server core.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/client/ext/history/springConfig/spring-history.xml" })
public class ClientModuleIntegrationTest {

  /**
   * Checks the module starts up with the server core.
   */
  @Test
  public void moduleStartUp() {
    //do nothing
  }
  
}
