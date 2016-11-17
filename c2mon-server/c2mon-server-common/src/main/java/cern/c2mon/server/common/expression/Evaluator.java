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

  private Evaluator() {
  }

  /**
   * Evaluates all expressions of an given tag.
   * The result of the expression will directly written to the given tag as a
   * side effect.
   *
   * @param updateTag The tag with the expressions which needs to be evaluated
   * @return
   */
  public static <T extends Tag> T evaluate(T updateTag) {
    if (!updateTag.getExpressions().isEmpty()) {
      Long tagId = updateTag.getId();
      LocalExpressionCache.initializeLocalCache(tagId);
      LocalExpressionCache.checkLocalCacheConsistency(updateTag);

      for (Expression currentExpression : updateTag.getExpressions()) {
        if (updateTag.getValue() != null) {

          LocalExpressionCache.checkLocalCounter(updateTag, currentExpression.getName());
          LocalExpressionCache.checkScriptState(updateTag, currentExpression);

          GroovyObject script = LocalExpressionCache.getScript(tagId, currentExpression.getName());
          Object result = invokeScript(script, updateTag.getValue());
          if (result instanceof Boolean) {
            currentExpression.setResult(result);
          } else {
            Object type = result != null ? result.getClass() : null;
            throw new IllegalArgumentException("Expression does not evaluate to a boolean. The result is " + type);
          }
        } else {
          log.warn("Tried to evaluate a expression with a null value: tag id:{}, tag name:{}",
              updateTag.getId(), updateTag.getName());
        }
      }
    }
    return updateTag;
  }

  private static Object invokeScript(GroovyObject script, Object arg) {
    return script.invokeMethod("run", new Object[]{arg});
  }
}
