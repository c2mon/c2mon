package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.dbaccess.SubEquipmentMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.subequipment.SubEquipment;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class SubEquipmentPersistenceConfig {

  @Bean
  public CachePersistenceDAO subEquipmentPersistenceDAO(SubEquipmentMapper subEquipmentMapper, SubEquipmentCache subEquipmentCache) {
    return new CachePersistenceDAOImpl<>(subEquipmentMapper, subEquipmentCache);
  }

  @Bean
  public BatchPersistenceManager subEquipmentPersistenceManager(CachePersistenceDAO<SubEquipment> subEquipmentPersistenceDAO,
                                                                SubEquipmentCache subEquipmentCache,
                                                                Environment environment) {
    BatchPersistenceManagerImpl manager = new BatchPersistenceManagerImpl<>(subEquipmentPersistenceDAO, subEquipmentCache);
    manager.setTimeoutPerBatch(environment.getRequiredProperty("c2mon.server.cachepersistence.timeoutPerBatch", Integer.class));
    return manager;
  }

  @Bean
  public PersistenceSynchroListener subEquipmentPersistenceSynchroListener(BatchPersistenceManager subEquipmentPersistenceManager,
                                                                           SubEquipmentCache subEquipmentCache,
                                                                           Environment environment) {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(subEquipmentCache, subEquipmentPersistenceManager, pullFrequency);
  }
}
