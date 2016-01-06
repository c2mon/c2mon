package cern.c2mon.server.eslog.logger;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;
import cern.c2mon.server.eslog.structure.queries.Query;
import cern.c2mon.server.eslog.structure.queries.QueryAliases;
import cern.c2mon.server.eslog.structure.queries.QueryIndexBuilder;
import cern.c2mon.server.eslog.structure.queries.QueryIndices;
import cern.c2mon.server.eslog.structure.queries.QueryTypes;
import cern.c2mon.server.eslog.structure.types.TagBoolean;
import cern.c2mon.server.eslog.structure.types.TagES;
import cern.c2mon.server.eslog.structure.types.TagString;
import lombok.extern.slf4j.Slf4j;

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
    boolean isPassed = connector.initTestPass();
    assertFalse(isPassed);

    connector.setClient(initClient);
    isPassed = connector.initTestPass();
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

    Set<String> init = new HashSet<>();
    Settings settings = createMonthSettings();
    String type = "tag_string";
    String mapping = "";

    connector.setClient(null); // should be caught
    boolean result = connector.handleIndexQuery("c2mon_2015-01", settings, type, mapping);
    assertFalse(result);

    connector.setClient(initClient);
    result = connector.handleIndexQuery("c2mon_2015-01", settings, type, mapping);
    assertTrue(result);
  }

  @Test
  public void testHandleAliasQuery() {
    Client initClient = connector.getClient();
    Set<String> init = new HashSet<>();
    Settings settings = Settings.settingsBuilder().build();
    String type = "tag_string";
    String mapping = "";

    connector.setClient(null);
    // Client is null
    boolean result = connector.handleAliasQuery("c2mon_2015-01", "tag_1");
    assertFalse(result);

    connector.setClient(initClient);
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
      log.info("how come?");
    }
  }

  @Test
  public void testGetIndexSettings() {
    Settings expected = Settings.settingsBuilder().put("number_of_shards", 10).put("number_of_replicas", 0).build();
    assertEquals(expected.get("number_of_shards"), connector.getIndexSettings("INDEX_MONTH_SETTINGS").get("number_of_shards"));
    assertEquals(expected.get("number_of_replicas"), connector.getIndexSettings("INDEX_MONTH_SETTINGS").get("number_of_replicas"));
  }

  private void clean(Client client) {
    if (client != null) {
      client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
      client.admin().indices().prepareRefresh().execute().actionGet();
    }
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
    return Settings.settingsBuilder().put("number_of_shards", IndexSettings.INDEX_MONTH_SETTINGS.getShards())
        .put("number_of_replicas", IndexSettings.INDEX_MONTH_SETTINGS.getReplica()).build();
  }
}