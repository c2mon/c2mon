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
package cern.c2mon.server.elasticsearch.connector;

import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the entire functionality of the node.
 *
 * @author Alban Marguet
 */
@Slf4j
public class TransportConnectorTest extends BaseElasticsearchIntegrationTest {

  @Autowired
  private TransportConnector connector;

  @Autowired
  private ElasticsearchProperties properties;

  @Before
  public void clientSetup() {
    while(!connector.isConnected()) {
      sleep();
    }
    log.debug("Connected to the cluster " + properties.getClusterName());
  }

  @After
  public void tidyUp() {
    connector.getClient().admin().indices().delete(new DeleteIndexRequest("*")).actionGet();
  }

  @Test
  public void testInit() {
    Settings expectedSettings = Settings.settingsBuilder()
            .put("node.local", true)
            .put("http.enabled", false)
            .put("node.name", properties.getNodeName())
            .put("cluster.name", properties.getClusterName())
            .build();

    assertTrue(connector.isConnected());
    assertNotNull(connector.getClient());
    assertTrue(properties.isEmbedded());
    assertEquals(expectedSettings, connector.getClient().settings());
    assertEquals("true", connector.getClient().settings().get("node.local"));
    assertEquals(properties.getNodeName(), connector.getClient().settings().get("node.name"));
  }

  @Test
  public void testInitTestPass() {
    Client initClient = connector.getClient();

//    connector.setClient(null);
//    boolean isPassed = connector.waitForYellowStatus();
//    assertFalse(isPassed);
//
//    connector.setClient(initClient);
//    isPassed = connector.waitForYellowStatus();
//    assertTrue(isPassed);
  }

  @Test
  public void testCreateLocalClient() {
    assertNotNull(connector.getClient());
//    assertEquals(1, properties.getPort());
//    assertEquals(isLocal, connector.getSettings().get("node.local"));
  }
//
//  @Test
//  public void testBulkAdd() {
//    IndexRequest newIndex = new IndexRequest("c2mon-tag_1973-06", "tag_boolean").source("");
//    boolean result = connector.bulkAdd(newIndex);
//    assertTrue(result);
//  }

  private void sleep() {
    sleep(500);
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch(InterruptedException e) {
      e.printStackTrace();
    }
  }
}
