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
package cern.c2mon.server.eslog.indexer;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.eslog.connector.TransportConnector;
import cern.c2mon.server.eslog.structure.mappings.EsMapping;
import cern.c2mon.server.eslog.structure.mappings.EsStringTagMapping;
import cern.c2mon.server.eslog.structure.types.EsTagBoolean;
import cern.c2mon.server.eslog.structure.types.EsTagImpl;
import cern.c2mon.server.eslog.structure.types.EsTagString;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
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
 * Test the EsIndexer methods for sending the right data to the Connector.
 * @author Alban Marguet.
 */
@Slf4j
@ContextConfiguration({"classpath:cern/c2mon/server/eslog/config/server-eslog-integration.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class EsIndexerTest {
  private static String clusterName;
  private static String nodeName;
  private static String host;
  private static String home;
  private static Client clusterClient;
  private static Client initClient;
  @Autowired
  private EsTagIndexer indexer;
  @Autowired
  private TransportConnector connector;

  @ClassRule
  public static TemporaryFolder c2monHome = new TemporaryFolder();

  @BeforeClass
  public static void setEnv() {
    System.setProperty("c2mon.home", c2monHome.toString());
  }

  @AfterClass
  public static void cleanEnv() {
    System.clearProperty("c2mon.home");
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
    indexer.getCacheIndicesTypes().clear();
    indexer.getCacheIndicesAliases().clear();
//    connector.closeBulk();
  }

  @Test
  public void testInit() {
    assertTrue(indexer.isAvailable());
    assertNotNull(indexer.getCacheIndicesTypes());
    assertNotNull(indexer.getCacheIndicesAliases());
  }

  @Test
  public void testUpdateLists() {
    Set<String> expectedIndex = new HashSet<>();
    Set<String> expectedType = new HashSet<>();
    assertEquals(expectedIndex, indexer.getCacheIndicesTypes().keySet());
    assertNull(indexer.getCacheIndicesTypes().get("c2mon-tag_2015-01"));
    expectedIndex.add("c2mon-tag_2015-01");

    connector.handleIndexQuery("c2mon-tag_2015-01", null, null);
    indexer.updateCache();

    assertEquals(expectedIndex, indexer.getCacheIndicesTypes().keySet());

    connector.handleIndexQuery("c2mon-tag_2015-01", "tag_string", new EsStringTagMapping(EsMapping.ValueType.stringType).getMapping());
    assertEquals(expectedType, indexer.getCacheIndicesTypes().get("c2mon-tag_2015-01"));
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
    assertEquals(expected, indexer.getCacheIndicesTypes().keySet());
    indexer.getCacheIndicesTypes().clear();
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
    assertEquals(expected, indexer.getCacheIndicesTypes().get(index));
    indexer.getCacheIndicesTypes().clear();
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
    assertEquals(expected, indexer.getCacheIndicesAliases().get(index));
    indexer.getCacheIndicesAliases().clear();
  }

  @Test
  public void testIndexTags() throws IDBPersistenceException {
    List<EsTagImpl> list = new ArrayList<>();
    EsTagImpl tag = new EsTagBoolean();
    tag.setDataType(EsMapping.ValueType.boolType.toString());
    tag.setId(1L);
    tag.setServerTimestamp(123456789000L);
    tag.setValue(true);
    String indexName = indexer.indexPrefix + indexer.millisecondsToYearMonth(tag.getServerTimestamp());
    String typeName = indexer.typePrefix + tag.getDataType();

    list.add(tag);

    assertEquals(1, tag.getValueNumeric());
    assertTrue(tag.getValueBoolean());
    assertNull(tag.getValueString());

    indexer.indexTags(list);

    assertTrue(indexer.getCacheIndicesTypes().keySet().contains(indexName));
    assertTrue(indexer.getCacheIndicesTypes().get(indexName).contains(typeName));

    assertTrue(connector.retrieveIndicesFromES().contains(indexName));
    assertTrue(connector.retrieveTypesFromES(indexName).contains(typeName));

    list.clear();
    connector.getClient().admin().indices().delete(new DeleteIndexRequest("*")).actionGet();
    indexer.updateCache();

    long size = 10;

    Set<String> listIndices = new HashSet<>();
    Set<String> listAliases = new HashSet<>();
    long id = 1L;
    long tagServerTime = 123456789000L;
    Map<String, String> metadata1 = new HashMap<>();
    Map<String, String> metadata2 = new HashMap<>();
    metadata1.put("test1", "value1");
    metadata2.put("test2", "2");

    //not all tags have the same metadata and last tag has nothing
    for (; id <= size; id++, tagServerTime += 1000) {
      tag = new EsTagString();
      tag.setDataType(EsMapping.ValueType.stringType.toString());
      tag.setId(id);
      tag.setServerTimestamp(tagServerTime);
      list.add(tag);
      listIndices.add(indexer.indexPrefix + indexer.millisecondsToYearMonth(tag.getServerTimestamp()));
      listAliases.add(indexer.typePrefix + tag.getId());
      if (id == size) {
        log.debug("list of tags realized");
      }
      else if (id % 2 == 0) {
        tag.setMetadata(metadata1);
      }
      else {
        tag.setMetadata(metadata2);
      }
    }

    indexName = indexer.indexPrefix + indexer.millisecondsToYearMonth(tagServerTime);
    indexer.indexTags(list);
    sleep();
    assertTrue(connector.waitForYellowStatus());

    Set<String> resultIndices = indexer.getCacheIndicesTypes().keySet();
    Set<String> resultTypes = indexer.getCacheIndicesTypes().get(indexName);
    Set<String> resultAliases = indexer.getCacheIndicesAliases().get(indexName);

    List<String> liveIndices = connector.getListOfIndicesFromES();
    List<String> liveTypes = connector.getListOfTypesFromES(indexName);
    List<String> liveAliases = connector.getListOfAliasesFromES(indexName);

    for (String a : connector.getListOfAliasesFromES(indexName)) {
      log.debug(a);
    }


    SearchResponse response = getResponse(connector.getClient(), new String[]{indexName});

    log.debug(response.toString());
    log.debug("size: " + size);
    log.debug("response: " + response.getHits().getTotalHits());

    assertEquals(size, response.getHits().getTotalHits());
    assertTrue(resultIndices.size() == liveIndices.size());
    assertTrue(resultAliases.size() == size && liveAliases.size() == size);

    assertTrue(resultTypes.contains("tag_string") && resultTypes.size() == 1);
    assertTrue(resultIndices.containsAll(listIndices) && resultIndices.size() == listIndices.size());
    assertTrue(resultAliases.containsAll(listAliases) && resultAliases.size() == listAliases.size()) ;

    assertTrue(liveIndices.containsAll(resultIndices));
    assertTrue(liveTypes.containsAll(resultTypes));
    assertTrue(liveAliases.containsAll(resultAliases));
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
    return client.prepareSearch(indices).setSearchType(SearchType.DEFAULT).setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
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