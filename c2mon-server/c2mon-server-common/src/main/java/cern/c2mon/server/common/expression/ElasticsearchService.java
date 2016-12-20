package cern.c2mon.server.common.expression;

import cern.c2mon.server.common.tag.Tag;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.AvgAggregation;
import io.searchbox.core.search.aggregation.DateHistogramAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class ElasticsearchService {

  private JestClient client;

  public ElasticsearchService() {
    JestClientFactory factory = new JestClientFactory();
    factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:9200")
        .multiThreaded(true)
        .build());
    client = factory.getObject();
  }

  public Double avg(Long id, String interval) {
    log.info("using interval: " + interval);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(boolQuery()
        .filter(termQuery("id", id))
        .must(rangeQuery("timestamp").gte("now-" + interval)))
        .size(0)
        .aggregation(AggregationBuilders.avg("avg").field("value"));

    SearchResult result;
    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("c2mon-tag*").build();

    try {
      result = client.execute(search);
    } catch (IOException e) {
      throw new RuntimeException("Error querying history for tag #" + id, e);
    }

    AvgAggregation aggregation = result.getAggregations().getAvgAggregation("avg");
    return aggregation == null ? null : aggregation.getAvg();
  }

  public List<Object> q(String name, Map<String, Object> metadata, String interval) {
    log.info("using interval: " + interval);

    BoolQueryBuilder nestedMetadataQuery = boolQuery();
    metadata.forEach((k, v) -> nestedMetadataQuery.must(wildcardQuery("metadata." + k, v.toString())));

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(boolQuery()
        .filter(termQuery("name", name))
        .must(rangeQuery("timestamp").gte("now-" + interval))
        .must(nestedQuery("metadata", nestedMetadataQuery)));

    SearchResult result;
    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("c2mon-tag*").build();

    try {
      result = client.execute(search);
    } catch (IOException e) {
      throw new RuntimeException("Error querying history for tag " + name, e);
    }

    return result.getHits(Map.class).stream().map(hit -> hit.source.get("value")).collect(toList());
  }

//
//  /**
//   * Find all tags by name with a given prefix.
//   *
//   * @param query the tag name prefix
//   * @return a list of tags whose names match the given prefix
//   */
//  public Collection<Tag> findByName(String query) {
//    List<Long> tagIds = new ArrayList<>();
//
//    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//    searchSourceBuilder
//        .query(prefixQuery("name", query))
//        .size(0)
//        .aggregation(AggregationBuilders.terms("group-by-name")
//            .field("name")
//            .subAggregation(
//                AggregationBuilders.topHits("top")
//                    .setSize(1)
//                    .addSort("timestamp", SortOrder.DESC)
//                    .setFetchSource("id", null)
//            ));
//
//    SearchResult result;
//    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("c2mon-tag*").build();
//
//    try {
//      result = client.execute(search);
//    } catch (IOException e) {
//      throw new RuntimeException("Error querying top most active tags", e);
//    }
//
//    for (TermsAggregation.Entry bucket : result.getAggregations().getTermsAggregation("group-by-name").getBuckets()) {
//      double id = (double) bucket.getTopHitsAggregation("top").getFirstHit(Map.class).source.get("id");
//      tagIds.add((long) id);
//    }
//
//    return tagCache.get(tagIds);
//  }
//
//  /**
//   * Find all tags containing the exact metadata key/value pair.
//   *
//   * @param key   the metadata key
//   * @param value the metadata value
//   * @return a list of tags containing the exact metadata requested
//   */
//  public Collection<Tag> findByMetadata(String key, String value) {
//    List<Long> tagIds = new ArrayList<>();
//
//    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//    searchSourceBuilder.query(
//        nestedQuery("metadata",
//            boolQuery().must(matchQuery("metadata." + key, value))))
//        .aggregation(AggregationBuilders.terms("group-by-id")
//            .field("id")
//            .size(0)
//        );
//
//    SearchResult result;
//    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("c2mon-tag*").build();
//
//    try {
//      result = client.execute(search);
//    } catch (IOException e) {
//      throw new RuntimeException("Error querying top most active tags", e);
//    }
//
//    tagIds.addAll(result.getAggregations().getTermsAggregation("group-by-id").getBuckets()
//        .stream()
//        .map(bucket -> Long.valueOf(bucket.getKey())).collect(Collectors.toList()));
//
//    return tagCache.get(tagIds);
//  }
}
