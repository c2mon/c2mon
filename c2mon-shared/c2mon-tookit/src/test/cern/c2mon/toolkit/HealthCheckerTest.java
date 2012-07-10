package cern.c2mon.toolkit;
import static org.junit.Assert.*;

import org.junit.Test;


/**
 * Tests the health checker runs against the test system.
 *
 * @author Mark Brightwell
 *
 */
public class HealthCheckerTest {

  /**
   * Runs against classpath resource. No status is checked, only check if a report is returned
   * (this should always be the case, even if components are unavailable).
   * @throws Exception if problem while running check
   */
  @Test
  public void runHealthCheck() throws Exception {
    System.setProperty("c2mon.healthchecker.config.location","classpath:/cern/c2mon/toolkit/health-checker.properties");
    String report = HealthChecker.checkHealth();
    assertNotNull(report);
  }
  
}
