package cern.c2mon.server.cache.expression;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.common.expression.LocalExpressionCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Franz Ritter
 */
@Service
public class LocalExpressionCacheScheduler {

  private static final long DEFAULT_TIMER = 1L;

  ScheduledThreadPoolExecutor executor;

  private DataTagCache dataTagCache;
  private RuleTagCache ruleTagCache;
  private ControlTagCache controlTagCache;

  @Autowired
  public LocalExpressionCacheScheduler(DataTagCache dataTagCache,
                                       RuleTagCache ruleTagCache,
                                       ControlTagCache controlTagCache) {

    this.executor = new ScheduledThreadPoolExecutor(1);
    this.dataTagCache = dataTagCache;
    this.ruleTagCache = ruleTagCache;
    this.controlTagCache = controlTagCache;
    executor.scheduleAtFixedRate(this::clearLocalCache, DEFAULT_TIMER, DEFAULT_TIMER, TimeUnit.MINUTES);
  }

  private void clearLocalCache() {

    List<Long> staleIds = LocalExpressionCache.getAllIds()
        .stream()
        .filter(key -> !(dataTagCache.hasKey(key) || ruleTagCache.hasKey(key) || controlTagCache.hasKey(key)))
        .collect(Collectors.toList());
    LocalExpressionCache.removeStaleTags(staleIds);
  }

}
