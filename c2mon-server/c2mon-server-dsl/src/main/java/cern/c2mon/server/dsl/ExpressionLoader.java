package cern.c2mon.server.dsl;

import java.util.List;

import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.ExpressionMapper;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.expression.ExpressionCacheObject;
import cern.c2mon.server.elasticsearch.BaseScript;

/**
 * @author Martin Flamm
 */
@Slf4j
@Service
public class ExpressionLoader implements SmartLifecycle {

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  private ExpressionMapper expressionMapper;

  private final GroovyShell shell;

  private long counter;

  @Autowired
  public ExpressionLoader(ExpressionMapper expressionMapper) {
    this.expressionMapper = expressionMapper;
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(ExpressionBase.class.getName());
    this.shell = new GroovyShell(config);
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }

  @Override
  public void start() {
    if (!running) {
      running = true;

      log.info("Start populating local expression cache from database.");
      counter = System.currentTimeMillis();
      List<ExpressionCacheObject> expressions = expressionMapper.getAll();
      counter = System.currentTimeMillis() - counter;
      log.info("It took {}ms to retrieve all expressions from the database.", counter);
      log.info("Fetched {} expressions from database.", expressions.size());
      expressions.forEach(item -> {
        ExpressionCache.cacheExpression(item.getId(), item);
      });

      counter = System.currentTimeMillis();
      expressions.parallelStream().forEach(item -> {
        ExpressionCache.cacheCompiledExpression(item.getId(), shell.parse(item.getExpression()));
        log.info("Compiling {}", item.getId());
      });
      counter = System.currentTimeMillis() - counter;
      log.info("It took {}ms to compile all expressions and safe them into cache.", counter);
      log.info("Finished populating local expression cache from database.");
      stop();
    }
  }

  @Override
  public void stop() {
    log.debug("Stopping local expression cache loader.");
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST;
  }
}