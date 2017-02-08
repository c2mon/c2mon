package cern.c2mon.server.elasticsearch;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import cern.c2mon.server.elasticsearch.tag.TagDocument;

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

  public Map<String, List<TagDocument>> q(String name, Map<String, Object> metadata, String interval) {
    log.info("using interval: " + interval);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    BoolQueryBuilder query = boolQuery();
    query
        .must(wildcardQuery("name", name))
        .must(rangeQuery("timestamp").gte("now-" + interval));

    metadata.forEach((k, v) -> query.must(wildcardQuery("metadata." + k, v.toString())));

    searchSourceBuilder
        .size(0)
        .aggregation(AggregationBuilders
            .filter("filter").filter(query)
            .subAggregation(AggregationBuilders.terms("group-by-name").field("name")
                .subAggregation(AggregationBuilders.topHits("top-tag-hits")
                    .setSize(1000)
                    .setFetchSource(new String[] {"value", "timestamp"}, null))));

    SearchResult result;
    Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("c2mon-tag*").build();

    try {
      result = client.execute(search);
    } catch (IOException e) {
      throw new RuntimeException("Error querying history for tag " + name, e);
    }

    List<TermsAggregation.Entry> buckets = result.getAggregations()
        .getFilterAggregation("filter")
        .getTermsAggregation("group-by-name").getBuckets();

    Map<String, List<TagDocument>> results = new HashMap<>();

    for (TermsAggregation.Entry bucket : buckets) {
      results.put(bucket.getKey(), bucket.getTopHitsAggregation("top-tag-hits")
          .getHits(TagDocument.class).stream()
          .filter(hit -> hit.source.get("value") != null)
          .map(hit -> {
            TagDocument doc = hit.source;
            doc.remove("es_metadata_id");
            return doc;
          })
          .collect(toList()));
    }

    return results;
  }
}
