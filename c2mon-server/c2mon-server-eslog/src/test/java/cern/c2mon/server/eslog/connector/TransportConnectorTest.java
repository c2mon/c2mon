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
package cern.c2mon.server.eslog.connector;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.eslog.config.EsLogIntegrationConfiguration;
import cern.c2mon.server.eslog.structure.mappings.EsTagMapping;
import cern.c2mon.server.eslog.structure.types.tag.EsTag;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test the entire functionality of the node.
 * Build the Spring environment.
 * @author Alban Marguet.
 */
@Slf4j
@ContextConfiguration(classes = {
    EsLogIntegrationConfiguration.class
})
@TestPropertySource("classpath:c2mon-server-default.properties")
@RunWith(SpringJUnit4ClassRunner.class)
public class TransportConnectorTest {
  private int shards = 10;
  private int replica = 0;
  private int localPort = 1;
  private String isLocal = "true";

  @Autowired
  private TransportConnector connector;

  @Before
  public void clientSetup() {
    while(!connector.isConnected()) {
      sleep();
    }
    log.debug("Connected to the cluster " + connector.getCluster());
  }

  @After
  public void tidyUp() {
    log.info("@After");
    connector.getClient().admin().indices().delete(new DeleteIndexRequest("*")).actionGet();
  }

  @Test
  public void testInit() {
    Settings expectedSettings = Settings.settingsBuilder()
            .put("node.local", true)
            .put("http.enabled", false)
            .put("node.name", connector.getNode())
            .put("cluster.name", connector.getCluster())
            .build();

    assertTrue(connector.isConnected());
    assertNotNull(connector.getClient());
    assertTrue(connector.isLocal());
    assertEquals(expectedSettings, connector.getSettings());
    assertEquals(isLocal, connector.getSettings().get("node.local"));
    assertEquals(connector.getNode(), connector.getSettings().get("node.name"));
    assertNotNull(connector.getBulkProcessor());
  }

  @Test
  public void testInitTestPass() {
    Client initClient = connector.getClient();

    connector.setClient(null);
    boolean isPassed = connector.waitForYellowStatus();
    assertFalse(isPassed);

    connector.setClient(initClient);
    isPassed = connector.waitForYellowStatus();
    assertTrue(isPassed);
  }

  @Test
  public void testCreateLocalClient() {
    assertNotNull(connector.getClient());
    assertEquals(localPort, connector.getPort());
    assertEquals(isLocal, connector.getSettings().get("node.local"));
  }

  @Test
  public void testHandleIndexQuery() {
    Client initClient = connector.getClient();

    String type = "type_string";
    String mapping = new EsTagMapping(EsTag.TYPE_STRING, String.class.getName()).getMapping();

    connector.setClient(initClient);
    boolean result = connector.handleIndexQuery("c2mon_2015-01", null, null);
    assertTrue(result);
  }

  @Test
  public void testBulkAdd() {
    BulkProcessor initBulkProcessor = connector.getBulkProcessor();
    boolean result = connector.bulkAdd(null);
    assertFalse(result);

    result = connector.bulkAdd(null);
    assertFalse(result);

    connector.setBulkProcessor(initBulkProcessor);
    IndexRequest newIndex = new IndexRequest("c2mon_1973-06", "tag_boolean").source("");
    result = connector.bulkAdd(newIndex);
    assertTrue(result);
  }

  @Test
  public void testGetIndexSettings() {
    Settings expected = Settings.settingsBuilder().put("number_of_shards", shards).put("number_of_replicas", replica).build();
    assertEquals(expected.get("number_of_shards"), getIndexSettings(shards, replica).get("number_of_shards"));
    assertEquals(expected.get("number_of_replicas"), getIndexSettings(shards, replica).get("number_of_replicas"));
  }

  private void sleep() {
    try {
      Thread.sleep(2000);
    } catch(InterruptedException e) {
      e.printStackTrace();
    }
  }


  private Settings getIndexSettings(int numOfShards, int numOfReplicas) {
    return Settings.settingsBuilder().put("number_of_shards", numOfShards)
            .put("number_of_replicas", numOfReplicas).build();
  }
}
