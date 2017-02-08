package cern.c2mon.server.elasticsearch;


import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.elasticsearch.tag.TagDocument;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Justin Lewis Salmon
 */
public abstract class BaseScript extends Script {

  ElasticsearchService service = new ElasticsearchService();

  public static void main(String[] args) {

    /**
     * Here are some experiments with the expression language.
     *
     * The idea is for it to look something like this:
     *
     *    q(name:'cpu.loadavg', host:'cs*', location:'864', '5m')
     *
     * How this works:
     *
     * The `q` function means "query", all expressions are wrapped in it. It is
     * the entry point for an expression.
     *
     * The first n parameters are either properties of a tag (e.g. name) or
     * metadata keys. These will be used to build the Elasticsearch query.
     *
     * The last parameter is an optional time duration "t", which will cause
     * the query to take into account all updates in the interval "now - t".
     *
     * In this case, we are looking at potentially multiple tags, because the
     * "host" parameter has a wildcard and there could be multiple tags with
     * the name "cpu.loadavg" on different hosts.
     *
     * Therefore, the result of this expression will be something like a set
     * of lists of timestamp-value pairs, grouped by tag e.g.:
     *
     * [
     *   {
     *     "tag": "cpu.loadavg",
     *     "metadata": {
     *       "host": "cs-ccr-tim1"
     *     },
     *     "values": [
     *       {1486111109 : 6.8},
     *       {1486111124 : 8.6},
     *       {1486111139 : 14.0},
     *       {1486111154 : 23.4}
     *     ]
     *   },
     *   {
     *     ...
     *   }
     * ]
     *
     * Normally it would be more useful to reduce this data down to a single
     * average value per host. So for example, we can wrap the original query
     * in an "avg" function to compute the average of the values for each tag,
     * e.g.:
     *
     *   avg(q(name:'cpu.loadavg', host:'cs*', location:'864', '5m'))
     *
     * which would result in something like this:
     *
     * [
     *   {
     *     "tag": "cpu.loadavg",
     *     "metadata": {
     *       "host": "cs-ccr-tim1"
     *     },
     *     "value": 15.2
     *   },
     *   {
     *     ...
     *   }
     * ]
     *
     */

    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.WARN);

    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(BaseScript.class.getName());
    GroovyShell shell = new GroovyShell(config);


    String e1 = "q(name:'*/cpu.loadavg', '1m')";
    Script s1 = shell.parse(e1);

    String e2 = "avg(q(name:'*/cpu.loadavg', '1m'))";
    Script s2 = shell.parse(e2);

    String e3 = "avg(q(name:'*/cpu.loadavg', '1m')) > 1.0";
    Script s3 = shell.parse(e3);

    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
      try {

        Object result = s1.run();
        System.out.println(e1 + ": ");
        log(result);

        result = s2.run();
        System.out.println(e2 + ": ");
        log(result);

        result = s3.run();
        System.out.println(e3 + ": ");
        log(result);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }, 0, 1, TimeUnit.SECONDS);
  }



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

  Map<String, List<TagDocument>> q(Map<String, Object> params, String interval) {
    String name = (String) params.remove("name");
    return service.q(name, params, interval);
  }


  private static void log(Object object) throws IOException {
    String json = new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(object);
    System.out.println(json + "\n");
  }
}
