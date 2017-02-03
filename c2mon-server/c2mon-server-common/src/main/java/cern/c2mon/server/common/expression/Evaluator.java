package cern.c2mon.server.common.expression;

import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagConstants;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Ritter
 */
@Slf4j
public class Evaluator {

  /**
   * Evaluates all expressions of a given tag.
   *
   * Note that this method modifies the tag object directly, it does not make
   * a copy.
   *
   * @param tag the tag with the expressions which needs to be evaluated
   * @return reference to the given (potentially modified) tag
   */
  public static <T extends Tag> T evaluate(T tag) {
    if (tag.getExpressions().isEmpty()) {
      return tag;
    }

    Long tagId = tag.getId();
    LocalExpressionCache.initializeTag(tag);

    for (Expression expression : tag.getExpressions()) {
      if (tag.getValue() != null) {

        LocalExpressionCache.addExpressionToTag(tag, expression);

        GroovyObject script = LocalExpressionCache.getScript(tagId, expression.getName());
        Object result = invoke(script, tag.getValue());

        if (result instanceof Boolean) {
          expression.setResult(result);
        } else {
          Object type = result != null ? result.getClass() : null;
          throw new IllegalArgumentException("Expected boolean expression result, got " + type);
        }
      } else {
        log.warn("Tried to evaluate expression '{}' on tag #{} with a null value", tag.getId(), tag.getName());
      }
    }

    return tag;
  }

  private static Object invoke(GroovyObject script, Object arg) {
    return script.invokeMethod("run", new Object[]{arg});
  }

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

    DataTagCacheObject tag = new DataTagCacheObject(210002L, "mem.avail", Float.class.getName(), DataTagConstants.MODE_OPERATIONAL);
    tag.setValue(2);
    Expression expression = new Expression("test-expression", "$value > 1");
    tag.setExpressions(Collections.singletonList(expression));

    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.WARN);

    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(BaseScript.class.getName());

    GroovyShell shell = new GroovyShell(config);


    String e1 = "q(name:'cpu.loadavg', location:'864', '5m')";
    Script s1 = shell.parse(e1);

    String e2 = "avg(q(name:'cpu.loadavg', location:'864', '5m'))";
    Script s2 = shell.parse(e2);

    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
      try {
        Object result = s1.run();
        System.out.println(e1 + ": " + result);

        result = s2.run();
        System.out.println(e2 + ": " + result);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }, 0, 1, TimeUnit.SECONDS);
  }
}
