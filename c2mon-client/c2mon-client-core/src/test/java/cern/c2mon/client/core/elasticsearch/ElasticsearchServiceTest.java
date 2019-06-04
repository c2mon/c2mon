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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.annotation.NotThreadSafe;
import org.awaitility.Awaitility;
import org.easymock.Mock;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.node.NodeValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.elasticsearch.Indices;
import cern.c2mon.server.elasticsearch.MappingFactory;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClientImpl;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentConverter;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentIndexer;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentListener;
import cern.c2mon.shared.client.configuration.ConfigConstants;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NotThreadSafe
@Slf4j
public class ElasticsearchServiceTest {

  private ElasticsearchClientImpl client;
  private TagConfigDocumentListener tagDocumentListener;
  private C2monClientProperties properties = new C2monClientProperties();
  private static ElasticsearchProperties elasticsearchProperties = new ElasticsearchProperties();
  @Mock
  private TagFacadeGateway tagFacadeGateway;

  public ElasticsearchServiceTest() throws NodeValidationException {
    this.client = new ElasticsearchClientImpl(elasticsearchProperties);
    Indices mustBeCreatedButVariableNotUsed = new Indices(this.client, elasticsearchProperties);
    TagConfigDocumentIndexer indexer = new TagConfigDocumentIndexer(client, elasticsearchProperties);
    ProcessCache processCache = createNiceMock(ProcessCache.class);
    EquipmentCache equipmentCache = createNiceMock(EquipmentCache.class);
    SubEquipmentCache subequipmentCache = createNiceMock(SubEquipmentCache.class);
    TagConfigDocumentConverter converter = new TagConfigDocumentConverter(processCache, equipmentCache, subequipmentCache);
    tagFacadeGateway = createNiceMock(TagFacadeGateway.class);
    tagDocumentListener = new TagConfigDocumentListener(this.client, indexer, converter, tagFacadeGateway);
  }

  @BeforeClass
  public static void cleanup() {
    log.debug("Clean ES properties");
    FileSystemUtils.deleteRecursively(new java.io.File(elasticsearchProperties.getEmbeddedStoragePath()));
  }

  @Before
  public void setupElasticsearch() throws InterruptedException, NodeValidationException {
    try {
      log.debug("Setup Elasticsearch cluster for testing...");
      CompletableFuture<Void> nodeReady = CompletableFuture.runAsync(() -> {
        client.waitForYellowStatus();
        client.getClient().admin().indices().delete(new DeleteIndexRequest(elasticsearchProperties.getTagConfigIndex()));
        Awaitility.await().until(() -> Indices.create(elasticsearchProperties.getTagConfigIndex(), "tag_config", MappingFactory.createTagConfigMapping()));
      });
      nodeReady.get(120, TimeUnit.SECONDS);
      log.debug("Elasticseach cluster ready for testing!");
    } catch (ExecutionException | TimeoutException e) {
      throw new RuntimeException("Timeout when waiting for embedded elasticsearch node to start!");
    }
  }

  @After
  public void tearDown() {
    log.debug("Closing ES client...");
    client.close();
  }

  @Before
  public void resetMocks() {
    reset(tagFacadeGateway);
  }

  @Test
  public void testSearchByMetadata() throws InterruptedException {
    try {
      Long testUserTagId = Double.doubleToLongBits(Math.random()) % 10000;
      String testUser = Long.toHexString(Double.doubleToLongBits(Math.random()));
      String responsible = "responsible";
      DataTagCacheObject tag = new DataTagCacheObject(testUserTagId);
      tag.getMetadata().getMetadata().put(responsible, testUser);
      expect(tagFacadeGateway.getAlarms(anyObject(DataTagCacheObject.class))).andReturn(Collections.emptyList()).times(2);
      replay(tagFacadeGateway);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);
      Long tag1234Id = Double.doubleToLongBits(Math.random()) % 10000;
      String value1234 = "1234";
      tag = new DataTagCacheObject(tag1234Id);
      String key1234 = "1234";
      tag.getMetadata().getMetadata().put(key1234, value1234);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      client.getClient().admin().indices().flush(new FlushRequest()).actionGet();

      ElasticsearchService service = new ElasticsearchService(properties);

      Awaitility.await().until(() -> service.getDistinctMetadataKeys().size() > 0);

      assertEquals("There should be 2 tags, one for responsible and one for 1234", 2, service.getDistinctMetadataKeys().size());

      Collection<Long> tagsForResponsibleUser = service.findByMetadata(responsible, testUser);
      assertEquals("There should be one tag with responsible user set to requested value", 1, tagsForResponsibleUser.size());
      assertEquals(testUserTagId, tagsForResponsibleUser.stream().findFirst().get());

      Collection<Long> tags1234 = service.findByMetadata(key1234, value1234);
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
      expect(tagFacadeGateway.getAlarms(anyObject(DataTagCacheObject.class))).andReturn(Collections.emptyList()).times(3);
      replay(tagFacadeGateway);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      tag = new DataTagCacheObject(Double.doubleToLongBits(Math.random()) % 10000);
      tag.setName(tagname);
      tag.getMetadata().getMetadata().put(metadataKey, "some other metadata value");
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      tag = new DataTagCacheObject(Double.doubleToLongBits(Math.random()) % 10000);
      tag.setName("other_tagname");
      tag.getMetadata().getMetadata().put(metadataKey, testUser);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      client.getClient().admin().indices().flush(new FlushRequest()).actionGet();

      ElasticsearchService service = new ElasticsearchService(properties);

      Awaitility.await().until(() -> service.findTagsByNameAndMetadata(tagname, metadataKey, testUser).size() > 0);

      Collection<Long> tagsForResponsibleUser = service.findTagsByNameAndMetadata(tagname, metadataKey, testUser);
      assertEquals("There should be one tag with given name and metadata", 1, tagsForResponsibleUser.size());
      assertEquals(testUserTagId, tagsForResponsibleUser.stream().findFirst().get());
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  @Test
  public void testSearchByName() throws InterruptedException {
    try {
      ElasticsearchService service = new ElasticsearchService(properties);
      Collection<Long> tagsForResponsibleUser = service.findByName("TEST");
      assertNotNull("The tags collection should not be null", tagsForResponsibleUser);
      assertEquals("There tags collection should be empty", 0, tagsForResponsibleUser.size());
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}