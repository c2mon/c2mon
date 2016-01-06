package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.mappings.Mapping;
import cern.c2mon.server.eslog.structure.queries.QueryAliases;
import cern.c2mon.server.eslog.structure.queries.QueryIndices;
import cern.c2mon.server.eslog.structure.queries.QueryTypes;
import cern.c2mon.server.eslog.structure.types.TagBoolean;
import cern.c2mon.server.eslog.structure.types.TagES;
import cern.c2mon.server.eslog.structure.types.TagString;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Alban Marguet.
 */
@Slf4j
@ContextConfiguration({"classpath:cern/c2mon/server/eslog/config/server-eslog-integration.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class IndexerTest {
  static String clusterName;
  static String nodeName;
  static String host;
  static String home;
  static Client clusterClient;
  static Client initClient;
  @Autowired
  Indexer indexer;
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
    connector.getClient().admin().indices().delete(new DeleteIndexRequest("*")).actionGet();
    connector.getClient().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
    connector.getClient().admin().indices().prepareRefresh().execute().actionGet();
    indexer.getAliases().clear();
    indexer.getIndices().clear();
    indexer.getTypes().clear();
  }

  @Test
  public void testInit() {
    assertTrue(indexer.isAvailable());
    assertNotNull(indexer.getAliases());
    assertNotNull(indexer.getIndices());
    assertNotNull(indexer.getTypes());
  }

  @Test
  public void testInstantiateIndex() {
    String index = "c2mon_2015-02";
    String type = "tag_string";
    TagES tag = new TagString();
    tag.setMapping(Mapping.ValueType.stringType);

    boolean isAcked = indexer.instantiateIndex(tag, index, type);
    assertTrue(isAcked);
    assertTrue(indexer.getIndices().contains(index));
    assertTrue(indexer.getTypes().contains(type));
  }

  @Test
  public void testUpdateLists() {
    Set<String> expectedIndex = new HashSet<>();
    Set<String> expectedType = new HashSet<>();
    assertEquals(expectedIndex, indexer.getIndices());
    assertEquals(expectedType, indexer.getTypes());
    expectedIndex.add("c2mon_2015-01");

    connector.handleIndexQuery("c2mon_2015-01", Settings.EMPTY, "", "");
    indexer.updateLists();

    assertEquals(expectedIndex, indexer.getIndices());
    assertEquals(expectedType, indexer.getTypes());
  }

  @Test
  public void testGenerateAliasName() {
    String expected = indexer.getTAG_PREFIX() + "1";
    String value = indexer.generateAliasName(1L);
    assertEquals(expected, value);
  }

  @Test
  public void testGenerateType() {
    String expected = indexer.getTAG_PREFIX() + "string";
    TagES tag = new TagString();
    tag.setDataType("String");
    String value = indexer.generateType(tag.getDataType());
    assertEquals(expected, value);
  }

  @Test
  public void testGenerateIndex() {
    String expected = indexer.getINDEX_PREFIX() + indexer.millisecondsToYearMonth(123456L);
    TagES tag = new TagBoolean();
    tag.setServerTimestamp(123456L);
    String value = indexer.generateIndex(tag.getServerTimestamp());
    assertEquals(expected, value);
  }

  @Test
  public void testMillisecondsToYearMonth() {
    String expected = "2015-12";
    String value = indexer.millisecondsToYearMonth(1448928000000L);
    assertEquals(expected, value);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBadIndex() {
    String index = "c2mon_bad";
    indexer.addIndex(index);
  }

  @Test
  public void testAddGoodIndex() {
    String index = "c2mon_2005-06";
    Set<String> expected = new HashSet<>();
    expected.add(index);
    indexer.addIndex(index);
    assertEquals(expected, indexer.getIndices());
    indexer.getIndices().clear();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBadType() {
    String index = "c2mon_bad";
    indexer.addType(index);
  }

  @Test
  public void testAddGoodType() {
    String index = "tag_string";
    Set<String> expected = new HashSet<>();
    expected.add(index);
    indexer.addType(index);
    assertEquals(expected, indexer.getTypes());
    indexer.getTypes().clear();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBadAlias() {
    String index = "tag_bad";
    indexer.addIndex(index);
  }

  @Test
  public void testAddGoodAlias() {
    String index = "tag_12658";
    Set<String> expected = new HashSet<>();
    expected.add(index);
    indexer.addAlias(index);
    assertEquals(expected, indexer.getAliases());
    indexer.getAliases().clear();
  }

  @Test
  public void testCheckIndex() {
    assertTrue(indexer.checkIndex("c2mon_1234-69"));
    assertFalse(indexer.checkIndex("c2mon-1234-69"));
    assertFalse(indexer.checkIndex("c2mon_1234.69"));
    assertFalse(indexer.checkIndex("tag_1234-69"));
  }

  @Test
  public void testCheckType() {
    assertTrue(indexer.checkType("tag_string"));
    assertTrue(indexer.checkType("tag_integer"));
    assertTrue(indexer.checkType("tag_long"));
    assertTrue(indexer.checkType("tag_double"));
    assertTrue(indexer.checkType("tag_boolean"));
    assertFalse(indexer.checkType("nope"));
    assertFalse(indexer.checkType("c2mon_1970-1"));
  }

  @Test
  @Ignore("Works alone but not along the other tests (bulk processing synchronization)")
  public void testWriteTag() {
    TagES tag = new TagString();
    tag.setDataType(Mapping.ValueType.stringType.toString());
    tag.setMapping(Mapping.ValueType.stringType);
    tag.setId(1L);
    tag.setServerTimestamp(123456789000L);
    tag.setValue("test");

    assertNull(tag.getValueNumeric());
    assertNull(tag.getValueBoolean());
    assertEquals("test", tag.getValue());
    assertEquals("test", tag.getValueString());

    indexer.writeTag(tag);
    connector.closeBulk();

    assertTrue(indexer.getIndices().contains(indexer.generateIndex(123456789000L)));
    assertTrue(indexer.getTypes().contains(indexer.generateType(tag.getDataType())));

    QueryIndices query = new QueryIndices(connector.getClient());
    QueryTypes queryTypes = new QueryTypes(connector.getClient());

    assertTrue(connector.handleListingQuery(query).contains(indexer.generateIndex(123456789000L)));
    assertTrue(connector.handleListingQuery(queryTypes).contains(indexer.generateType(tag.getDataType())));
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
      tag.setDataType(Mapping.ValueType.stringType.toString());
      tag.setMapping(Mapping.ValueType.stringType);
      tag.setId(id);
      tag.setServerTimestamp(tagServerTime);
      list.add(tag);
      listIndices.add(indexer.generateIndex(tag.getServerTimestamp()));
      listAliases.add(indexer.generateAliasName(tag.getId()));
    }

    indexer.indexTags(list);

    Set<String> resultIndices = indexer.getIndices();
    Set<String> resultAliases = indexer.getAliases();
    Set<String> resultTypes = indexer.getTypes();

    QueryIndices queryIndices = new QueryIndices(connector.getClient());
    QueryTypes queryTypes = new QueryTypes(connector.getClient());
    QueryAliases queryAliases = new QueryAliases(connector.getClient());

    List<String> liveIndices = connector.handleListingQuery(queryIndices);
    List<String> liveTypes = connector.handleListingQuery(queryTypes);
    List<String> liveAliases = connector.handleListingQuery(queryAliases);


    SearchResponse response = getResponse(connector.getClient(), new String[]{"c2mon_1973-11"}, 0, 10, null);
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

  @Test
  public void testAddBulkAlias() {
    assertTrue(indexer.checkAlias("tag_1"));
    assertTrue(indexer.checkAlias("tag_114564894132185"));
    assertFalse(indexer.checkAlias("tag-114564894132185"));
    assertFalse(indexer.checkAlias("c2mon-114564894132185"));
    assertFalse(indexer.checkAlias("tag_lalala"));
    assertFalse(indexer.checkAlias("tag_45678.2"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadBulkAddAlias() {
    indexer.addAliasFromBatch("badIndex", null);
  }

  @Test
  public void testBulkAddAlias() {
    TagES tag = new TagString();
    tag.setId(1L);
    tag.setDataType(Mapping.ValueType.stringType.toString());
    tag.setMapping(Mapping.ValueType.stringType);
    indexer.getAliases().add("tag_1");
    connector.handleIndexQuery("c2mon_2015-12", connector.getIndexSettings("INDEX_MONTH_SETTINGS"),
        indexer.generateType(tag.getDataType()), tag.getMapping());

    // already contains the alias
    assertFalse(indexer.addAliasFromBatch("c2mon_2015-12", tag));

    indexer.getAliases().clear();
    // indices do not contain "c2mon_2015-12"
    assertFalse(indexer.addAliasFromBatch("c2mon_2015-12", tag));

    indexer.getIndices().add("c2mon_2015-12");
    assertTrue(indexer.addAliasFromBatch("c2mon_2015-12", tag));
    assertTrue(indexer.getAliases().contains("tag_1"));
  }

  @Test
  public void testBadIndexByBatch() throws IOException {
    TagES tag = new TagString();
    tag.setDataType(Mapping.ValueType.stringType.toString());
    tag.setId(1L);
    tag.setMapping(Mapping.ValueType.stringType);

    assertFalse(indexer.indexByBatch(null, tag.getDataType(), tag.build(), tag));
    assertFalse(indexer.indexByBatch("c2mon_2015-12", "badType", tag.build(), tag));
    assertFalse(indexer.getIndices().contains("c2mon_2015-12"));

    assertTrue(indexer.indexByBatch("c2mon_2015-12", indexer.generateType(tag.getDataType()), tag.build(), tag));
    connector.closeBulk();
    assertTrue(indexer.getIndices().size() == 1);
    assertTrue(indexer.getIndices().contains("c2mon_2015-12"));
  }

  private SearchResponse getResponse(Client client, String[] indices, int from, int size, List<Long> tagIds) {
    SearchRequestBuilder requestBuilder = client.prepareSearch();
    requestBuilder.setSearchType(SearchType.DEFAULT)
        .setIndices(indices)
        .setFrom(from)
        .setSize(size);

    if (tagIds != null) {
      requestBuilder.setQuery(QueryBuilders.boolQuery()
          .filter(QueryBuilders.termsQuery("id", tagIds)));
    }

    return requestBuilder.execute().actionGet();
  }

  private void sleep() {
    try {
      Thread.sleep(2000L);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}