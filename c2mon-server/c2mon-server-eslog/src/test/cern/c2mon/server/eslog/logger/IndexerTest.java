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

import cern.c2mon.server.eslog.structure.mappings.Mapping;
import cern.c2mon.server.eslog.structure.mappings.TagStringMapping;
import cern.c2mon.server.eslog.structure.queries.QueryAliases;
import cern.c2mon.server.eslog.structure.queries.QueryIndices;
import cern.c2mon.server.eslog.structure.queries.QueryTypes;
import cern.c2mon.server.eslog.structure.types.TagBoolean;
import cern.c2mon.server.eslog.structure.types.TagES;
import cern.c2mon.server.eslog.structure.types.TagString;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the Indexer methods for sending the right data to the Connector.
 * @author Alban Marguet.
 */
@Slf4j
@ContextConfiguration({"classpath:cern/c2mon/server/eslog/config/server-eslog-integration.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class IndexerTest {
  private static String clusterName;
  private static String nodeName;
  private static String host;
  private static String home;
  private static Client clusterClient;
  private static Client initClient;
  @Autowired
  private TagIndexer indexer;
  @Autowired
  private TransportConnector connector;

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
    log.info("@After");
    connector.getClient().admin().indices().delete(new DeleteIndexRequest("*")).actionGet();
    connector.getClient().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
    connector.getClient().admin().indices().prepareRefresh().execute().actionGet();
    indexer.getIndicesTypes().clear();
    indexer.getIndicesAliases().clear();
  }

  @Test
  public void testInit() {
    assertTrue(indexer.isAvailable());
    assertNotNull(indexer.getIndicesTypes());
    assertNotNull(indexer.getIndicesAliases());
  }

  @Test
  public void testInstantiateIndex() {
    String index = "c2mon-tag_2015-02";

    boolean isAcked = indexer.instantiateIndex(index);
    assertTrue(isAcked);
    assertTrue(connector.getClient().admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists());
  }

  @Test
  public void testInstantiateType() {
    String index = "c2mon-tag_2015-02";
    String type = "tag_string";

    testInstantiateIndex();
    boolean isAcked = indexer.instantiateType(index, type);
    assertTrue(isAcked);
    assertTrue(connector.getClient().admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists());
    assertTrue(connector.getClient().admin().indices().typesExists(new TypesExistsRequest(new String[]{index}, type)).actionGet().isExists());
  }

  @Test
  public void testUpdateLists() {
    Set<String> expectedIndex = new HashSet<>();
    Set<String> expectedType = new HashSet<>();
    assertEquals(expectedIndex, indexer.getIndicesTypes().keySet());
    assertNull(indexer.getIndicesTypes().get("c2mon-tag_2015-01"));
    expectedIndex.add("c2mon-tag_2015-01");

    connector.handleIndexQuery("c2mon-tag_2015-01", null, null);
    indexer.updateLists();

    assertEquals(expectedIndex, indexer.getIndicesTypes().keySet());

    connector.handleIndexQuery("c2mon-tag_2015-01", "tag_string", new TagStringMapping(Mapping.ValueType.stringType).getMapping());
    assertEquals(expectedType, indexer.getIndicesTypes().get("c2mon-tag_2015-01"));
  }

  @Test
  public void testGenerateAliasName() {
    String expected = indexer.getTypePrefix() + "1";
    String value = indexer.generateAliasName(1);
    assertEquals(expected, value);
  }

  @Test
  public void testGenerateType() {
    String expected = indexer.getTypePrefix() + "string";
    TagES tag = new TagString();
    tag.setDataType("String");
    String value = indexer.generateTagType(tag.getDataType());
    assertEquals(expected, value);
  }

  @Test
  public void testGenerateIndex() {
    String expected = indexer.getIndexPrefix() + indexer.millisecondsToYearMonth(123456L);
    TagES tag = new TagBoolean();
    tag.setServerTimestamp(123456L);
    String value = indexer.generateTagIndex(tag.getServerTimestamp());
    assertEquals(expected, value);
  }

  @Test
  public void testMillisecondsToYearMonth() {
    String expected = "2015-12";
    String value = indexer.millisecondsToYearMonth(1448928000000L);
    assertEquals(expected, value);
  }

  @Test
  public void testMillisecondsToYearWeek() {
    String expected = "2015-49";
    String value = indexer.millisecondsToYearWeek(1448928000000L);
    assertEquals(expected, value);
  }

  @Test
  public void testMillisecondsToYearMonthDay() {
    String expected = "2015-12-01";
    String value = indexer.millisecondsToYearMonthDay(1448928000000L);
    assertEquals(expected, value);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBadIndex() {
    String index = "c2mon-tag_bad";
    indexer.addIndex(index);
  }

  @Test
  public void testAddGoodIndex() {
    String index = "c2mon-tag_2005-06";
    Set<String> expected = new HashSet<>();
    expected.add(index);
    indexer.addIndex(index);
    assertEquals(expected, indexer.getIndicesTypes().keySet());
    indexer.getIndicesTypes().clear();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBadType() {
    String index = "c2mon-tag_bad";
    String badType = "tagff_test";
    indexer.addType(index, badType);
  }

  @Test
  public void testAddGoodType() {
    String index = "c2mon-tag_1970-01";
    String type = "tag_string";
    Set<String> expected = new HashSet<>();
    expected.add(type);
    indexer.addIndex(index);
    indexer.addType(index, type);
    assertEquals(expected, indexer.getIndicesTypes().get(index));
    indexer.getIndicesTypes().clear();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBadAlias() {
    String index = "tag_bad";
    indexer.addIndex(index);
  }

  @Test
  public void testAddGoodAlias() {
    String index = "c2mon-tag_1970-01";
    String alias = "tag_12658";
    Set<String> expected = new HashSet<>();
    expected.add(alias);
    indexer.addIndex(index);
    indexer.addAlias(index, alias);
    assertEquals(expected, indexer.getIndicesAliases().get(index));
    indexer.getIndicesAliases().clear();
  }

  @Test
  public void testCheckIndex() {
    log.debug("IndexFormat " + indexer.getIndexFormat());
    log.debug("IndexPrefix " +indexer.getIndexPrefix());
    assertTrue(indexer.checkIndex("c2mon-tag_1234-69"));
    assertFalse(indexer.checkIndex("c2mon-tag-1234-69"));
    assertFalse(indexer.checkIndex("c2mon-tag_1234.69"));
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
    assertFalse(indexer.checkType("c2mon-tag_1970-1"));
  }

  @Test
  public void testSendTagToBatch() {
    TagES tag = new TagBoolean();
    tag.setDataType(Mapping.ValueType.boolType.toString());
    tag.setId(1L);
    tag.setServerTimestamp(123456789000L);
    tag.setValue(true);

    String indexName = indexer.generateTagIndex(tag.getServerTimestamp());

    assertEquals(1, tag.getValueNumeric());
    assertTrue(tag.getValueBoolean());
    assertNull(tag.getValueString());

    indexer.sendTagToBatch(tag);
    connector.closeBulk();

    assertTrue(indexer.getIndicesTypes().keySet().contains(indexer.generateTagIndex(123456789000L)));
    assertTrue(indexer.getIndicesTypes().get(indexName).contains(indexer.generateTagType(tag.getDataType())));

    QueryIndices query = new QueryIndices(connector.getClient());
    QueryTypes queryTypes = new QueryTypes(connector.getClient());

    assertTrue(connector.handleListingQuery(query, indexName).contains(indexer.generateTagIndex(123456789000L)));
    assertTrue(connector.handleListingQuery(queryTypes, indexName).contains(indexer.generateTagType(tag.getDataType())));
  }

  @Test
  public void testIndexTags() {
    long size = 10;
    List<TagES> list = new ArrayList<>();
    Set<String> listIndices = new HashSet<>();
    Set<String> listAliases = new HashSet<>();
    long id = 1L;
    long tagServerTime = 123456789000L;
    Map<String, String> metadata = new HashMap<>();
    metadata.put("test1", "value1");
    metadata.put("test2", "2");

    for (; id <= size; id++, tagServerTime += 1000) {
      TagES tag = new TagString();
      tag.setDataType(Mapping.ValueType.stringType.toString());
      tag.setId(id);
      tag.setServerTimestamp(tagServerTime);
      list.add(tag);
      listIndices.add(indexer.generateTagIndex(tag.getServerTimestamp()));
      listAliases.add(indexer.generateAliasName(tag.getId()));
      tag.setMetadata(metadata);
    }


    String indexName = indexer.generateTagIndex(tagServerTime);
    indexer.indexTags(list);
    sleep();
    assertTrue(connector.waitForYellowStatus());

    Set<String> resultIndices = indexer.getIndicesTypes().keySet();
    Set<String> resultAliases = indexer.getIndicesAliases().get(indexName);
    Set<String> resultTypes = indexer.getIndicesTypes().get(indexName);

    QueryIndices queryIndices = new QueryIndices(connector.getClient());
    QueryTypes queryTypes = new QueryTypes(connector.getClient());
    QueryAliases queryAliases = new QueryAliases(connector.getClient());

    List<String> liveIndices = connector.handleListingQuery(queryIndices, indexName);
    List<String> liveTypes = connector.handleListingQuery(queryTypes, indexName);
    List<String> liveAliases = connector.handleListingQuery(queryAliases, indexName);


    SearchResponse response = getResponse(connector.getClient(), new String[]{indexName});

    log.debug(response.toString());
    log.debug("size: " + size);
    log.debug("response: " + response.getHits().getTotalHits());

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
    String index = "c2mon-tag_2015-12";

    indexer.getIndicesAliases().put(index, new HashSet<String>());
    indexer.getIndicesAliases().get(index).add("tag_1");
    connector.getClient().admin().indices().prepareCreate(index).execute().actionGet();
    connector.handleIndexQuery(index, indexer.generateTagType(tag.getDataType()), new TagStringMapping(Mapping.ValueType.stringType).getMapping());

    // already contains the alias
    assertFalse(indexer.addAliasFromBatch(index, tag));

    indexer.getIndicesAliases().clear();
    // indices do not contain "c2mon_2015-12"
    assertFalse(indexer.addAliasFromBatch(index, tag));

    indexer.getIndicesAliases().put(index, new HashSet<String>());
    assertTrue(indexer.addAliasFromBatch(index, tag));
    //normal behavior in the program, easier tests
    indexer.addAlias(index, "tag_1");
    assertTrue(indexer.getIndicesAliases().get(index).contains("tag_1"));
  }

  @Test
  public void testIndexByBatch() throws IOException {
    TagES tag = new TagString();
    tag.setDataType(Mapping.ValueType.stringType.toString());
    tag.setId(1L);
    String type = indexer.generateTagType(tag.getDataType());

    assertFalse(indexer.indexByBatch(null, tag.getDataType(), tag.build(), tag));
    assertFalse(indexer.indexByBatch("c2mon-tag_2015-12", "badType", tag.build(), tag));
    assertNull(indexer.getIndicesTypes().get("c2mon-tag_2015-12"));

    assertTrue(indexer.indexByBatch("c2mon-tag_2015-12", type, tag.build(), tag));
    connector.closeBulk();
    assertTrue(indexer.getIndicesTypes().size() == 1);
    assertTrue(indexer.getIndicesTypes().get("c2mon-tag_2015-12").contains(type));
  }

  @Test
  public void testRetrieveIndexFormat() {
    long millis = 123456789;
    String expectedMonth = indexer.millisecondsToYearMonth(millis);
    String expectedWeek = indexer.millisecondsToYearWeek(millis);
    String expectedDay = indexer.millisecondsToYearMonthDay(millis);

    indexer.setIndexFormat("M");
    String monthIndex = indexer.retrieveIndexFormat(indexer.indexPrefix, millis);
    assertEquals(indexer.indexPrefix + expectedMonth, monthIndex);

    indexer.setIndexFormat("W");
    String weekIndex = indexer.retrieveIndexFormat(indexer.indexPrefix, millis);
    assertEquals(indexer.indexPrefix + expectedWeek, weekIndex);

    indexer.setIndexFormat("D");
    String dayIndex = indexer.retrieveIndexFormat(indexer.indexPrefix, millis);
    assertEquals(indexer.indexPrefix + expectedDay, dayIndex);
  }

  private SearchResponse getResponse(Client client, String[] indices) {
    return client.prepareSearch(indices).setSearchType(SearchType.DEFAULT).execute().actionGet();
  }

  private void sleep() {
    try {
      Thread.sleep(3000L);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}