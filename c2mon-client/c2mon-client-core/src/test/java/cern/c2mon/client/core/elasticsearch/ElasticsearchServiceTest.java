/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.client.core.elasticsearch;

import cern.c2mon.cache.actions.alarm.AlarmService;
import cern.c2mon.cache.api.impl.SimpleCache;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.elasticsearch.IndexManager;
import cern.c2mon.server.elasticsearch.MappingFactory;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClientRest;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.domain.IndexMetadata;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentConverter;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentIndexer;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentListener;
import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import org.apache.http.annotation.NotThreadSafe;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NotThreadSafe
public class ElasticsearchServiceTest {

  private static ElasticsearchProperties elasticsearchProperties = new ElasticsearchProperties();

  private TagConfigDocumentListener tagDocumentListener;
  private C2monClientProperties properties = new C2monClientProperties();
  private ElasticsearchClientRest client;
  private IndexManager indexManager;

  @Mock
  private AlarmService alarmService;

  public ElasticsearchServiceTest() {
    client = new ElasticsearchClientRest(elasticsearchProperties);
    indexManager = new IndexManager(client);
    TagConfigDocumentIndexer indexer = new TagConfigDocumentIndexer(elasticsearchProperties, indexManager, null, null, null);
    TagConfigDocumentConverter converter = new TagConfigDocumentConverter(
      new SimpleCache<>("procCache"),
      new SimpleCache<>("eqCache"),
      new SimpleCache<>("subEqCache")
    );
    TagCacheCollection tagCacheFacade = new TagCacheCollection(new SimpleCache<>("rule"),
      new SimpleCache<>("data"), new SimpleCache<>("alive"), new SimpleCache<>("cFault"),
      new SimpleCache<>("state"));
    alarmService = EasyMock.createNiceMock(AlarmService.class);
    tagDocumentListener = new TagConfigDocumentListener(elasticsearchProperties, indexer, converter, tagCacheFacade, alarmService);
  }

  @BeforeClass
  public static void setUpClass() {
    EmbeddedElasticsearchManager.start(elasticsearchProperties);
  }

  @AfterClass
  public static void tearDownClass() {
    EmbeddedElasticsearchManager.stop();
  }

  @Before
  public void setupElasticsearch() throws InterruptedException {
    try {
      CompletableFuture<Void> nodeReady = CompletableFuture.runAsync(() -> {
        EmbeddedElasticsearchManager.getEmbeddedNode().deleteIndex(elasticsearchProperties.getTagConfigIndex());
        indexManager.create(IndexMetadata.builder().name(elasticsearchProperties.getTagConfigIndex()).build(),
            MappingFactory.createTagConfigMapping());
        try {
          Thread.sleep(1000); //it takes some time for the index to be recreated
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
      nodeReady.get(120, TimeUnit.SECONDS);
    } catch (ExecutionException | TimeoutException e) {
      throw new RuntimeException("Timeout when waiting for embedded elasticsearch node to start!");
    }
  }

  @Before
  public void resetMocks() {
    reset(alarmService);
  }

  @Test
  public void testSearchByMetadata() throws InterruptedException {
    try {
      Long testUserTagId = Double.doubleToLongBits(Math.random()) % 10000;
      String testUser = Long.toHexString(Double.doubleToLongBits(Math.random()));
      String responsible = "responsible";
      DataTagCacheObject tag = new DataTagCacheObject(testUserTagId);
      tag.getMetadata().getMetadata().put(responsible, testUser);
      expect(alarmService.getTagWithAlarmsAtomically(anyLong())).andReturn(new TagWithAlarms<>(tag,Collections.emptyList())).times(2);
      replay(alarmService);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);
      Long tag1234Id = Double.doubleToLongBits(Math.random()) % 10000;
      String value1234 = "1234";
      tag = new DataTagCacheObject(tag1234Id);
      String key1234 = "1234";
      tag.getMetadata().getMetadata().put(key1234, value1234);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();
      Thread.sleep(10000);

      ElasticsearchService service = new ElasticsearchService(properties, "c2mon");

      assertEquals("There should be 2 tags, one for responsible and one for 1234", 2, service.getDistinctTagMetadataKeys().size());

      Collection<Long> tagsForResponsibleUser = service.findTagsByMetadata(responsible, testUser);
      assertEquals("There should be one tag with responsible user set to requested value", 1, tagsForResponsibleUser.size());
      assertEquals(testUserTagId, tagsForResponsibleUser.stream().findFirst().get());

      Collection<Long> tags1234 = service.findTagsByMetadata(key1234, value1234);
      assertEquals("There should be one tag with 1234 parameter set to requested value", 1, tags1234.size());
      assertEquals(tag1234Id, tags1234.stream().findFirst().get());
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  @Test
  public void testSearchByNameAndMetadata() throws InterruptedException {
    try {
      Long testUserTagId = Double.doubleToLongBits(Math.random()) % 10000;
      String testUser = Long.toHexString(Double.doubleToLongBits(Math.random()));
      String metadataKey = "metadataKey";
      DataTagCacheObject tag = new DataTagCacheObject(testUserTagId);
      String tagname = "tagname";
      tag.setName(tagname);
      tag.getMetadata().getMetadata().put(metadataKey, testUser);
      expect(alarmService.getTagWithAlarmsAtomically(anyLong())).andReturn(new TagWithAlarms<>(tag,Collections.emptyList())).times(3);
      replay(alarmService);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      tag = new DataTagCacheObject(Double.doubleToLongBits(Math.random()) % 10000);
      tag.setName(tagname);
      tag.getMetadata().getMetadata().put(metadataKey, "some other metadata value");
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      tag = new DataTagCacheObject(Double.doubleToLongBits(Math.random()) % 10000);
      tag.setName("other_tagname");
      tag.getMetadata().getMetadata().put(metadataKey, testUser);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();
      Thread.sleep(10000);

      ElasticsearchService service = new ElasticsearchService(properties, "c2mon");

      Collection<Long> tagsForResponsibleUser = service.findTagsByNameAndMetadata(tagname, metadataKey, testUser);
      assertEquals("There should be one tag with given name and metadata", 1, tagsForResponsibleUser.size());
      assertEquals(testUserTagId, tagsForResponsibleUser.stream().findFirst().get());
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  @Test
  public void testSearchByName() {
    try {
      ElasticsearchService service = new ElasticsearchService(properties, "c2mon");
      Collection<Long> tagsForResponsibleUser = service.findTagsByName("TEST");
      assertNotNull("The tags collection should not be null", tagsForResponsibleUser);
      assertEquals("There tags collection should be empty", 0, tagsForResponsibleUser.size());
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}