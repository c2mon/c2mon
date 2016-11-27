package cern.c2mon.server.common.expression;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.expression.Expression;
import groovy.lang.GroovyObject;
import lombok.extern.slf4j.Slf4j;

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
}
