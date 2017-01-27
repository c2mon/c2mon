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
package cern.c2mon.server.elasticsearch.tag;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.elasticsearch.Indices;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
import cern.c2mon.server.elasticsearch.junit.CachePopulationRule;
import cern.c2mon.server.elasticsearch.util.EntityUtils;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
@TestPropertySource(properties = {
    // Setting the number of concurrent requests to 0 causes the flush
    // operation of the bulk tp be executed in a synchronous manner
    "c2mon.server.elasticsearch.concurrentRequests=0"
})
public class TagDocumentIndexerTests extends BaseElasticsearchIntegrationTest {

  @Autowired
  private TagDocumentIndexer indexer;

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;

  @Autowired
  private TagDocumentConverter converter;

  @Autowired
  private ElasticsearchClient client;

  @Test
  public void indexTags() throws IDBPersistenceException, InterruptedException {
    DataTagCacheObject tag = (DataTagCacheObject) EntityUtils.createDataTag();

    TagDocument document = converter.convert(tag);
    indexer.storeData(document);

    // Refresh the index to make sure the document is searchable
    String index = Indices.indexFor(document);
    client.getClient().admin().indices().prepareRefresh(index).get();
    client.getClient().admin().cluster().prepareHealth().setIndices(index).setWaitForYellowStatus().get();
    client.waitForYellowStatus();

    // Make sure the index was created
    assertTrue(Indices.exists(index));

    // Make sure the tag exists in the index
    SearchResponse response = client.getClient().prepareSearch(index).setRouting(tag.getId().toString()).get();
    assertEquals(1, response.getHits().totalHits());

    // Clean up
    DeleteIndexResponse deleteResponse = client.getClient().admin().indices().prepareDelete(index).get();
    assertTrue(deleteResponse.isAcknowledged());
  }
}
