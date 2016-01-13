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
package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;
import cern.c2mon.server.eslog.structure.mappings.TagStringMapping;
import cern.c2mon.server.eslog.structure.queries.Query;
import cern.c2mon.server.eslog.structure.queries.QueryAliases;
import cern.c2mon.server.eslog.structure.queries.QueryIndices;
import cern.c2mon.server.eslog.structure.queries.QueryTypes;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the entire functionality of the node.
 * Need to disable c2mon.properties.
 * @author Alban Marguet.
 */
@Slf4j
@ContextConfiguration({"classpath:cern/c2mon/server/eslog/config/server-eslog-integration.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class TransportConnectorTest {
  @Autowired
  TransportConnector connector;

  @Before
  public void clientSetup() {
    while (!connector.isConnected()) {
      sleep();
    }
    log.debug("Connected to the cluster " + connector.getCluster());
  }

  @After
  public void tidyUp() {
    sleep();
    log.info("@After");
    connector.getClient().admin().indices().delete(new DeleteIndexRequest("*")).actionGet();
  }

  @Test
  public void testInit() {
    Settings expectedSettings = Settings.settingsBuilder().put("node.local", true).put("node.name", connector.getNode()).put("cluster.name", connector.getCluster()).build();
    assertTrue(connector.isConnected());
    assertNotNull(connector.getClient());
    assertTrue(connector.isLocal());
    assertEquals(expectedSettings, connector.getSettings());
    assertEquals("true", connector.getSettings().get("node.local"));
    assertEquals(connector.getNode(), connector.getSettings().get("node.name"));
    assertNotNull(connector.getBulkProcessor());
    assertNotNull(connector.getClusterFinder());
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
    assertEquals(1, connector.getPort());
    assertEquals("true", connector.getSettings().get("node.local"));
  }

  @Test
  public void testHandleListingQuery() {
    Query query = null;
    Set<String> result = new HashSet<>();

    assertEquals(0, connector.handleListingQuery(query, null).size());

    query = new QueryIndices(connector.getClient());
    result.addAll(connector.handleListingQuery(query, null));
    assertEquals(0, result.size());

    query = new QueryAliases(connector.getClient());
    result = new HashSet<>();
    result.addAll(connector.handleListingQuery(query, null));
    assertEquals(0, result.size());

    query = new QueryTypes(connector.getClient());
    result = new HashSet<>();
    result.addAll(connector.handleListingQuery(query, null));
    assertEquals(0, result.size());
  }

  @Test
  public void testHandleIndexQuery() {
    Client initClient = connector.getClient();

    Settings settings = createMonthSettings();
    String type = "tag_string";
    String mapping = new TagStringMapping(ValueType.stringType).getMapping();

    connector.setClient(null); // should be caught
    boolean result = connector.handleIndexQuery("c2mon_2015-01", settings, type, mapping);
    assertFalse(result);

    connector.setClient(initClient);
    result = connector.handleIndexQuery("c2mon_2015-01", settings, null, null);
    assertTrue(result);
  }

  @Test
  public void testHandleAliasQuery() {
    Client initClient = connector.getClient();
    Settings settings = Settings.settingsBuilder().build();
    String type = "tag_string";
    String mapping = new TagStringMapping(ValueType.stringType).getMapping();

    connector.setClient(null);
    // Client is null
    boolean result = connector.handleAliasQuery("c2mon_2015-01", "tag_1");
    assertFalse(result);

    connector.setClient(initClient);
    connector.getClient().admin().indices().prepareCreate("c2mon_2015-01").execute().actionGet();
    connector.handleIndexQuery("c2mon_2015-01", settings, type, mapping);
    result = connector.handleAliasQuery("c2mon_2015-01", "tag_1");
    assertTrue(result);
  }

  @Test
  public void testBulkAdd() {
    BulkProcessor initBulkProcessor = connector.getBulkProcessor();

    boolean result = connector.bulkAdd(null);
    assertFalse(result);

    connector.setBulkProcessor(null);
    result = connector.bulkAdd(null);
    assertFalse(result);

    connector.setBulkProcessor(initBulkProcessor);
    IndexRequest newIndex = new IndexRequest("c2mon_1973-06", "tag_boolean").source("");
    result = connector.bulkAdd(newIndex);
    assertTrue(result);
  }

  @Test
  public void testCloseBulk() {
    connector.closeBulk();
    try {
      assertTrue(connector.getBulkProcessor().awaitClose(10, TimeUnit.SECONDS));
      assertNotNull(connector.getBulkProcessor());
    }
    catch (InterruptedException e) {
      log.debug("how come?");
    }
  }

  @Test
  public void testGetIndexSettings() {
    Settings expected = Settings.settingsBuilder().put("number_of_shards", 10).put("number_of_replicas", 0).build();
    assertEquals(expected.get("number_of_shards"), connector.getIndexSettings(10, 0).get("number_of_shards"));
    assertEquals(expected.get("number_of_replicas"), connector.getIndexSettings(10, 0).get("number_of_replicas"));
  }

  private void sleep() {
    try {
      Thread.sleep(2000L);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private Settings createMonthSettings() {
    return Settings.settingsBuilder().put("number_of_shards", 10)
        .put("number_of_replicas", 0).build();
  }
}
