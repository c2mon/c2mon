package cern.c2mon.server.command;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test with the core modules (cache, cachepersistence and daq-out).
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/command/config/server-command-integration.xml"})
public class CommandModuleIntegrationTest {

  @Test
  public void testModuleStartup() {
    //do nothing
  }
  
  
}
