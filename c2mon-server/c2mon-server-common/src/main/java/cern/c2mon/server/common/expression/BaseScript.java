package cern.c2mon.server.common.expression;


import cern.c2mon.server.common.tag.Tag;
import groovy.lang.Closure;
import groovy.lang.Script;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Justin Lewis Salmon
 */
public abstract class BaseScript extends Script {

  ElasticsearchService service = new ElasticsearchService();

  Double avg(Tag tag, String interval) {
    return service.avg(tag.getId(), interval);
  }

  Double avg(List<Number> data) {
    return data.stream().mapToDouble(Number::doubleValue).average().orElse(0);
  }

  List<Object> q(Map<String, Object> params, String interval) {
    params.forEach((k, v) -> System.out.print(k + ": " + v + ", "));
    System.out.println(interval);

    String name = (String) params.remove("name");

    return service.q(name, params, interval);
  }
}
