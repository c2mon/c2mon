package cern.c2mon.server.dsl;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.dbaccess.ExpressionMapper;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.expression.ExpressionCacheObject;

/**
 * @author Martin Flamm
 */
@Slf4j
@Component
public class ExpressionLoaderTask implements SmartLifecycle {

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  private final RuleTagCache ruleTagCache;

  private ExpressionMapper expressionMapper;
  private ExpressionParser compiler;

  private final GroovyShell shell;

  private long counter;
  private final ForkJoinPool threadPool;

  @Autowired
  public ExpressionLoaderTask(ExpressionMapper expressionMapper, RuleTagCache ruleTagCache, ExpressionParser compiler) {
    this.expressionMapper = expressionMapper;
    this.compiler = compiler;
    this.ruleTagCache = ruleTagCache;
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(ExpressionBase.class.getName());
    this.shell = new GroovyShell(config);
    this.threadPool = new ForkJoinPool(10);
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

      log.info("Start populating local expression cache from database");
      counter = System.currentTimeMillis();
      List<ExpressionCacheObject> expressions = expressionMapper.getAll();
      counter = System.currentTimeMillis() - counter;
      log.info("It took {}ms to retrieve {} expressions from the database", counter, expressions.size());

      counter = System.currentTimeMillis();
      threadPool.submit(() -> expressions.parallelStream()
          .forEach(item -> {
        ruleTagCache.acquireWriteLockOnKey(item.getId());
        ExpressionCache.cacheCompiledExpression(item.getId(), compiler.compileExpression(item));
        ruleTagCache.putQuiet(item);
        ruleTagCache.releaseWriteLockOnKey(item.getId());
        ExpressionCache.cacheExpressionId(item.getId());
        log.info("Compiling expression #{}", item.getId());
      }));

      counter = System.currentTimeMillis() - counter;
      if (!expressions.isEmpty()) {
        log.info("It took {}ms to compile all expressions and safe them into rule cache", counter);
      }
      log.info("Finished populating local expression cache from database");
      stop();
    }
  }

  @Override
  public void stop() {
    log.debug("Stopping local expression cache loader");
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