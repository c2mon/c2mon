package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.rule.RuleTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Justin Lewis Salmon
 */
public class RuleTagPersistenceConfig {

  @Autowired
  private Environment environment;

  @Autowired
  private ClusterCache clusterCache;

  @Autowired
  private ThreadPoolTaskExecutor cachePersistenceThreadPoolTaskExecutor;

  @Autowired
  private RuleTagMapper ruleTagMapper;

  @Autowired
  private RuleTagCache ruleTagCache;

  @Bean
  public CachePersistenceDAO<RuleTag> ruleTagPersistenceDAO() {
    return new CachePersistenceDAOImpl<>(ruleTagMapper, ruleTagCache);
  }

  @Bean
  public BatchPersistenceManager ruleTagPersistenceManager() {
    return new BatchPersistenceManagerImpl<>(ruleTagPersistenceDAO(), ruleTagCache, clusterCache, cachePersistenceThreadPoolTaskExecutor);
  }

  @Bean
  public PersistenceSynchroListener ruleTagPersistenceSynchroListener() {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(ruleTagCache, ruleTagPersistenceManager(), pullFrequency);
  }
}
