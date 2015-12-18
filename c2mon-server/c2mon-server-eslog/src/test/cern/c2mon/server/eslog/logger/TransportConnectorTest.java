package cern.c2mon.server.eslog.logger;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
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
  static String clusterName;
  static String nodeName;
  static String host;
  static String home;
  static Client clusterClient;
  static Client initClient;
  @Autowired
  TransportConnector connector;

  @Before
  public void clientSetup() throws IOException {
    log.info("@Before");
    log.info("begin delete directory");

    File dataDir = new File(home + "/data");
    File[] data = dataDir.listFiles();

    if (data != null) {
      for (File file : data) {
        file.delete();
      }
    }

    log.info("end. Cleaned ./data directory");

    if (connector.getClient() != null) {
      initClient = connector.getClient();
    }

    nodeName = connector.getNode();
    clusterName = connector.getCluster();
    host = connector.getHost();

    connector.setClient(initClient);
    clusterClient = connector.getClient();
  }

  @After
  public void tidyUp() {
    sleep();

    log.info("@After");

    clean(clusterClient, connector.getIndices());
    connector.getAliases().clear();
    connector.getIndices().clear();
    connector.getTypes().clear();
  }

  @Test
  public void testInit() {
    Settings expectedSettings = Settings.settingsBuilder().put("node.local", true).put("node.name", nodeName).put("cluster.name", clusterName).build();

    assertNotNull(clusterClient);
    assertTrue(connector.isLocal());
    assertEquals(expectedSettings, connector.getSettings());
    assertEquals("true", connector.getSettings().get("node.local"));
    assertEquals(nodeName, connector.getSettings().get("node.name"));
    assertNotNull(connector.getIndices());
    assertNotNull(connector.getTypes());
    assertNotNull(connector.getAliases());
    assertNotNull(connector.getBulkProcessor());
    assertNotNull(connector.getClusterFinder());
  }

  @Test
  public void testInitTestPass() {
    boolean isPassed = connector.initTestPass();
    assertTrue(isPassed);

    connector.setClient(null);
    isPassed = connector.initTestPass();
    assertFalse(isPassed);
  }

  @Test
  public void testCreateLocalClient() {
    assertNotNull(clusterClient);
    assertEquals(1, connector.getPort());
    assertEquals("true", connector.getSettings().get("node.local"));
  }

  @Test
  public void testHandleListingQuery() {
    Query query = null;
    Set<String> result = new HashSet<>();

    assertTrue(connector.handleListingQuery(query).size() == 0);

    query = new QueryIndices(clusterClient);
    result.addAll(connector.handleListingQuery(query));
    assertEquals(connector.getIndices(), result);

    query = new QueryAliases(clusterClient);
    result = new HashSet<>();
    result.addAll(connector.handleListingQuery(query));
    assertEquals(connector.getAliases(), result);

    query = new QueryTypes(clusterClient);
    result = new HashSet<>();
    result.addAll(connector.handleListingQuery(query));
    assertEquals(connector.getTypes(), result);
  }

  @Test
  public void testHandleIndexQuery() {
    Client initClient = clusterClient;

    Set<String> init = new HashSet<>();
    init.addAll(connector.getIndices());
    Settings settings = connector.getMonthIndexSettings();
    String type = "tag_string";
    String mapping = "";

    clusterClient = null;
    Query query = new QueryIndices(clusterClient); // Bad Query type
    boolean result = connector.handleIndexQuery(query, "nullIndex", settings, type, mapping);
    assertEquals(init, connector.getIndices());
    assertFalse(result);

    // due to bad Query type
    connector.setClient(initClient);
    clusterClient = connector.getClient();
    result = connector.handleIndexQuery(query, "nullIndex", settings, type, mapping);
    assertFalse(result);

    // Parameters not set
    query = new QueryIndexBuilder(clusterClient);
    result = connector.handleIndexQuery(query, "nullIndex", settings, type, mapping);
    assertFalse(result);

    query = createIndexQuery(clusterClient);
    result = connector.handleIndexQuery(query, "c2mon_2015-01", settings, type, mapping);
    assertTrue(result);
    assertFalse(init.containsAll(connector.getIndices()));
  }

  @Test
  public void testHandleAliasQuery() {
    Set<String> init = new HashSet<>();
    init.addAll(connector.getAliases());
    Settings settings = Settings.settingsBuilder().build();
    String type = "tag_string";
    String mapping = "";

    clusterClient = null;
    Query query = new QueryAliases(clusterClient);
    boolean result = connector.handleAliasQuery(query, "nullIndex", "tag_1");
    assertEquals(init, connector.getAliases());
    assertFalse(result);

    // Client is null
    result = connector.handleAliasQuery(query, "c2mon_2015-01", "tag_1");
    assertEquals(init, connector.getAliases());
    assertFalse(result);

    clusterClient = initClient;
    query.setClient(clusterClient);
    result = connector.handleAliasQuery(query, "badFormat", "badFormat");
    assertFalse(result);

    connector.handleIndexQuery(createIndexQuery(clusterClient), "c2mon_2015-01", settings, type, mapping);
    result = connector.handleAliasQuery(query, "c2mon_2015-01", "tag_1");
    assertTrue(result);
    assertFalse(init.containsAll(connector.getAliases()));
  }

  @Test
  public void testInstantiateIndex() {
    String index = "c2mon_2015-02";
    String type = "tag_string";
    TagES tag = new TagString();
    tag.setMapping(ValueType.stringType);

    boolean isAcked = connector.instantiateIndex(tag, index, type);
    assertTrue(isAcked);
    assertTrue(connector.getIndices().contains(index));
    assertTrue(connector.getTypes().contains(type));
  }

  @Test
  public void testUpdateLists() {
    Set<String> expectedIndex = new HashSet<>();
    Set<String> expectedType = new HashSet<>();
    assertEquals(expectedIndex, connector.getIndices());
    assertEquals(expectedType, connector.getTypes());

    connector.handleIndexQuery(createIndexQuery(connector.getClient()), "c2mon_2015-01", Settings.EMPTY, "tag_string", "");
    connector.updateLists();
    expectedIndex.add("c2mon_2015-01");
    expectedType.add("tag_string");
    assertEquals(expectedIndex, connector.getIndices());
    assertEquals(expectedType, connector.getTypes());
  }

  @Test
  public void testGenerateAliasName() {
    String expected = connector.getTAG_PREFIX() + "1";
    String value = connector.generateAliasName(1L);
    assertEquals(expected, value);
  }

  @Test
  public void testGenerateType() {
    String expected = connector.getTAG_PREFIX() + "string";
    TagES tag = new TagString();
    tag.setDataType("String");
    String value = connector.generateType(tag.getDataType());
    assertEquals(expected, value);
  }

  @Test
  public void testGenerateIndex() {
    String expected = connector.getINDEX_PREFIX() + connector.millisecondsToYearMonth(123456L);
    TagES tag = new TagBoolean();
    tag.setServerTime(123456L);
    String value = connector.generateIndex(tag.getServerTime());
    assertEquals(expected, value);
  }

  @Test
  public void testMillisecondsToYearMonth() {
    String expected = "2015-12";
    String value = connector.millisecondsToYearMonth(1448928000000L);
    assertEquals(expected, value);
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

  @Test(expected = IllegalArgumentException.class)
  public void testAddBadIndex() {
    String index = "c2mon_bad";
    connector.addIndex(index);
  }

  @Test
  public void testAddGoodIndex() {
    String index = "c2mon_2005-06";
    Set<String> expected = new HashSet<>();
    expected.add(index);
    connector.addIndex(index);
    assertEquals(expected, connector.getIndices());
    connector.getIndices().clear();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBadType() {
    String index = "c2mon_bad";
    connector.addType(index);
  }

  @Test
  public void testAddGoodType() {
    String index = "tag_string";
    Set<String> expected = new HashSet<>();
    expected.add(index);
    connector.addType(index);
    assertEquals(expected, connector.getTypes());
    connector.getTypes().clear();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBadAlias() {
    String index = "tag_bad";
    connector.addIndex(index);
  }

  @Test
  public void testAddGoodAlias() {
    String index = "tag_12658";
    Set<String> expected = new HashSet<>();
    expected.add(index);
    connector.addAlias(index);
    assertEquals(expected, connector.getAliases());
    connector.getAliases().clear();
  }

  @Test
  public void testCheckIndex() {
    assertTrue(connector.checkIndex("c2mon_1234-69"));
    assertFalse(connector.checkIndex("c2mon-1234-69"));
    assertFalse(connector.checkIndex("c2mon_1234.69"));
    assertFalse(connector.checkIndex("tag_1234-69"));
  }

  @Test
  public void testCheckType() {
    assertTrue(connector.checkType("tag_string"));
    assertTrue(connector.checkType("tag_integer"));
    assertTrue(connector.checkType("tag_long"));
    assertTrue(connector.checkType("tag_double"));
    assertTrue(connector.checkType("tag_boolean"));
    assertFalse(connector.checkType("nope"));
    assertFalse(connector.checkType("c2mon_1970-1"));
  }

  @Test
  public void testAddBulkAlias() {
    assertTrue(connector.checkAlias("tag_1"));
    assertTrue(connector.checkAlias("tag_114564894132185"));
    assertFalse(connector.checkAlias("tag-114564894132185"));
    assertFalse(connector.checkAlias("c2mon-114564894132185"));
    assertFalse(connector.checkAlias("tag_lalala"));
    assertFalse(connector.checkAlias("tag_45678.2"));
  }

  @Test
  public void testGetMonthIndexSettings() {
    Settings expected = Settings.settingsBuilder().put("number_of_shards", 10).put("number_of_replicas", 0).build();
    assertEquals(expected.get("number_of_shards"), connector.getMonthIndexSettings().get("number_of_shards"));
    assertEquals(expected.get("number_of_replicas"), connector.getMonthIndexSettings().get("number_of_replicas"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadBulkAddAlias() {
    connector.bulkAddAlias("badIndex", null);
  }

  @Test
  public void testBulkAddAlias() {
    TagES tag = new TagString();
    tag.setId(1L);
    tag.setDataType(ValueType.stringType.toString());
    tag.setMapping(ValueType.stringType);
    connector.getAliases().add("tag_1");
    connector.handleIndexQuery(createIndexQuery(connector.getClient()), "c2mon_2015-12", connector.getMonthIndexSettings(),
        connector.generateType(tag.getDataType()), tag.getMapping());

    assertFalse(connector.bulkAddAlias("c2mon_2015-12", tag));
    connector.getAliases().clear();
    assertTrue(connector.bulkAddAlias("c2mon_2015-12", tag));
    assertTrue(connector.getAliases().contains("tag_1"));
  }

  @Test
  @Ignore
  public void testBadBulkAdd() throws IOException {
    TagES tag = new TagString();
    tag.setDataType(ValueType.stringType.toString());
    tag.setId(1L);
    tag.setMapping(ValueType.stringType);

    assertFalse(connector.bulkAdd(null, tag.getDataType(), tag.build(), tag));
    assertFalse(connector.bulkAdd("c2mon_2015-12", "badType", tag.build(), tag));
    assertFalse(connector.getIndices().contains("c2mon_2015-12"));

    assertTrue(connector.bulkAdd("c2mon_2015-12", connector.generateType(tag.getDataType()), tag.build(), tag));
    connector.closeBulk();
    assertTrue(connector.getIndices().size() == 1);
    assertTrue(connector.getIndices().contains("c2mon_2015-12"));
  }

  @Test
  @Ignore
  public void testIndexTag() {
    TagES tag = new TagString();
    tag.setDataType(ValueType.stringType.toString());
    tag.setMapping(ValueType.stringType);
    tag.setId(1L);
    tag.setServerTime(123456789000L);

    connector.indexTag(tag);
    connector.closeBulk();

    assertTrue(connector.getIndices().contains(connector.generateIndex(123456789000L)));
    assertTrue(connector.getTypes().contains(connector.generateType(tag.getDataType())));

    QueryIndices query = new QueryIndices(connector.getClient());
    QueryTypes queryTypes = new QueryTypes(connector.getClient());

    assertTrue(connector.handleListingQuery(query).contains(connector.generateIndex(123456789000L)));
    assertTrue(connector.handleListingQuery(queryTypes).contains(connector.generateType(tag.getDataType())));
  }

  @Test
  public void testIndexTags() {
    long size = 10;
    List<TagES> list = new ArrayList<>();
    Set<String> listIndices = new HashSet<>();
    Set<String> listAliases = new HashSet<>();
    long id = 1L;
    long tagServerTime = 123456789000L;

    for (; id <= size; id++, tagServerTime += 1000) {
      TagES tag = new TagString();
      tag.setDataType(ValueType.stringType.toString());
      tag.setMapping(ValueType.stringType);
      tag.setId(id);
      tag.setServerTime(tagServerTime);
      list.add(tag);
      listIndices.add(connector.generateIndex(tag.getServerTime()));
      listAliases.add(connector.generateAliasName(tag.getId()));
    }

    connector.indexTags(list);

    Set<String> resultIndices = connector.getIndices();
    Set<String> resultAliases = connector.getAliases();
    Set<String> resultTypes = connector.getTypes();

    QueryIndices queryIndices = new QueryIndices(connector.getClient());
    QueryTypes queryTypes = new QueryTypes(connector.getClient());
    QueryAliases queryAliases = new QueryAliases(connector.getClient());

    List<String> liveIndices = connector.handleListingQuery(queryIndices);
    List<String> liveTypes = connector.handleListingQuery(queryTypes);
    List<String> liveAliases = connector.handleListingQuery(queryAliases);
    List<Long> queryIds = new ArrayList<>();

    for (long i = 1; i <= size; i++) {
      queryIds.add(i);
    }

    QueryIndices query = new QueryIndices(connector.getClient(), Arrays.asList("c2mon_1973-11"), true, Arrays.asList("tag_string"), queryIds, 0, 10, -1, -1);
    SearchResponse response = query.getResponse();
    log.info(response.toString());

    assertEquals(size, response.getHits().getTotalHits());
    assertTrue(resultIndices.size() == liveIndices.size());
    assertTrue(resultAliases.size() == size && liveAliases.size() == size);

    assertTrue(resultTypes.contains("tag_string") && resultTypes.size() == 1);
    assertTrue(resultIndices.containsAll(listIndices) && resultIndices.size() == listIndices.size());
    assertTrue(resultAliases.containsAll(listAliases) && resultAliases.size() == listAliases.size());

    assertTrue(liveIndices.containsAll(resultIndices));
    assertTrue(liveTypes.containsAll(resultTypes));
    assertTrue(liveAliases.containsAll(resultAliases));
  }

  private void clean(Client client, Set<String> indices) {
    log.info("Delete the indices present in the ElasticSearch cluster.");
    if (client != null) {
      for (String index : indices) {
        log.info("delete " + index);
        client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet();
      }

      client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
      client.admin().indices().prepareRefresh().execute().actionGet();
    }
  }

  private void sleep() {
    try {
      Thread.sleep(1000L);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private QueryIndexBuilder createIndexQuery(Client client) {
    return new QueryIndexBuilder(client, Arrays.asList("c2mon_2015-01"), true, Arrays.asList("tag_String"), Arrays.asList(1L), 0, 1, -1, -1);
  }
}
