package cern.c2mon.server.common.expression;

import java.util.HashMap;
import java.util.Map;

import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.expression.Expression;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Franz Ritter
 */
// TODO: make the class to a service for getting access to the cache (for rules which includes more tags).
@Slf4j
public class Evaluator {

  private static GroovyShell shell = new GroovyShell();

  private Evaluator() {
  }

  public static <T extends Tag> T evaluate(T updateTag) {
    for (Expression currentExpression : updateTag.getExpressions()) {
      if (updateTag.getValue() != null) {
        if(((AbstractTagCacheObject) updateTag).getExpressionScripts().get(currentExpression.getName()) == null){
          initializeExpression(updateTag, currentExpression.getName());
        }
        Script script = ((AbstractTagCacheObject) updateTag).getExpressionScripts().get(currentExpression.getName());
        Binding currentBinding = script.getBinding();
        currentBinding.setVariable("$value", updateTag.getValue());
        Object result = script.run();
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

    return updateTag;
  }

  public static <T extends Tag> void initializeExpression(T tag, String expressionName) {
    Expression expression = tag.getExpressions().stream().filter(expr -> expr.getName().equals(expressionName)).findFirst().get();
    if(expression != null) {
      Script currentScript = shell.parse(expression.getExpression());
      Binding currentBinding = new Binding();
      currentScript.setBinding(currentBinding);
      ((AbstractTagCacheObject) tag).getExpressionScripts().put(expression.getName(), currentScript);
    } else {
      log.error("Not possible to initialize the expression {} of the tag {}", expressionName, tag.getName());
    }
  }

}
