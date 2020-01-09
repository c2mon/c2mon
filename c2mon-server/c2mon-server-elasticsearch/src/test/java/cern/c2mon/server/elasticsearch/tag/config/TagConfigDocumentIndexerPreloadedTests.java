package cern.c2mon.server.elasticsearch.tag.config;

import cern.c2mon.server.elasticsearch.Indices;
import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
import cern.c2mon.server.test.CachePopulationRule;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = CachePopulationRule.class)
public class TagConfigDocumentIndexerPreloadedTests extends BaseElasticsearchIntegrationTest {

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;

  @Autowired
  private TagConfigDocumentIndexer indexer;

  @Test
  public void reindexTagConfigDocuments() {
    final String index = Indices.indexFor(new TagConfigDocument());
    if (!Indices.exists(index)) {
      //Delete the index first
      DeleteIndexResponse deleteResponse = client.getClient().admin().indices().prepareDelete(index).get();
      assertTrue("The index could not be deleted", deleteResponse.isAcknowledged());
    }
    //reindex everything from the cache
    indexer.reindexAllTagConfigDocuments();
    // Refresh the index to make sure the document is searchable
    client.getClient().admin().indices().prepareRefresh(index).get();
    client.getClient().admin().cluster().prepareHealth().setIndices(index).setWaitForYellowStatus().get();
    SearchResponse response = client.getClient().prepareSearch(index).get();
    //53 IDs from c2mon-server-test/src/resources/sql/cache-data-insert.sql
    assertEquals("There should be 53 tag config documents", 53, response.getHits().totalHits);
  }
}
