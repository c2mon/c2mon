//package cern.c2mon.server.eslog.structure.queries;
//
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.action.search.*;
//import org.elasticsearch.client.Client;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.sort.SortOrder;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Query a multiSearch request against the ElasticSearch cluster.
// * Basically represents one QueryLastXValuesRange for every required index in indices().
// * We are then getting the last X values in the given range for each different provided tagId.
// * @author Alban Marguet.
// */
//@Slf4j
//public class QueryMultiSearch extends Query {
//    public QueryMultiSearch(Client client) {
//        super(client);
//    }
//
//    public QueryMultiSearch(Client client, List<String> indices, boolean isTypeDefined, List<String> types, List<Long> tagIds, int from, int size, int min, int max) {
//        super(client, indices, isTypeDefined, types, tagIds, from, size, min, max);
//    }
//
//    public List<List<String>> getListOfAnswer() {
//        List<List<String>> list = new ArrayList<>();
//        List<SearchResponse> responses = getResponse();
//
//        for(SearchResponse response : responses) {
//            SearchHit[] hits = response.getHits().getHits();
//            List<String> page = new ArrayList<>();
//            for (SearchHit hit : hits) {
//                page.add(hit.getSourceAsString());
//            }
//
//            list.add(page);
//        }
//
//        return list;
//    }
//
//    protected List<SearchResponse> getResponse() {
//        List<SearchResponse> list = new ArrayList<>();
//
//        MultiSearchRequestBuilder sr = client.prepareMultiSearch();
//        for (int i = 0; i < indices().length; i++) {
//            sr.add(getSearchRequest(indices()[i]));
//        }
//
//        MultiSearchResponse responses = sr.execute().actionGet();
//        for (MultiSearchResponse.Item item : responses.getResponses()) {
//            list.add(item.getResponse());
//        }
//        return list;
//    }
//
//    protected SearchRequestBuilder getSearchRequest(String index) {
//        SearchRequestBuilder srb = client.prepareSearch(index);
//
//        if (isTypeDefined()) {
//            srb.setTypes(types());
//        }
//
//        srb.setSearchType(SearchType.DEFAULT)
//                .setFrom(from())
//                .setSize(size())
//                .setQuery(QueryBuilders.boolQuery()
//                        .must(QueryBuilders.rangeQuery("tagServerTime").gte(min()).lte(max()))
//                        .filter(QueryBuilders.termsQuery("tagId", tagIds())))
//                .addSort("tagServerTime", SortOrder.DESC)
//                .setRouting(getRouting(tagIds()));
//        return srb;
//    }
//}