package cern.c2mon.server.eslog.structure;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Different defined queries in ElasticSearch.
 * @author Alban Marguet.
 */
public enum Query {
    LAST_X_VALUES{
        @Override
        public SearchResponse getResponse(Client client, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
            //TODO: limit size ?
            String[] routing = getRouting(tagIds);

            SearchRequestBuilder requestBuilder = client.prepareSearch(indexes);

            if (isTypeDefined) {
                requestBuilder.setTypes(types);
            }

            return requestBuilder.setSearchType(SearchType.DEFAULT)
                    .setFrom(from)
                    .setSize(size)
                    .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("tagId", tagIds)))
                    .addSort("tagServerTime", SortOrder.DESC)
                    .setRouting(routing)
                    .execute().actionGet();
        }

        @Override
        public List<SearchResponse> getResponses(Client client, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
            List<SearchResponse> responses =  new ArrayList<>();
            responses.add(getResponse(client, indexes, isTypeDefined, types, tagIds, from, size, min, max));
            return responses;
        }
    },
    RANGE_QUERY {
        @Override
        public SearchResponse getResponse(Client client, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
            String[] routing = getRouting(tagIds);

            SearchRequestBuilder requestBuilder = client.prepareSearch(indexes);

            if (isTypeDefined) {
                requestBuilder.setTypes(types);
            }

            return requestBuilder
                .setSearchType(SearchType.DEFAULT)
                    .setFrom(from)
                    .setSize(size)
                    .setQuery(QueryBuilders.boolQuery()
                            .must(QueryBuilders.rangeQuery("tagServerTime").gte(min).lte(max))
                            .filter(QueryBuilders.termsQuery("tagId", tagIds)))
                    .addSort("tagServerTime", SortOrder.DESC)
                    .setRouting(routing)
                    .execute().actionGet();
        }

        @Override
        public List<SearchResponse> getResponses(Client client, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
            List<SearchResponse> responses =  new ArrayList<>();
            responses.add(getResponse(client, indexes, isTypeDefined, types, tagIds, from, size, min, max));
            return responses;
        }
    },
    RANGE_PAGED_QUERY {
        @Override
        public SearchResponse getResponse(Client client, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
            String[] routing = getRouting(tagIds);

            SearchRequestBuilder requestBuilder = client.prepareSearch(indexes);

            if (isTypeDefined) {
                requestBuilder.setTypes(types);
            }

            return requestBuilder
                    .setSearchType(SearchType.DEFAULT)
                    .setFrom(from)
                    .setSize(size)
                    .setQuery(QueryBuilders.boolQuery()
                            .must(QueryBuilders.rangeQuery("tagServerTime").gte(min).lte(max))
                            .filter(QueryBuilders.termsQuery("tagId", tagIds)))
                    .addSort("tagServerTime", SortOrder.DESC)
                    .setRouting(routing)
                    .execute().actionGet();
        }

        public List<SearchResponse> getResponses(Client client, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
            //TODO: probably rework in history player context.
            List<SearchResponse> responses = new ArrayList<>();

            String[] routing = getRouting(tagIds);

            long count = client.prepareCount(indexes)
                    .setTypes(types)
                    .setQuery(QueryBuilders.boolQuery()
                            .must(QueryBuilders.rangeQuery("tagServerTime").gte(min).lte(max))
                            .filter(QueryBuilders.termsQuery("tagId", tagIds)))
                    .setRouting(routing)
                    .execute().actionGet().getCount();

            int begin = 0;

            for (long i = 0; i < count; i++) {
                SearchResponse response = getResponse(client, indexes, isTypeDefined, types, tagIds, begin, PAGE_SIZE, min, max);

                if (response.getHits().getHits().length > 0) {
                    responses.add(response);
                    begin += PAGE_SIZE;
                } else {
                    i = count; // best case exit
                }
            }

            return responses;
        }
    },
    INDICES {
        @Override
        public SearchResponse getResponse(Client client, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
            String[] routing = getRouting(tagIds);

            SearchRequestBuilder requestBuilder = client.prepareSearch(indexes);

            return requestBuilder
                    .setSearchType(SearchType.DEFAULT)
                    .setFrom(0)
                    .setSize(1)
                    .setQuery(QueryBuilders.boolQuery()
                            .filter(QueryBuilders.termsQuery("tagId", tagIds)))
                    .setRouting(routing)
                    .execute().actionGet();
        }

        @Override
        public List<SearchResponse> getResponses(Client client, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max) {
            return null;
        }
    };

    protected final int PAGE_SIZE = 10000;
    public abstract SearchResponse getResponse(Client client, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max);
    public abstract List<SearchResponse> getResponses(Client client, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max);

    public List<String> getIndices(Client client) {
        String[] indices = client.admin().indices().prepareGetIndex().get().indices();
        return Arrays.asList(indices);
    }

    /**
     * Utility method to get String value of every long in an array of TagES ids.
     * @param tagIds array of long
     * @return array of String
     */
    protected String[] getRouting(long[] tagIds) {
        String[] routing = new String[tagIds.length];
        for (int i = 0; i < tagIds.length; i++) {
            routing[i] = String.valueOf(tagIds[i]);
        }
        return routing;
    }
}
