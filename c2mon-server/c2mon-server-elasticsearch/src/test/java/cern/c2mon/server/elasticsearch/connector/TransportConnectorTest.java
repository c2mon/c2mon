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

import java.sql.Timestamp;

import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.structure.converter.EsAlarmLogConverter;
import cern.c2mon.server.elasticsearch.structure.converter.EsSupervisionEventConverter;
import cern.c2mon.server.elasticsearch.structure.mappings.EsAlarmMapping;
import cern.c2mon.server.elasticsearch.structure.mappings.EsTagMapping;
import cern.c2mon.server.elasticsearch.structure.types.EsAlarm;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTag;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.elasticsearch.structure.types.EsSupervisionEvent;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

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
    log.info("@After");
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
//    assertEquals(expectedSettings, connector.getSettings());
//    assertEquals(isLocal, connector.getSettings().get("node.local"));
//    assertEquals(connector.getNode(), connector.getSettings().get("node.name"));
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
    assertEquals(1, properties.getPort());
//    assertEquals(isLocal, connector.getSettings().get("node.local"));
  }

  @Test
  public void testCreateIndex() {
    Client initClient = connector.getClient();

    connector.setClient(initClient);
    // creating multiple times the same index should always return true
    for (int i=0; i <10; i++) {
      assertTrue(connector.createIndex("c2mon_2015-01"));
    }
  }

  @Test
  public void testCreateIndexTypeMapping() {
    Client initClient = connector.getClient();

    String index = "c2mon_2015-01";
    String type = "type_string";
    String mapping = new EsTagMapping(EsTag.TYPE_STRING, String.class.getName()).getMapping();

    connector.setClient(initClient);

    assertTrue(connector.createIndex(index));

    // creating multiple times the same mapping should always return true
    for (int i=0; i < 10; i++) {
      assertTrue(connector.createIndexTypeMapping(index, type, mapping));
    }
  }

  @Test
  public void testBulkAdd() {
    BulkProcessor initBulkProcessor = connector.getBulkProcessor();
    boolean result = connector.bulkAdd(null);
    assertFalse(result);

    result = connector.bulkAdd(null);
    assertFalse(result);

    connector.setBulkProcessor(initBulkProcessor);
    IndexRequest newIndex = new IndexRequest("c2mon-tag_1973-06", "tag_boolean").source("");
    result = connector.bulkAdd(newIndex);
    assertTrue(result);
  }

  @Test
  public void testLogAlarmEvent() {
    final String indexName = "index-test_alarm";
    EsAlarmLogConverter esAlarmLogConverter = new EsAlarmLogConverter();

    for (long i = 1; i <= 3; i++) {
      EsAlarm esAlarm = esAlarmLogConverter.convert(CacheObjectCreation.createTestAlarm1());
      esAlarm.setId(i);

      String mapping = new EsAlarmMapping().getMapping();
      assertTrue(connector.logAlarmEvent(indexName, mapping, esAlarm));
      connector.indexExists(indexName);

      sleep(2000);
      SearchResponse response = connector.getClient().prepareSearch(indexName).setTypes(TransportConnector.TYPE_ALARM).setSize(0).execute().actionGet();
      assertEquals(response.toString(), i, response.getHits().getTotalHits());
    }
  }

  @Test
  public void testLogSupervisionEvent() {
    final String indexName = "index-test_supervision";
    EsSupervisionEventConverter esSupervisionEventConverter = new EsSupervisionEventConverter();

    for (long i = 1; i <= 3; i++) {
      SupervisionEvent event = new SupervisionEventImpl(SupervisionConstants.SupervisionEntity.PROCESS,
          i,
          "P_PROCESS" + i,
          SupervisionConstants.SupervisionStatus.RUNNING,
          new Timestamp(123456789),
          "test message");
      EsSupervisionEvent esSupervisionEvent = esSupervisionEventConverter.convert(event);
      String mapping = new EsAlarmMapping().getMapping();

      assertTrue(connector.logSupervisionEvent(indexName, mapping, esSupervisionEvent));
      connector.indexExists(indexName);

      sleep(2000);
      SearchResponse response = connector.getClient().prepareSearch(indexName).setTypes(TransportConnector.TYPE_SUPERVISION).setSize(0).execute().actionGet();
      assertEquals(response.toString(), i, response.getHits().getTotalHits());
    }
  }

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
