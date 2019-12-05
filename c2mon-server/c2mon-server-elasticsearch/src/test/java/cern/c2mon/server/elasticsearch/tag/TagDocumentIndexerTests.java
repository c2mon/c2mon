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
package cern.c2mon.server.elasticsearch.tag;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.elasticsearch.ElasticsearchSuiteTest;
import cern.c2mon.server.elasticsearch.ElasticsearchTestDefinition;
import cern.c2mon.server.elasticsearch.IndexNameManager;
import cern.c2mon.server.elasticsearch.junit.CachePopulationRule;
import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;
import cern.c2mon.server.elasticsearch.util.EntityUtils;
import cern.c2mon.server.elasticsearch.util.IndexUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
public class TagDocumentIndexerTests extends ElasticsearchTestDefinition {

  @Autowired
  private TagDocumentIndexer indexer;

  @Autowired
  private IndexNameManager indexNameManager;

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;

  @Autowired
  private TagDocumentConverter converter;

  private TagDocument document;

  @Before
  public void setUp() {
    DataTagCacheObject tag = (DataTagCacheObject) EntityUtils.createDataTag();
    document = converter.convert(tag).orElseThrow(() -> new IllegalArgumentException("TagDocument conversion failed"));
    indexName = indexNameManager.indexFor(document);
  }

  @Test
  public void indexSingleTagTest() throws IDBPersistenceException, InterruptedException, IOException {
    indexer.storeData(document);

    // Bulk flush operation seem to require more time
    Thread.sleep(1000L);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertTrue("Index should have been created.",
        IndexUtils.doesIndexExist(indexName, ElasticsearchSuiteTest.getProperties()));

    List<String> indexData = EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName);
    assertEquals("Index should have one document inserted.", 1, indexData.size());
  }

  @Test
  public void indexMultipleTagsTest() throws IDBPersistenceException, InterruptedException, IOException {
    indexer.storeData(document);
    indexer.storeData(document);

    // Bulk flush operation seem to require more time
    Thread.sleep(1000l);

    EmbeddedElasticsearchManager.getEmbeddedNode().refreshIndices();

    assertTrue("Index should have been created.",
        IndexUtils.doesIndexExist(indexName, ElasticsearchSuiteTest.getProperties()));

    List<String> indexData = EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments(indexName);
    assertEquals("Index should have two documents inserted.", 2, indexData.size());
  }
}
