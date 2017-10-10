package cern.c2mon.server.dsl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.lang.Script;

import cern.c2mon.server.elasticsearch.ElasticsearchService;
import cern.c2mon.server.elasticsearch.tag.TagDocument;

/**
 * @author Martin Flamm
 */
public abstract class ExpressionBase extends Script {

  ElasticsearchService service = new ElasticsearchService();

  Double avg(Map<String, List<TagDocument>> data) {
    Map<String, Double> results = new HashMap<>();

    for (Map.Entry<String, List<TagDocument>> entry : data.entrySet()) {
      results.put(entry.getKey(), entry.getValue().stream()
          .mapToDouble(tag -> tag.getProperty("value", Double.class))
          .average()
          .orElse(0));
    }

    return results.values().stream().mapToDouble(e -> e).average().orElse(0);
  }

  Double min(Map<String, List<TagDocument>> data) {
    Map<String, Double> results = new HashMap<>();

    for (Map.Entry<String, List<TagDocument>> entry : data.entrySet()) {
      results.put(entry.getKey(), entry.getValue().stream()
          .mapToDouble(tag -> tag.getProperty("value", Double.class))
          .min()
          .orElse(0));
    }

    return results.values().stream().mapToDouble(e -> e).min().orElse(0);
  }

  Double max(Map<String, List<TagDocument>> data) {
    Map<String, Double> results = new HashMap<>();

    for (Map.Entry<String, List<TagDocument>> entry : data.entrySet()) {
      results.put(entry.getKey(), entry.getValue().stream()
          .mapToDouble(tag -> tag.getProperty("value", Double.class))
          .max()
          .orElse(0));
    }

    return results.values().stream().mapToDouble(e -> e).max().orElse(0);
  }

//  Map<String, List<TagDocument>> meta(Map<String, Object> paramsl) {
//return new
//  }

  Map<String, List<TagDocument>> q(Map<String, Object> params, String interval) {
    String name = (String) params.remove("name");
    Map<String, List<TagDocument>> result = service.q(name, params, interval);
    return result;
  }

  Map<String, List<TagDocument>> q2(Map<String, Object> params) {
    return service.q2(params);
  }

  private static void log(Object object) throws IOException {
    String json = new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(object);
    System.out.println(json + "\n");
  }
}
