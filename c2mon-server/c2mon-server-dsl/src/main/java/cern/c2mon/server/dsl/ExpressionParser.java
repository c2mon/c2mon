package cern.c2mon.server.dsl;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.stereotype.Component;

import cern.c2mon.server.common.expression.ExpressionCacheObject;

/**
 * @author Martin Flamm
 */
@Slf4j
@Component
public class ExpressionParser {

  private final GroovyShell shell;

  public ExpressionParser() {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(ExpressionBase.class.getName());
    this.shell = new GroovyShell(config);
  }

  public Script compileExpression(ExpressionCacheObject expression) {
    return shell.parse(expression.getExpression());
  }
}
