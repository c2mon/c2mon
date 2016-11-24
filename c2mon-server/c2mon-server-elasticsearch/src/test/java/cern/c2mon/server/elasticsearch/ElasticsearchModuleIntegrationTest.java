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
package cern.c2mon.server.elasticsearch;

import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
import cern.c2mon.server.elasticsearch.connector.Connector;
import cern.c2mon.server.elasticsearch.connector.TransportConnector;
import cern.c2mon.server.elasticsearch.listener.EsAlarmLogListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * Integration test with the core modules.
 *
 * @author Alban Marguet
 */
@Slf4j
public class ElasticsearchModuleIntegrationTest extends BaseElasticsearchIntegrationTest {

  @Autowired
  private TransportConnector connector;

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
