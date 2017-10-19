package cern.c2mon.server.dsl;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.dbaccess.ExpressionMapper;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.expression.ExpressionCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.dsl.config.DslProperties;
import cern.c2mon.server.elasticsearch.BaseScript;
import cern.c2mon.server.elasticsearch.ElasticsearchService;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

/**
 * @author Martin Flamm
 */

@Slf4j
@Service
public class ExpressionEvaluatorTask extends TimerTask implements SmartLifecycle {

  /**
   * Timer object
   */
  private Timer timer;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  private final RuleTagCache ruleTagCache;

  private final DslProperties properties;

  private final TagLocationService tagLocationService;

  private final ElasticsearchService elasticsearchService;

  private ExpressionMapper expressionMapper;
  private ExpressionLoaderTask expressionLoader;

  private final GroovyShell shell;
  //  String e3 = "(avg(q(name:'*/cpu.loadavg', '1m'))> 3) && (avg(q(name:'*/cpu.temp', '1m'))> 40)";
//  Script s3;
  private long counter;
  private final ForkJoinPool threadPool;

  @Autowired
  public ExpressionEvaluatorTask(RuleTagCache ruleTagCache, TagLocationService tagLocationService, DslProperties properties, ElasticsearchService elasticsearchService, ExpressionMapper expressionMapper, ExpressionLoaderTask expressionLoader) {
    super();
    this.ruleTagCache = ruleTagCache;
    this.properties = properties;
    this.tagLocationService = tagLocationService;
    this.elasticsearchService = elasticsearchService;
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(BaseScript.class.getName());
    this.shell = new GroovyShell(config);
    this.expressionMapper = expressionMapper;
    this.expressionLoader = expressionLoader;
    //this.s3 =  shell.parse(e3);
    this.threadPool = new ForkJoinPool(10);
  }

  @Override
  public void run() {
    if (!expressionLoader.isRunning() && !ExpressionCache.isEmpty()) {
      try {

        Map<Long, Script> compiledExpressions = ExpressionCache.getCompiledExpressions();

        counter = System.currentTimeMillis();
        threadPool.awaitTermination(10, TimeUnit.SECONDS);
        threadPool.submit(() -> compiledExpressions.entrySet().parallelStream()
            .forEach(entry -> updateCacheValue(entry.getKey(), runScript(entry.getValue())))
        ).get(5, TimeUnit.MINUTES);

        counter = System.currentTimeMillis() - counter;
//        compiledExpressions.entrySet().parallelStream()
//            .forEach(entry -> updateCacheValue(entry.getKey(), runScript(entry.getValue())));

//        compiledExpressions.forEach((k, v) -> {
//          if (ruleTagCache.get(k) instanceof ExpressionCacheObject) {
//            ExpressionCacheObject cachedExpression = (ExpressionCacheObject) ruleTagCache.get(k);
//            Object temp = v.run();
//            cachedExpression.setValue(temp);
//            //ExpressionCache.cacheExpressionId(k, tempExpression);
//            ruleTagCache.put(cachedExpression.getId(), cachedExpression);
//            RuleTag ruletag = ruleTagCache.get(cachedExpression.getId());
//            if(cachedExpression.getId() % 150 == 0) log.info("{} left out of {}", idx.decrementAndGet(), compiledExpressions.size());
//          }
//        });


        //log.info("The following {} expression IDs are stored in the ruleTagCache {}", ExpressionCache.getExpressionIds().size(), ExpressionCache.getExpressionIds());
//        for(Long id : ExpressionCache.getExpressionIds()) {
//          log.info("#{}, value {}", id, ruleTagCache.get(id).getValue());
//        }
        log.info("Evaluating {} expressions took {}ms.", compiledExpressions.size(), counter);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void updateCacheValue(Long itemId, Object newValue) {
    ruleTagCache.acquireWriteLockOnKey(itemId);
    getExpressionFromCache(itemId).ifPresent(cachedExpression -> updateExpressionCacheValue(newValue, cachedExpression));
    ruleTagCache.releaseWriteLockOnKey(itemId);
  }

  private void updateExpressionCacheValue(Object newValue, ExpressionCacheObject cachedExpression) {
    cachedExpression.setValue(newValue);
    DataTagQuality dataTagQuality = new DataTagQualityImpl();
    dataTagQuality.validate();
    cachedExpression.setDataTagQuality(dataTagQuality);
    expressionMapper.updateConfig(cachedExpression);
    ruleTagCache.put(cachedExpression.getId(), cachedExpression);
  }


  private Optional<ExpressionCacheObject> getExpressionFromCache(Long id) {
    RuleTag tag = ruleTagCache.get(id);
    if (tag instanceof ExpressionCacheObject) {
      return Optional.of((ExpressionCacheObject) tag);
    }
    return Optional.empty();
  }

  private Object runScript(Script value) {
    return value.run();
  }

  private void cacheExpression(ExpressionCacheObject expression) {
    ruleTagCache.put(expression.getId(), expression);
  }

  @Override
  public boolean isAutoStartup() {
    return properties.isAutoStartup();
  }

  @Override
  public void stop(Runnable runnable) {
    this.stop();
    runnable.run();
  }

  @Override
  public void stop() {
    log.info("Stopping the C2MON script evaluation.");
    threadPool.shutdown();
    timer.cancel();
    running = false;
  }

  @Override
  public void start() {
    log.info("Starting the C2MON script evaluation.");
    timer = new Timer("Evaluator");
    timer.schedule(this, properties.getEvaluationCycle(), properties.getEvaluationCycle());
    running = true;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_INTERMEDIATE;
  }

  /*private ExpressionCacheObject createExpression(long id, String expressionString) {
    ExpressionCacheObject expression = new ExpressionCacheObject();
    expression.setId(id);
    expression.setExpression(expressionString);

    Metadata metadata = new Metadata();
    metadata.addMetadata("responsible", "Joe Bloggs");
    expression.setMetadata(metadata);

    expression.setAlarmIds(new ArrayList<>(Arrays.asList(1L, 2L, 3L)));
    expression.setDescription("This is a expression description");
    expression.setDataType("java.lang.Float");
    expression.setVersion(12525216526L);
    expression.setResult(22.25452F);
    return expression;
  }*/
}