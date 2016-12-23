///******************************************************************************
// * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
// *
// * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
// * C2MON is free software: you can redistribute it and/or modify it under the
// * terms of the GNU Lesser General Public License as published by the Free
// * Software Foundation, either version 3 of the license.
// *
// * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// * more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
// *****************************************************************************/
//package cern.c2mon.server.elasticsearch.indexer;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.*;
//
//import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
//import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
//import cern.c2mon.server.elasticsearch.connector.TransportConnector;
//import cern.c2mon.server.elasticsearch.structure.mappings.MappingFactory;
//import cern.c2mon.server.elasticsearch.structure.types.tag.EsTag;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.search.SearchType;
//import org.elasticsearch.client.Client;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
//
//import static junit.framework.TestCase.assertEquals;
//import static junit.framework.TestCase.assertNotNull;
//import static junit.framework.TestCase.assertNull;
//import static org.junit.Assert.assertTrue;
//
///**
// * Test the EsIndexer methods for sending the right data to the TransportConnector.
// *
// * @author Alban Marguet
// */
//@Slf4j
//public class EsIndexerTest extends BaseElasticsearchIntegrationTest {
//  private static Client clusterClient;
//  private static Client initClient;
//
//  private static final String TAG_INDEX_SUFFIX = "-tag_";
//
//  @Autowired
//  private EsTagIndexer<EsTag> indexer;
//
//  @Autowired
//  private TransportConnector connector;
//
//  @Autowired
//  private ElasticsearchProperties properties;
//
//  @Before
//  public void clientSetup() throws IOException {
//    delete();
//
//    if (connector.getClient() != null) {
//      initClient = connector.getClient();
//    }
//
//    connector.setClient(initClient);
//    clusterClient = connector.getClient();
//  }
//
//  @After
//  public void tidyUp() {
//    connector.getClient().admin().indices().delete(new DeleteIndexRequest("*")).actionGet();
//    connector.getClient().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
//    connector.getClient().admin().indices().prepareRefresh().execute().actionGet();
////    connector.closeBulk();
//  }
//
//  private void delete() {
//    log.info("begin delete directory");
//
//    File dataDir = new File(properties.getEmbeddedStoragePath() + "/data");
//    File[] data = dataDir.listFiles();
//
//    if (data != null) {
//      for (File file : data) {
//        file.delete();
//      }
//    }
//
//    log.info("end. Cleaned ./data directory");
//  }
//
//  @Test
//  public void testInit() {
//    assertTrue(indexer.isAvailable());
//    assertNotNull(indexer.getIndicesToTypesCache());
//  }
//
//  @Test
//  public void testCreateIndex() {
//    Client initClient = connector.getClient();
//
//    connector.setClient(initClient);
//    // creating multiple times the same index should always return true
//    for (int i=0; i <10; i++) {
//      assertTrue(connector.createIndex("c2mon_2015-01"));
//    }
//  }
//
//  @Test
//  public void testCreateIndexTypeMapping() {
//    Client initClient = connector.getClient();
//
//    String index = "c2mon_2015-01";
//    String type = "type_string";
//    String mapping = MappingFactory.createTagMapping(String.class.getName());
//
//    connector.setClient(initClient);
//
//    assertTrue(connector.createIndex(index));
//
//    // creating multiple times the same mapping should always return true
//    for (int i=0; i < 10; i++) {
//      assertTrue(connector.createIndexTypeMapping(index, type, mapping));
//    }
//  }
//
//  @Test
//  public void testUpdateLists() {
//    Set<String> expectedIndex = new HashSet<>();
//    Set<String> expectedType = new HashSet<>();
//    assertEquals(expectedIndex, indexer.getIndicesToTypesCache().keySet());
//    assertNull(indexer.getIndicesToTypesCache().get("c2mon-tag_2015-01"));
//    expectedIndex.add("c2mon-tag_2015-01");
//
//    assertTrue(indexer.createNonExistentIndex("c2mon-tag_2015-01"));
//
//    assertEquals(expectedIndex, indexer.getIndicesToTypesCache().keySet());
//
//    connector.createIndexTypeMapping("c2mon-tag_2015-01", "tag_string", MappingFactory.createTagMapping(String.class.getName()));
//    assertEquals(expectedType, indexer.getIndicesToTypesCache().get("c2mon-tag_2015-01"));
//  }
//
//  @Test
//  public void testMillisecondsToYearMonth() {
//    String expected = "2015-12";
//    String value = indexer.millisecondsToYearMonth(1448928000000L);
//    assertEquals(expected, value);
//  }
//
//  @Test
//  public void testMillisecondsToYearWeek() {
//    String expected = "2015-W49";
//    String value = indexer.millisecondsToYearWeek(1448928000000L);
//    assertEquals(expected, value);
//  }
//
//  @Test
//  public void testMillisecondsToYearMonthDay() {
//    String expected = "2015-12-01";
//    String value = indexer.millisecondsToYearMonthDay(1448928000000L);
//    assertEquals(expected, value);
//  }
//
//  @Test(expected = IllegalArgumentException.class)
//  public void testAddBadIndex() {
//    String index = "c2mon-tag_bad";
//    indexer.addIndex(index);
//  }
//
//  @Test
//  public void testAddGoodIndex() {
//    String index = "c2mon-tag_2005-06";
//    Set<String> expected = new HashSet<>();
//    expected.add(index);
//    indexer.addIndex(index);
//    assertEquals(expected, indexer.getIndicesToTypesCache().keySet());
//    indexer.getIndicesToTypesCache().clear();
//  }
//
//  @Test(expected = IllegalArgumentException.class)
//  public void testAddBadType() {
//    String index = "c2mon-tag_bad";
//    String badType = "tagff_test";
//    indexer.addType(index, badType);
//  }
//
//  @Test
//  public void testAddGoodType() {
//    String index = "c2mon-tag_1970-01";
//    String type = "tag_string";
//    Set<String> expected = new HashSet<>();
//    expected.add(type);
//    indexer.addIndex(index);
//    indexer.addType(index, type);
//    assertEquals(expected, indexer.getIndicesToTypesCache().get(index));
//    indexer.getIndicesToTypesCache().clear();
//  }
//
//  @Test(expected = IllegalArgumentException.class)
//  public void testAddBadAlias() {
//    String index = "tag_bad";
//    indexer.addIndex(index);
//  }
//
//  @Test
//  public void testAddGoodAlias() {
//    String index = "c2mon-tag_1970-01";
//    String alias = "tag_12658";
//    Set<String> expected = new HashSet<>();
//    expected.add(alias);
//    indexer.addIndex(index);
//  }
//
//  @Test
//  public void testIndexTags() throws IDBPersistenceException {
//    List<EsTag> list = new ArrayList<>();
//    EsTag tag = new EsTag(1L, Boolean.class.getName());
//
//    tag.getC2mon().setServerTimestamp(123456789000L);
//    tag.setRawValue(true);
//    String indexName = properties.getIndexPrefix() + TAG_INDEX_SUFFIX +indexer.millisecondsToYearMonth(tag.getC2mon().getServerTimestamp());
//    String typeName = "type_" + EsTagIndexer.getSimpleTypeName(tag.getC2mon().getDataType());
//
//    list.add(tag);
//
//    assertEquals(1, tag.getValue());
//    assertTrue(tag.getValueBoolean());
//    assertNull(tag.getValueString());
//
//    indexer.indexTags(list);
//
//    assertTrue(indexer.getIndicesToTypesCache().keySet().contains(indexName));
//    assertTrue(indexer.getIndicesToTypesCache().get(indexName).contains(typeName));
//
//    assertTrue(connector.retrieveIndices().contains(indexName));
//    assertTrue(connector.retrieveTypes(indexName).contains(typeName));
//
//    list.clear();
//    connector.getClient().admin().indices().delete(new DeleteIndexRequest("*")).actionGet();
//    indexer.clearCache();
//
//    long size = 10;
//
//    Set<String> listIndices = new HashSet<>();
//    long id = 1L;
//    long tagServerTime = 123456789000L;
//    Map<String, String> metadata1 = new HashMap<>();
//    Map<String, String> metadata2 = new HashMap<>();
//    metadata1.put("test1", "value1");
//    metadata2.put("test2", "2");
//
//    //not all tags have the same metadata and last tag has nothing
//    for (; id <= size; id++, tagServerTime += 1000) {
//      tag = new EsTag(id, String.class.getName());
//
//      tag.getC2mon().setServerTimestamp(tagServerTime);
//      list.add(tag);
//      listIndices.add(properties.getIndexPrefix() + TAG_INDEX_SUFFIX + indexer.millisecondsToYearMonth(tag.getC2mon().getServerTimestamp()));
//
//      if (id == size) {
//        log.debug("list of tags realized");
//      } else if (id % 2 == 0) {
//        tag.getMetadata().putAll(metadata1);
//      } else {
//        tag.getMetadata().putAll(metadata2);
//      }
//    }
//
//    indexName = properties.getIndexPrefix() + TAG_INDEX_SUFFIX + indexer.millisecondsToYearMonth(tagServerTime);
//    indexer.indexTags(list);
//    sleep();
//    connector.waitForYellowStatus();
//
//    Set<String> resultIndices = indexer.getIndicesToTypesCache().keySet();
//    Set<String> resultTypes = indexer.getIndicesToTypesCache().get(indexName);
//
//    List<String> liveIndices = connector.getListOfIndicesFromES();
//    Collection<String> liveTypes = connector.getTypesFromES(indexName);
//
//    SearchResponse response = getResponse(connector.getClient(), new String[]{indexName});
//
//    log.debug(response.toString());
//    log.debug("size: " + size);
//    log.debug("response: " + response.getHits().getTotalHits());
//
//    assertEquals(size, response.getHits().getTotalHits());
//    if (liveIndices.contains(".kibana")) {
//      assertEquals(resultIndices.size() + 1, liveIndices.size());
//    }
//    else {
//      assertEquals(Arrays.toString(liveIndices.toArray()), resultIndices.size(), liveIndices.size());
//    }
//
//    assertTrue(resultTypes.contains("type_string"));
//    assertTrue(resultIndices.containsAll(listIndices));
//
//    assertTrue(liveIndices.containsAll(resultIndices));
//    assertTrue(liveTypes.containsAll(resultTypes));
//  }
//
//  @Test
//  public void testRetrieveIndexFormat() {
//    long millis = 123456789;
//    String expectedMonth = indexer.millisecondsToYearMonth(millis).toLowerCase();
//    String expectedWeek = indexer.millisecondsToYearWeek(millis).toLowerCase();
//    String expectedDay = indexer.millisecondsToYearMonthDay(millis).toLowerCase();
//
//    properties.setIndexType("M");
//    String monthIndex = indexer.getOrCreateIndex(millis);
//    assertEquals(properties.getIndexPrefix() + "-tag_" + expectedMonth, monthIndex);
//
//    properties.setIndexType("W");
//    String weekIndex = indexer.getOrCreateIndex(millis);
//    assertEquals(properties.getIndexPrefix() + "-tag_" + expectedWeek, weekIndex);
//
//    properties.setIndexType("D");
//    String dayIndex = indexer.getOrCreateIndex(millis);
//    assertEquals(properties.getIndexPrefix() + "-tag_" + expectedDay, dayIndex);
//  }
//
//  private SearchResponse getResponse(Client client, String[] indices) {
//    return client.prepareSearch(indices).setSearchType(SearchType.DEFAULT).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
//  }
//
//  private void sleep() {
//    try {
//      Thread.sleep(3000L);
//    } catch(InterruptedException e) {
//      e.printStackTrace();
//    }
//  }
//}
