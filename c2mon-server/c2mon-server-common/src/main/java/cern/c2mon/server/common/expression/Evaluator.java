package cern.c2mon.server.common.expression;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.expression.Expression;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Franz Ritter
 */
// TODO: make the class to a service for getting access to the cache (for rules which includes more tags).
@Slf4j
public class Evaluator {

  private Evaluator(){}

  public static<T extends Tag> T evaluate(T updateTag) {
    for (Expression expression : updateTag.getExpressions()) {
      if (updateTag.getValue() != null) {
        Boolean newEvaluation = evaluateExpression(expression.getExpression(), updateTag.getValue());
        expression.setResult(newEvaluation);
      } else {
        log.warn("Tried to evaluate a expression with a null value: tag id:{}, tag name:{}",
            updateTag.getId(), updateTag.getName());
      }
    }

    return updateTag;
  }

  /**
   * Takes a expression which needs one argument parameter and evaluates it.
   * The result has to be a boolean and the parameter in the expression has
   * to be named '$value'.
   *
   * @param expression the groovy based expression which will be evaluated
   * @param value      the parameter which will be injected into the expression
   * @return The result of the evaluated expression.
   */
  // TODO use the 'Script' class to avoid repeated groovy compilation
  private static Boolean evaluateExpression(String expression, Object value) {

    Binding binding = new Binding();
    binding.setVariable("$value", value);

    GroovyShell shell = new GroovyShell(binding);
    Object result = shell.evaluate(expression);
    if (result instanceof Boolean) {
      return (Boolean) result;
    } else {
      Object type = result != null ? result.getClass() : null;
      throw new IllegalArgumentException("Expression does not evaluate to a boolean. The result is " + type);
    }

  }

}
