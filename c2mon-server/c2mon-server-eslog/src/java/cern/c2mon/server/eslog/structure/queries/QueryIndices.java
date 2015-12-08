package cern.c2mon.server.eslog.structure.queries;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Arrays;
import java.util.List;

/**
 * Query to be launched against ElasticSearch to retrieve all the indices present in the cluster.
 * @author Alban Marguet.
 */
@Slf4j
public class QueryIndices extends Query {

    public QueryIndices(Client client, String[] indices, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
        super(client, indices, isTypeDefined, types, tagIds, from, size, min, max);
    }
    /**
     * Query to get all the entries where the tagId is present.
     * @return SearchResponse
     */
    protected SearchResponse getResponse() {
        SearchRequestBuilder requestBuilder = client.prepareSearch(indices());

        return requestBuilder
                .setSearchType(SearchType.DEFAULT)
                .setFrom(0)
                .setSize(1)
                .setQuery(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.termsQuery("tagId", tagIds())))
                .setRouting(getRouting(tagIds()))
                .execute().actionGet();
    }

    /**
     * Simple query to get all the indices in the cluster.
     * @return List<String>: names of the indices.
     */
    public List<String> getListOfAnswer() {
        String[] indices = client.admin().indices().prepareGetIndex().get().indices();
        return Arrays.asList(indices);
    }

    public void initTest() {
        try {
            List<String> indices = getListOfAnswer();
            log.info("indices present in the cluster:");
            for (String s : indices) {
                log.info(s);
            }
        } catch(NoNodeAvailableException e) {
            log.error("initTest() - Error while creating client, could not find a connection to the ElasticSearch cluster, is it running?");
        }
    }
}
