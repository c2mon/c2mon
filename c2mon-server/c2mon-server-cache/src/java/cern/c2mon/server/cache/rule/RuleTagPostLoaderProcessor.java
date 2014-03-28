package cern.c2mon.server.cache.rule;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.RuleTagFacade;
import cern.c2mon.server.common.rule.RuleTag;

/**
 * Manages the multi threaded loading of the rule
 * parent ids at start up.
 *  
 * @author Mark Brightwell
 *
 */
@Service
public class RuleTagPostLoaderProcessor {
  
  private static final Logger LOGGER = Logger.getLogger(RuleTagPostLoaderProcessor.class);

  private RuleTagFacade ruleTagFacade;
  
  private RuleTagCache ruleTagCache;
  
  private ClusterCache clusterCache;
  
  /**
   * Thread pool settings.
   */
  private int threadPoolMax = 16;
  private int threadPoolMin = 4;
  
  /** Cluster Cache key to avoid loading twice the parent rule ids at startup */
  public static final String ruleCachePostProcessedKey = "c2mon.cache.rule.ruleCachePostProcessed";
    
  @Autowired
  public RuleTagPostLoaderProcessor(RuleTagFacade ruleTagFacade, RuleTagCache ruleTagCache, ClusterCache clusterCache) {
    super();
    this.ruleTagFacade = ruleTagFacade;
    this.ruleTagCache = ruleTagCache;
    this.clusterCache = clusterCache;
  }

  /**
   * Loads parent ids in batches of 500 on bean creation,
   * if the distributed cache is being initialised.
   */
  @PostConstruct
  public void loadRuleParentIds() {  
    LOGGER.trace("Entering loadRuleParentIds()...");
    
    LOGGER.trace("Trying to get cache lock for " + RuleTagCache.cacheInitializedKey);
    clusterCache.acquireWriteLockOnKey(RuleTagCache.cacheInitializedKey);
    try {
      Boolean isRuleCachePostProcessed = (Boolean) clusterCache.getCopy(ruleCachePostProcessedKey);
      if (!isRuleCachePostProcessed.booleanValue()) {
        LOGGER.info("Setting parent ids for rules...");
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threadPoolMin, threadPoolMax, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000));
        LoaderTask task = new LoaderTask();
        int counter = 0;
        for (Long key : ruleTagCache.getKeys()) {      
          task.addKey(key);
          counter++;
          if (counter == 500) {              
            threadPoolExecutor.execute(task);
            task = new LoaderTask();        
            counter = 0; 
          }      
        }
        threadPoolExecutor.execute(task);
        threadPoolExecutor.shutdown();
        try {
          threadPoolExecutor.awaitTermination(1200, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          LOGGER.warn("Exception caught while waiting for rule parent id loading threads to complete (waited longer then timeout?): ", e);      
        }
        LOGGER.info("... rule parent ids set.");
        clusterCache.put(ruleCachePostProcessedKey, Boolean.TRUE);
      } else {
        LOGGER.info("Cache " + RuleTagCache.cacheInitializedKey + " was already initialized. No need for action..");
      }
    } finally {
      clusterCache.releaseWriteLockOnKey(RuleTagCache.cacheInitializedKey);
      LOGGER.trace("Released cache lock .. for " + RuleTagCache.cacheInitializedKey);
    }  
    
    LOGGER.trace("Leaving loadRuleParentIds()");
  }
  
  private class LoaderTask implements Runnable {

    private List<Long> keyList = new LinkedList<Long>();
    
    public void addKey(Long key) {
      keyList.add(key);
    }
    
    @Override
    public void run() {
      for (Long ruleKey : keyList) {
        RuleTag ruleTag = ruleTagCache.get(ruleKey);
        //if not empty, already processed
        if (ruleTag.getProcessIds().isEmpty()) {
          ruleTagFacade.setParentSupervisionIds(ruleTag);
          ruleTagCache.putQuiet(ruleTag);
        }
      }      
    }
    
  }

  /**
   * Setter method.
   * @param threadPoolMax the threadPoolMax to set
   */
  public void setThreadPoolMax(int threadPoolMax) {
    this.threadPoolMax = threadPoolMax;
  }

  /**
   * Setter method.
   * @param threadPoolMin the threadPoolMin to set
   */
  public void setThreadPoolMin(int threadPoolMin) {
    this.threadPoolMin = threadPoolMin;
  }
  
}
