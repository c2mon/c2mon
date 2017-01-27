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
package cern.c2mon.server.elasticsearch.supervision;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.elasticsearch.Indices;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
import cern.c2mon.server.elasticsearch.util.EntityUtils;
import cern.c2mon.shared.client.supervision.SupervisionEvent;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Alban Marguet
 * @author Justin LEwis Salmon
 */
public class SupervisionEventDocumentIndexerTests extends BaseElasticsearchIntegrationTest {

  @Autowired
  private SupervisionEventDocumentIndexer indexer;

  @Autowired
  private ElasticsearchClient client;

  @Test
  public void logSupervisionEvent() throws IDBPersistenceException {
    SupervisionEvent event = EntityUtils.createSupervisionEvent();

    SupervisionEventDocument document = new SupervisionEventDocumentConverter().convert(event);
    indexer.storeData(document);

    // Refresh the index to make sure the document is searchable
    String index = Indices.indexFor(document);
    client.getClient().admin().indices().prepareRefresh(index).execute().actionGet();

    // Make sure the index was created
    assertTrue(Indices.exists(index));

    // Make sure the alarm exists in the index
    SearchResponse response = client.getClient().prepareSearch(index).setTypes("supervision").execute().actionGet();
    assertEquals(response.getHits().totalHits(), 1);

    // Clean up
    DeleteIndexResponse deleteResponse = client.getClient().admin().indices().prepareDelete(index).execute().actionGet();
    assertTrue(deleteResponse.isAcknowledged());
  }
}
