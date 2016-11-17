package cern.c2mon.server.common.expression;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

/**
 * A Factory to create objects which can evaluate expressions.
 *
 * @author Franz Ritter
 */
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
   * Builds dynamically an object which can evaluate an expression.
   *
   * @param expression The expression which needs to be evaluated from the object
   * @return A object with a run method to evaluate the given expression.=
   */
  public static GroovyObject createScript(String expression) {

    String temporaryClass = buildGroovyClass(expression);
    Class clazz = classLoader.parseClass(temporaryClass);
    try {
      return (GroovyObject) clazz.newInstance();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

}
