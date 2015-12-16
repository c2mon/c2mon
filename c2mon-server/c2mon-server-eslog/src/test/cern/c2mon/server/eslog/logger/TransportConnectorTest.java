package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.mappings.Mapping;
import cern.c2mon.server.eslog.structure.queries.*;
import cern.c2mon.server.eslog.structure.types.TagBoolean;
import cern.c2mon.server.eslog.structure.types.TagES;
import cern.c2mon.server.eslog.structure.types.TagString;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.*;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the entire functionality of the node.
 * 
 * @author Alban Marguet.
 */
@Slf4j
public class TransportConnectorTest {
  static String clusterName;
  static String nodeName;
  static String host;
  static String home;
  static Node clusterNode;
  static Client clusterClient;

  TransportConnector connector;

  @BeforeClass
  public static void initCluster() {
    log.info("@BeforeClass");
    clusterName = "elasticsearch";
    home = "../config/elasticsearch";
    host = "localhost";
    nodeName = "transportNode";

		clusterNode = nodeBuilder()
				.settings(Settings.settingsBuilder()
						.put("path.home", home)
						.put("cluster.name", clusterName)
						.put("node.local", true)
						.put("node.name", "ClusterNode")
						.put("node.data", true)
						.put("node.master", true)
						.put("http.enabled", false)
						.put("transport.host", "localhost")
						.put("transport.tcp.port", 9300)
						.build())
				.node();

    clusterNode.start();
    clusterClient = clusterNode.client();
    log.info("Node created with home " + home + " in cluster " + clusterName + ".");
    clusterClient.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
  }

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

    connector = new TransportConnector();
    connector.setLocal(true);
    connector.setCluster(clusterName);
    connector.setHost(host);
    connector.setNode(nodeName);
    connector.init();
		connector.setPort(9300); //Because of the setLocal(true); to be in default mode.
    // clean(connector.getClient(), connector.getIndices());
  }

  @After
  public void tidyUp() {
    log.info("@After");
    clean(connector.getClient(), connector.getIndices());
    connector.getAliases().clear();
    connector.getIndices().clear();
    connector.getTypes().clear();
    connector.close(connector.getClient());
  }

  @AfterClass
  public static void cleanCluster() {
    log.info("@AfterClass");
    clusterClient.close();
    clusterNode.close();
  }

  @Test
  public void testInit() {
    Settings expectedSettings = Settings.settingsBuilder().put("node.local", true).put("node.name", nodeName).put("cluster.name", clusterName).build();

    assertNotNull(connector.getClient());
    assertTrue(connector.isLocal());
    assertEquals(expectedSettings, connector.getSettings());
    assertEquals("true", connector.getSettings().get("node.local"));
    assertEquals(nodeName, connector.getSettings().get("node.name"));
    assertNotNull(connector.getIndices());
    assertNotNull(connector.getTypes());
    assertNotNull(connector.getAliases());
    assertNotNull(connector.getBulkProcessor());
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
  public void testCreateClient() {
    Client client = connector.createClient();
    // setLocal(true) in @Before
    assertNotNull(connector.getClient());
    assertEquals(1, connector.getPort());
    assertEquals("true", connector.getSettings().get("node.local"));
    client.close();

    connector.setLocal(false);
    connector.setPort(9300);
    client = connector.createClient();
    assertNotNull(connector.getClient());
    assertEquals(9300, connector.getPort());
    assertEquals("localhost", connector.getHost());
    client.close();
  }

  @Test
  public void testHandleListingQuery() {
    Client client = connector.getClient();
    Query query = null;
    Set<String> result = new HashSet<>();

    assertNull(connector.handleListingQuery(query));

    query = new QueryIndexBuilder(client);
    assertNull(connector.handleListingQuery(query));

    query = new QueryIndices(client);
    result.addAll(connector.handleListingQuery(query));
    assertEquals(connector.getIndices(), result);

    query = new QueryAliases(client);
    result = new HashSet<>();
    result.addAll(connector.handleListingQuery(query));
    assertEquals(connector.getAliases(), result);

    query = new QueryTypes(client);
    result = new HashSet<>();
    result.addAll(connector.handleListingQuery(query));
    assertEquals(connector.getTypes(), result);
  }

  @Test
  public void testHandleIndexQuery() {
    Set<String> init = new HashSet<>();
    init.addAll(connector.getIndices());
    Settings settings = connector.getMonthIndexSettings();
    String type = "tag_string";
    String mapping = "";

    Client client = null;
    Query query = new QueryIndices(client); // Bad Query type
    boolean result = connector.handleIndexQuery(query, "nullIndex", settings, type, mapping);
    assertEquals(init, connector.getIndices());
    assertFalse(result);

    // due to bad Query type
    client = connector.getClient();
    result = connector.handleIndexQuery(query, "nullIndex", settings, type, mapping);
    assertFalse(result);

    // Parameters not set
    query = new QueryIndexBuilder(client);
    result = connector.handleIndexQuery(query, "nullIndex", settings, type, mapping);
    assertFalse(result);

    query = createIndexQuery(client);
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

    Client client = null;
    Query query = new QueryAliases(client);
    boolean result = connector.handleAliasQuery(query, "nullIndex", "tag_1");
    assertEquals(init, connector.getAliases());
    assertFalse(result);

    // Client is null
    result = connector.handleAliasQuery(query, "c2mon_2015-01", "tag_1");
    assertEquals(init, connector.getAliases());
    assertFalse(result);

    client = connector.getClient();
    query.setClient(client);
    result = connector.handleAliasQuery(query, "badFormat", "badFormat");
    assertFalse(result);

    connector.handleIndexQuery(createIndexQuery(client), "c2mon_2015-01", settings, type, mapping);
    result = connector.handleAliasQuery(query, "c2mon_2015-01", "tag_1");
    assertTrue(result);
    assertFalse(init.containsAll(connector.getAliases()));
  }

  @Test
  public void testInstantiateIndex() {
    String index = "c2mon_2015-02";
    String type = "tag_string";
    TagES tag = new TagString();
    tag.setMapping("string");

    boolean isAcked = connector.instantiateIndex(tag, index, type);
    assertTrue(isAcked);
    assertTrue(connector.getIndices().contains(index));
    assertTrue(connector.getTypes().contains(type));
  }

  @Test
  public void testUpdateLists() {
    Set<String> expectedIndex = new HashSet<>();
    Set<String> expectedType = new HashSet<>();
    Set<String> expectedAlias = new HashSet<>();
    assertEquals(expectedIndex, connector.getIndices());
    assertEquals(expectedType, connector.getTypes());
    assertEquals(expectedAlias, connector.getAliases());

    connector.handleIndexQuery(createIndexQuery(connector.getClient()), "c2mon_2015-01", Settings.EMPTY, "tag_string", "");
    connector.updateLists();
    expectedIndex.add("c2mon_2015-01");
    expectedType.add("tag_string");
    assertEquals(expectedIndex, connector.getIndices());
    assertEquals(expectedType, connector.getTypes());
    assertEquals(expectedAlias, connector.getAliases());
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
    tag.setTagServerTime(123456L);
    String value = connector.generateIndex(tag.getTagServerTime());
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
    tag.setTagId(1L);
    tag.setDataType(Mapping.stringType);
    tag.setMapping(Mapping.stringType);
    connector.getAliases().add("tag_1");
    connector.handleIndexQuery(createIndexQuery(connector.getClient()), "c2mon_2015-12", connector.getMonthIndexSettings(),
        connector.generateType(tag.getDataType()), tag.getMapping());

    assertFalse(connector.bulkAddAlias("c2mon_2015-12", tag));
    connector.getAliases().clear();
    assertTrue(connector.bulkAddAlias("c2mon_2015-12", tag));
    assertTrue(connector.getAliases().contains("tag_1"));
  }

  @Test
  public void testBadBulkAdd() throws IOException {
    TagES tag = new TagString();
    tag.setDataType(Mapping.stringType);
    tag.setTagId(1L);
    tag.setMapping(Mapping.stringType);
    assertFalse(connector.bulkAdd(null, tag.getDataType(), tag.build(), tag));
    assertFalse(connector.bulkAdd("c2mon_2015-12", "badType", tag.build(), tag));
    assertFalse(connector.getIndices().contains("c2mon_2015-12"));
    assertTrue(connector.bulkAdd("c2mon_2015-12", connector.generateType(tag.getDataType()), tag.build(), tag));
    assertTrue(connector.getIndices().contains("c2mon_2015-12"));
  }

  @Test
  public void testIndexTag() {
    TagES tag = new TagString();
    tag.setDataType(Mapping.stringType);
    tag.setMapping(Mapping.stringType);
    tag.setTagId(1L);
    tag.setTagServerTime(123456789000L);
    connector.indexTag(tag);
    assertTrue(connector.getIndices().contains(connector.generateIndex(123456789000L)));
    assertTrue(connector.getTypes().contains(connector.generateType(tag.getDataType())));
    // assertTrue(connector.getAliases().contains(connector.generateAliasName(tag.getTagId())));
    QueryIndices query = new QueryIndices(connector.getClient());
    QueryTypes queryTypes = new QueryTypes(connector.getClient());
    // QueryAliases queryAliases = new QueryAliases(connector.getClient());
    assertTrue(connector.handleListingQuery(query).contains(connector.generateIndex(123456789000L)));
    assertTrue(connector.handleListingQuery(queryTypes).contains(connector.generateType(tag.getDataType())));
    // assertTrue(connector.handleListingQuery(queryAliases).contains(connector.generateAliasName(tag.getTagId())));
  }

  @Test
  public void testIndexTags() {
    long size = 1000L;
    List<TagES> list = new ArrayList<>();
    Set<String> listIndices = new HashSet<>();
    Set<String> listAliases = new HashSet<>();
    long id = 1L;
    long tagServerTime = 123456789000L;
    for (; id <= size; id++, tagServerTime += 1000) {
      TagES tag = new TagString();
      tag.setDataType(Mapping.stringType);
      tag.setMapping(Mapping.stringType);
      tag.setTagId(id);
      tag.setTagServerTime(tagServerTime);
      list.add(tag);
      listIndices.add(connector.generateIndex(tag.getTagServerTime()));
      listAliases.add(connector.generateAliasName(tag.getTagId()));
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
    // assertTrue(resultAliases.size() == size && liveAliases.size() == size);

    assertTrue(resultTypes.contains("tag_string") && resultTypes.size() == 1);
    assertTrue(resultIndices.containsAll(listIndices) && resultIndices.size() == listIndices.size());
    // assertTrue(resultAliases.containsAll(listAliases) && resultAliases.size()
    // == listAliases.size());

    assertTrue(liveIndices.containsAll(resultIndices));
    assertTrue(liveTypes.containsAll(resultTypes));
    // assertTrue(liveAliases.containsAll(resultAliases));
  }

  private void clean(Client client, Set<String> indices) {
    log.info("Delete the mess.");
    if (client != null) {
      for (String index : indices) {
        log.info("delete " + index);
        client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet();
      }

      client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
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
