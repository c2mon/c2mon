package cern.c2mon.server.dsl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.event.ObjectChangeListener;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.RuleTagFacade;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.dbaccess.ExpressionMapper;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.expression.ExpressionCacheObject;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.dsl.config.DslProperties;
import cern.c2mon.server.elasticsearch.BaseScript;
import cern.c2mon.server.elasticsearch.ElasticsearchService;

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
  private ExpressionLoader expressionLoader;

  private final GroovyShell shell;
  //  String e3 = "(avg(q(name:'*/cpu.loadavg', '1m'))> 3) && (avg(q(name:'*/cpu.temp', '1m'))> 40)";
//  Script s3;
  private long counter;

  @Autowired
  public ExpressionEvaluatorTask(RuleTagCache ruleTagCache, TagLocationService tagLocationService, DslProperties properties, ElasticsearchService elasticsearchService, ExpressionMapper expressionMapper, ExpressionLoader expressionLoader) {
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
  }

  @Override
  public void run() {
    if (!expressionLoader.isRunning() && !ExpressionCache.isEmpty()) {
      try {
        counter = System.currentTimeMillis();
        ExpressionCache.getCompiledExpressions().forEach((k, v) -> {
          ExpressionCacheObject tempExpression = ExpressionCache.getExpression(k);
          Object temp = v.run();
          tempExpression.setValue(temp);
          ExpressionCache.cacheExpression(k, tempExpression);
          ruleTagCache.putQuiet(tempExpression);
          //expressionMapper.updateCacheable(tempExpression);
          RuleTag ruletag = ruleTagCache.get(tempExpression.getId());
          log.info("Evaluating expression #{} with a value of {}", k, tempExpression.getValue());
        });
        counter = System.currentTimeMillis() - counter;
        log.info("Evaluating all expressions took {}s.", counter / 1000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
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