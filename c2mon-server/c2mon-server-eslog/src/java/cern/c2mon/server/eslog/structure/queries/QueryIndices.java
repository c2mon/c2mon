package cern.c2mon.server.eslog.structure.queries;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Arrays;
import java.util.List;

/**
 * Query to be launched against ElasticSearch to retrieve all the indices present in the cluster.
 * @author Alban Marguet.
 */
public class QueryIndices extends Query {
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
}
