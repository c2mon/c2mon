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

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.elasticsearch.Indices;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocument;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentConverter;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentIndexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Slf4j
public class ElasticsearchServiceTest extends BaseElasticsearchIntegrationTest {

  @Autowired
  private TagConfigDocumentIndexer indexer;

  @Autowired
  private TagConfigDocumentConverter converter;

  private C2monClientProperties properties = new C2monClientProperties();

  private ElasticsearchService service;

  @Before
  public void before() throws Exception {
    properties.getElasticsearch().setUrl("http://localhost:" + client.getClient().settings().get("http.port"));
    log.info("Connecting Elasticsearch HTTP client to {}", properties.getElasticsearch().getUrl());
    service = new ElasticsearchService(properties, "c2mon");
    addDataTags();
  }

  @After
  public void cleanup() throws Exception {
    DataTagCacheObject tag = EntityUtils.createDataTag1();

    TagConfigDocument document = converter.convert(tag)
            .orElseThrow(()->new Exception("Tag conversion failed"));
    String index = Indices.indexFor(document);

    // Clean up
    DeleteIndexResponse deleteResponse = client.getClient().admin().indices().prepareDelete(index).get();
    assertTrue(deleteResponse.isAcknowledged());
  }

  private void addDataTags() throws Exception {
    DataTagCacheObject tag1 = EntityUtils.createDataTag1();
    DataTagCacheObject tag2 = EntityUtils.createDataTag2();

    TagConfigDocument document = converter.convert(tag1, EntityUtils.createAlarmsForTag(tag1.getId()))
            .orElseThrow(()->new Exception("Tag conversion failed"));
    indexer.indexTagConfig(document);

    document = converter.convert(tag2)
        .orElseThrow(()->new Exception("Tag conversion failed"));
    indexer.indexTagConfig(document);

    String index = Indices.indexFor(document);
    assertTrue(Indices.exists(index));

    // Refresh the index to make sure the document is searchable
    client.getClient().admin().indices().prepareRefresh(index).get();
    client.getClient().admin().cluster().prepareHealth().setIndices(index).setWaitForYellowStatus().get();

    // Make sure the tag exists in the index
    SearchResponse response = client.getClient().prepareSearch(index).setRouting(tag1.getId().toString()).get();
    assertEquals(1, response.getHits().getTotalHits());
    response = client.getClient().prepareSearch(index).setRouting(tag2.getId().toString()).get();
    assertEquals(1, response.getHits().getTotalHits());

    log.info("Added two documents to index {}", index);
  }

  @Test
  public void testFindTagsByMetadata() throws Exception {
    Collection<Long> tagsForResponsibleUser = service.findTagsByMetadata("responsiblePerson", "Fred");
    assertEquals("There should be one tag with responsible user set to requested value", 1, tagsForResponsibleUser.size());
    assertEquals(Long.valueOf(1L), tagsForResponsibleUser.stream().findFirst().get());

    Collection<Long> tagIds = service.findTagsByMetadata("responsiblePerson");
    assertEquals("There should be 2 tags with that metadata", 2, tagIds.size());
    assertTrue("Should contain that tag id", tagIds.contains(EntityUtils.createDataTag1().getId()));
    assertTrue("Should contain that tag id", tagIds.contains(EntityUtils.createDataTag2().getId()));
  }

  @Test
  public void testFindTagsByAlarmMetadata() throws Exception {
    Collection<Long> tagIds = service.findTagsByAlarmMetadata("meta2");
    assertEquals("There should be 1 tag with an alarm with that metadata", 1, tagIds.size());
    assertTrue("Should contain that tag id", tagIds.contains(EntityUtils.createDataTag1().getId()));

    tagIds = service.findTagsByAlarmMetadata("meta5");
    assertEquals("There should be 1 tag with an alarm with that metadata", 1, tagIds.size());
    assertTrue("Should contain that tag id", tagIds.contains(EntityUtils.createDataTag1().getId()));

    tagIds = service.findTagsByAlarmMetadata("metaDoesNotExist");
    assertEquals("There should be no tag with that alarm metadata", 0, tagIds.size());

    tagIds = service.findTagsByAlarmMetadata("meta6", "value6");
    assertEquals("There should be 1 tag with an alarm with that metadata", 1, tagIds.size());
    assertTrue("Should contain that tag id", tagIds.contains(EntityUtils.createDataTag1().getId()));

    tagIds = service.findTagsByAlarmMetadata("meta1", "1");
    assertEquals("There should be 1 tag with an alarm with that metadata", 1, tagIds.size());
    assertTrue("Should contain that tag id", tagIds.contains(EntityUtils.createDataTag1().getId()));

    tagIds = service.findTagsByAlarmMetadata("meta1", "DoesNotExist");
    assertEquals("There should be no tag with that alarm metadata", 0, tagIds.size());
  }

  @Test
  public void testGetDistinctTagsMetadataKeys() throws Exception {
    assertEquals("There should be 4 distinct metadata keys", 4, service.getDistinctTagMetadataKeys().size());
  }

  @Test
  public void testFindTagsByNameAndMetadata() throws Exception {
    Collection<Long> tagIds = service.findTagsByNameAndMetadata("cpu.*", "building", "2");
    assertEquals("There should be one tag with given name and metadata", 1, tagIds.size());
    assertEquals("Tag with id 2L should match", Long.valueOf(2L), tagIds.stream().findFirst().get());
  }

  @Test
  public void testFindTagsByName() throws Exception {
    Collection<Long> tagsForResponsibleUser = service.findTagsByName(EntityUtils.createDataTag1().getName());
    assertNotNull("The tags collection should not be null", tagsForResponsibleUser);
    assertEquals("The search result should be 1", 1, tagsForResponsibleUser.size());

    tagsForResponsibleUser = service.findTagsByName("cpu.*");
    assertNotNull("The tags collection should not be null", tagsForResponsibleUser);
    assertEquals("The search result should be 2", 2, tagsForResponsibleUser.size());
  }
}