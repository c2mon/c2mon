/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.configuration;

import java.util.*;
import org.junit.Ignore;
import org.junit.Test;

import cern.c2mon.server.configuration.loader.ConfigurationCacheLoaderTest;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;

import static org.junit.Assert.*;

/**
 * Component/integration tests of the configuration module (integrates the cache
 * modules, but mocks the daq module).
 *
 * @author Mark Brightwell
 */
public class ConfigurationLoaderTest extends ConfigurationCacheLoaderTest {

  @Test
  @Ignore
  public void testConcurrentConfigRequestRejected() throws InterruptedException {

    final ConfigurationReport report;

//    clusterCache.acquireWriteLockOnKey(JmsContainerManager.CONFIG_LOCK_KEY);
    try {
      ConcurrentConfigRequestor ccr = new ConcurrentConfigRequestor();
      Thread t = new Thread(ccr);
      t.start();
      t.join();

      report = ccr.report;

      assertTrue(report.toXML().contains(Status.FAILURE.toString()));
      assertTrue(report.toXML().contains("rejected since another configuration is still running"));

    } finally {
//      clusterCache.releaseWriteLockOnKey(JmsContainerManager.CONFIG_LOCK_KEY);
    }
  }

  class ConcurrentConfigRequestor implements Runnable {
    ConfigurationReport report;

    @Override
    public void run() {
      report = configurationLoader.applyConfiguration(2);
    }
  }

  @Test
  @Ignore
  public void testGetConfigurationReportHeaders() {
    List<ConfigurationReportHeader> reports = configurationLoader.getConfigurationReports();
    assertFalse(reports.isEmpty());

    for (ConfigurationReportHeader report : reports) {
      assertNotNull(report.getName());
      assertNotNull(report.getId());
      assertNotNull(report.getStatus());
      assertNotNull(report.getStatusDescription());
    }
  }

  @Test
  @Ignore
  public void testGetConfigurationReports() {
    List<ConfigurationReport> reports = configurationLoader.getConfigurationReports(String.valueOf(1));
    assertFalse(reports.isEmpty());
    assertTrue(reports.size() > 1); // Config 1 gets run 3 times

    reports.addAll(configurationLoader.getConfigurationReports(String.valueOf(2)));
    assertTrue(reports.size() > 1); // Config 2 gets run once

    for (ConfigurationReport report : reports) {
      assertNotNull(report.getName());
      assertNotNull(report.getId());
      assertNotNull(report.getStatus());
      assertNotNull(report.getStatusDescription());
    }
  }
}
