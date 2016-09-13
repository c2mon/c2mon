package cern.c2mon.shared.rule.expression;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * Provides the functionality to run a groovy script on runtime.
 *
 * @author Franz Ritter
 */
// TODO use the 'Script' class to avoid repeated groovy compilation
public class Interpreter {

  /**
   * Takes a expression which needs one argument parameter and evaluates it.
   * The result has to be a boolean and the parameter in the expression has has
   * to be named '$value'.
   *
   * @param expression the groovy based expression which will be evaluated
   * @param value      the parameter which will be injected into the expression
   * @return The result of the evaluated expression.
   */
  public static Boolean evaluateExpression(String expression, Object value) {

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
