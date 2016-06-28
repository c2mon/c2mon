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
package cern.c2mon.server.eslog;

import cern.c2mon.server.eslog.config.EsLogConfiguration;
import cern.c2mon.server.eslog.config.EsLogIntegrationConfiguration;
import cern.c2mon.server.eslog.connector.Connector;
import cern.c2mon.server.eslog.listener.EsAlarmLogListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

/**
 * Integration test with the core modules.
 * @author Alban Marguet.
 */
@Slf4j
@ContextConfiguration(classes = {
    EsLogIntegrationConfiguration.class
})
@TestPropertySource("classpath:c2mon-server-default.properties")
@RunWith(SpringJUnit4ClassRunner.class)
public class ESLogModuleIntegrationTest {

  @Autowired
  private Connector connector;

  @Autowired
  private EsAlarmLogListener esAlarmLogListener;

  @Before
  public void setup() {
    while(!connector.isConnected()) {
      connector.waitForYellowStatus();
    }
  }

  @Test
  public void testModuleStartup() {
    String[] indices = connector.getClient().admin().indices().prepareGetIndex().get().indices();
    log.info("indices in the cluster:");
    for (String index : indices) {
      log.info(index);
    }
  }

}
