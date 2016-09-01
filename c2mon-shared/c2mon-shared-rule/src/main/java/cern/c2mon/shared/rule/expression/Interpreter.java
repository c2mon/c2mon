package cern.c2mon.shared.rule.expression;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * Parodies the functionality to run a groovy script dynamic in run time and return a result.
 *
 * @author Franz Ritter
 */
public class Interpreter {

  /**
   * Takes a expression which needs one argument parameter and evaluates it.
   * The result has to be a boolean and the parameter in the expression has has to be named '$value'.
   *
   * @param expression The groovy based expression which will be evaluated.
   * @param value The parameter which will be injected into the expression.
   * @return The result of the evaluated expression.
   */
  public static Object evaluateExpression(String expression, Object value) {

    Binding binding = new Binding();
    binding.setVariable("$value", value);

    GroovyShell shell = new GroovyShell(binding);
    return shell.evaluate(expression);
  }
}
