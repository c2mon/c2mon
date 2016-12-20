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

  void q2(Closure closure) {
    System.out.println("Closure called");

    QuerySpec query = new QuerySpec();

    Closure code = closure.rehydrate(query, this, this);
    code.setResolveStrategy(Closure.DELEGATE_FIRST);

    Object object = code.call();
    System.out.println(object);
  }

  @Data
  public class QuerySpec {
    private String name;
    private String thing;

    QuerySpec name(String name) {
      this.name = name;
      return this;
    }

    QuerySpec thing(String thing) {
      this.thing = thing;
      return this;
    }
  }
}
