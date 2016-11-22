package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.rule.RuleTag;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class RuleTagPersistenceConfig {

  @Bean
  public CachePersistenceDAO ruleTagPersistenceDAO(RuleTagMapper ruleTagMapper, RuleTagCache ruleTagCache) {
    return new CachePersistenceDAOImpl<>(ruleTagMapper, ruleTagCache);
  }

  @Bean
  public BatchPersistenceManager ruleTagPersistenceManager(CachePersistenceDAO<RuleTag> ruleTagPersistenceDAO,
                                                           RuleTagCache ruleTagCache) {
    return new BatchPersistenceManagerImpl<>(ruleTagPersistenceDAO, ruleTagCache);
  }

  @Bean
  public PersistenceSynchroListener ruleTagPersistenceSynchroListener(BatchPersistenceManager ruleTagPersistenceManager,
                                                                      RuleTagCache ruleTagCache,
                                                                      Environment environment) {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(ruleTagCache, ruleTagPersistenceManager, pullFrequency);
  }
}
