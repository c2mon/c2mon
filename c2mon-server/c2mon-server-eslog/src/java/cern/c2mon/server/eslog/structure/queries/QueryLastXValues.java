package cern.c2mon.server.eslog.structure.queries;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to query the last X values to the ElasticSearch cluster with at least a given index.
 * Can alos specify the type, the TagId to fine tune the query.
 * @author Alban Marguet.
 */
public class QueryLastXValues extends Query {
    public QueryLastXValues(Client client, String[] indices, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
        super(client, indices, isTypeDefined, types, tagIds, from, size, min, max);
    }

    public SearchResponse getResponse() {
        //TODO: limit size ?
        String[] routing = getRouting(tagIds());

        SearchRequestBuilder requestBuilder = client.prepareSearch(indices());

        if (isTypeDefined()) {
            requestBuilder.setTypes(types());
        }

        return requestBuilder.setSearchType(SearchType.DEFAULT)
                .setFrom(from())
                .setSize(size())
                .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("tagId", tagIds())))
                .addSort("tagServerTime", SortOrder.DESC)
                .setRouting(getRouting(tagIds()))
                .execute().actionGet();
    }

    public List<String> getListOfAnswer() {
        List<String> list = new ArrayList<>();
        SearchResponse response = getResponse();
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            list.add(hit.getSourceAsString());
        }
        return list;
    }
}
