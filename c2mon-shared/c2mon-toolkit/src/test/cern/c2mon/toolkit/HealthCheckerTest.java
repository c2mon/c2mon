/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
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
    System.setProperty("c2mon.healthchecker.config.location","classpath:/cern/c2mon/toolkit/health-checker-test.properties");
    String report = HealthChecker.checkHealth();
    assertNotNull(report);
  }
  
}
