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
package cern.c2mon.server.elasticsearch.tag.config;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.elasticsearch.ElasticsearchSuiteTest;
import cern.c2mon.server.elasticsearch.ElasticsearchTestDefinition;
import cern.c2mon.server.elasticsearch.IndexManager;
import cern.c2mon.server.elasticsearch.IndexNameManager;
import cern.c2mon.server.elasticsearch.junit.CachePopulationRule;
import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;
import cern.c2mon.server.elasticsearch.util.EntityUtils;
import cern.c2mon.server.elasticsearch.util.IndexUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Szymon Halastra
 * @author Justin Lewis Salmon
 */
public class TagConfigDocumentIndexerTests extends ElasticsearchTestDefinition {

  @Autowired
  private IndexNameManager indexNameManager;

  @Autowired
  private IndexManager indexManager;

  @Autowired
  private TagConfigDocumentIndexer indexer;

  @Autowired
  private TagConfigDocumentConverter converter;

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;

  private DataTagCacheObject tag;
  private TagConfigDocument document;

  @Before
  public void setUp() throws Exception {
    tag = (DataTagCacheObject) EntityUtils.createDataTag();
    document = converter.convert(tag).orElseThrow(() -> new Exception("Tag conversion failed"));
    indexName = indexNameManager.indexFor(document);
  }

  @Test
  public void addDataTag() throws Exception {
    indexer.indexTagConfig(document);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertTrue("Index should have been created.",
        IndexUtils.doesIndexExist(indexName, ElasticsearchSuiteTest.getProperties()));

    List<String> indexData = EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName);
    assertEquals("Index should have one document inserted.", 1, indexData.size());
  }

  @Test
  public void updateDataTag() throws Exception {
    document.put("description", tag.getDescription() + " MODIFIED.");
    ((Map<String, Object>) document.get("metadata")).put("spam", "eggs");
    indexer.updateTagConfig(document);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertTrue("Index should have been created when trying to update non-existing one.",
        IndexUtils.doesIndexExist(indexName, ElasticsearchSuiteTest.getProperties()));

    List<String> indexData = EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName);
    assertEquals("Index should have one document inserted.", 1, indexData.size());

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(indexData.get(0));

    assertEquals(tag.getDescription() + " MODIFIED.", jsonNode.get("description").asText());
    assertEquals("eggs", jsonNode.get("metadata").get("spam").asText());
  }

  @Test
  public void removeDataTag() throws Exception {
    indexer.indexTagConfig(document);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    List<String> indexData = EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName);
    assertEquals("Index should have been created.", 1, indexData.size());

    indexer.removeTagConfigById(tag.getId());

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertTrue("Index should exist after tag config deletion.",
        IndexUtils.doesIndexExist(indexName, ElasticsearchSuiteTest.getProperties()));

    indexData = EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName);
    assertEquals("Index documents should been deleted.", 0, indexData.size());
  }
}
