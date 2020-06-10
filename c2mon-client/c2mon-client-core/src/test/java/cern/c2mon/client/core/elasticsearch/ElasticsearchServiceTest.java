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

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.actions.alarm.AlarmService;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.elasticsearch.IndexManager;
import cern.c2mon.server.elasticsearch.MappingFactory;
import cern.c2mon.server.elasticsearch.config.ElasticsearchModule;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.domain.IndexMetadata;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentConverter;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentIndexer;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentListener;
import cern.c2mon.server.elasticsearch.util.ContainerizedElasticsearchManager;
import cern.c2mon.server.elasticsearch.util.ElasticsearchTestClient;
import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Double.doubleToLongBits;
import static java.util.Collections.emptyList;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * When running in GitLab CI, these tests will connect to ElasticSearch's service
 * as configured in gitlab-ci.yml and c2mon-server-gitlab-ci.properties. For local
 * testing, an embedded ElasticSearch service is used by default, and can be
 * turned off by setting {@code c2mon.server.elasticsearch.embedded=false} below,
 * while ensuring Docker is running locally.
 */
// @TestPropertySource(properties = {"c2mon.server.elasticsearch.embedded=false"})
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheActionsModuleRef.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  CacheLoadingModuleRef.class,
  SupervisionModule.class,
  ElasticsearchModule.class,
  C2monIgniteConfiguration.class
})
@RunWith(SpringRunner.class)
public class ElasticsearchServiceTest {

  @Autowired
  private ElasticsearchProperties elasticsearchProperties;

  @Autowired
  private ElasticsearchTestClient esTestClient;

  @Autowired
  private IndexManager indexManager;

  @Autowired
  private TagConfigDocumentIndexer indexer;

  @Autowired
  private TagConfigDocumentConverter converter;

  @Autowired
  private TagCacheCollection tagCacheFacade;

  private final ElasticsearchService esService = new ElasticsearchService(new C2monClientProperties(), "c2mon");

  private AlarmService alarmService;

  private TagConfigDocumentListener tagDocumentListener;

  @Before
  public void setUp() {
    alarmService = createNiceMock(AlarmService.class);
    tagDocumentListener = new TagConfigDocumentListener(elasticsearchProperties, indexer, converter, tagCacheFacade, alarmService);

    if (elasticsearchProperties.isEmbedded()) {
      EmbeddedElasticsearchManager.start(elasticsearchProperties);
    } else {
      ContainerizedElasticsearchManager.start(elasticsearchProperties);
    }
  }

  @AfterClass
  public static void tearDownClass() {
    EmbeddedElasticsearchManager.stop();
    ContainerizedElasticsearchManager.stop();
  }

  @Before
  public void setupElasticsearch() throws InterruptedException {
    try {
      CompletableFuture<Void> nodeReady = CompletableFuture.runAsync(() -> {
        esTestClient.deleteIndex(elasticsearchProperties.getTagConfigIndex());
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
      Long testUserTagId = doubleToLongBits(Math.random()) % 10000;
      String testUser = Long.toHexString(doubleToLongBits(Math.random()));
      String responsible = "responsible";
      DataTagCacheObject tag = new DataTagCacheObject(testUserTagId);
      tag.getMetadata().getMetadata().put(responsible, testUser);
      expect(alarmService.getTagWithAlarmsAtomically(anyLong())).andReturn(new TagWithAlarms<>(tag, emptyList())).times(2);
      replay(alarmService);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);
      Long tag1234Id = doubleToLongBits(Math.random()) % 10000;
      String value1234 = "1234";
      tag = new DataTagCacheObject(tag1234Id);
      String key1234 = "1234";
      tag.getMetadata().getMetadata().put(key1234, value1234);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      esTestClient.refreshIndices();
      Thread.sleep(10000);

      assertEquals("There should be 2 tags, one for responsible and one for 1234", 2, esService.getDistinctTagMetadataKeys().size());

      Collection<Long> tagsForResponsibleUser = esService.findTagsByMetadata(responsible, testUser);
      assertEquals("There should be one tag with responsible user set to requested value", 1, tagsForResponsibleUser.size());
      assertEquals(testUserTagId, tagsForResponsibleUser.stream().findFirst().get());

      Collection<Long> tags1234 = esService.findTagsByMetadata(key1234, value1234);
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
      Long testUserTagId = doubleToLongBits(Math.random()) % 10000;
      String testUser = Long.toHexString(doubleToLongBits(Math.random()));
      String metadataKey = "metadataKey";
      DataTagCacheObject tag = new DataTagCacheObject(testUserTagId);
      String tagname = "tagname";
      tag.setName(tagname);
      tag.getMetadata().getMetadata().put(metadataKey, testUser);
      expect(alarmService.getTagWithAlarmsAtomically(anyLong())).andReturn(new TagWithAlarms<>(tag, emptyList())).times(3);
      replay(alarmService);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      tag = new DataTagCacheObject(doubleToLongBits(Math.random()) % 10000);
      tag.setName(tagname);
      tag.getMetadata().getMetadata().put(metadataKey, "some other metadata value");
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      tag = new DataTagCacheObject(doubleToLongBits(Math.random()) % 10000);
      tag.setName("other_tagname");
      tag.getMetadata().getMetadata().put(metadataKey, testUser);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      esTestClient.refreshIndices();
      Thread.sleep(10000);

      Collection<Long> tagsForResponsibleUser = esService.findTagsByNameAndMetadata(tagname, metadataKey, testUser);
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
      Collection<Long> tagsForResponsibleUser = esService.findTagsByName("TEST");
      assertNotNull("The tags collection should not be null", tagsForResponsibleUser);
      assertEquals("There tags collection should be empty", 0, tagsForResponsibleUser.size());
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}