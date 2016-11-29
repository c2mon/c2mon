package cern.c2mon.server.cachepersistence.config;

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
import org.springframework.context.annotation.Configuration;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class RuleTagPersistenceConfig extends AbstractPersistenceConfig {

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
    Integer pullFrequency = cacheProperties.getBufferedListenerPullFrequency();
    return new PersistenceSynchroListener(ruleTagCache, ruleTagPersistenceManager(), pullFrequency);
  }
}
