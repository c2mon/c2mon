package cern.c2mon.server.common.expression;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import lombok.extern.slf4j.Slf4j;

/**
 * A Factory to create objects which can evaluate expressions.
 *
 * @author Franz Ritter
 */
@Slf4j
public class ExpressionFactory {

  private static GroovyClassLoader classLoader = new GroovyClassLoader();

  private static String buildGroovyClass(String expression) {
    return "class Foo implements Serializable {" +
        "" +
        " Object run(Object arg) { " +
        expression.replace("$value", "arg") +
        "}" +
        "}";
  }

  /**
   * Compile an invokable {@link GroovyObject} object from a given expression.
   *
   * @param expression the expression to evaluate
   * @return an invokable {@link GroovyObject}
   */
  public static GroovyObject createScript(String expression) {
    String temporaryClass = buildGroovyClass(expression);
    Class clazz = classLoader.parseClass(temporaryClass);

    try {
      return (GroovyObject) clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Error creating script", e);
    }
  }
}
