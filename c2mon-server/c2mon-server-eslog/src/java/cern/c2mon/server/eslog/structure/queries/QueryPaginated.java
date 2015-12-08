package cern.c2mon.server.eslog.structure.queries;

import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to get queries paginated according to the number of answers.
 * @author Alban Marguet.
 */
public class QueryPaginated extends Query {
    public QueryPaginated(Client client, String[] indices, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
        super(client, indices, isTypeDefined, types, tagIds, from, size, min, max);
    }

    private final int PAGE_SIZE = 1000;

    public List<List<String>> getListOfAnswer() {
        List<List<String>> list = new ArrayList<>();
        List<SearchResponse> responses = getResponses();

        for(SearchResponse response : responses) {
            SearchHit[] hits = response.getHits().getHits();
            List<String> page = new ArrayList<>();
            for (SearchHit hit : hits) {
                page.add(hit.getSourceAsString());
            }

            list.add(page);
        }

        return list;
    }

    public List<SearchResponse> getResponses() {
        List<SearchResponse> list = new ArrayList<>();
        long counter = getResponse(true, 0).getHits().getTotalHits();

        SearchResponse response;
        for(int i = 0; i < counter; i+=PAGE_SIZE) {
            response = getResponse(false, i);
            list.add(response);
        }

        return list;
    }

    private SearchResponse getResponse(boolean toCount, int begin) {
        int size = 0;
        int from = from();
        SearchRequestBuilder countBuilder = client.prepareSearch(indices());

        if (isTypeDefined()) {
            countBuilder.setTypes(types());
        }

        if (!toCount) {
            from = begin;
            size = PAGE_SIZE;
            countBuilder.addSort("tagServerTime", SortOrder.DESC);
        }

        return countBuilder.setSearchType(SearchType.DEFAULT)
                .setSize(size)
                .setFrom(from)
                .setQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("tagServerTime").gte(min()).lte(max()))
                        .filter(QueryBuilders.termsQuery("tagId", tagIds())))
                .setRouting(getRouting(tagIds()))
                .execute().actionGet();
        }
}
