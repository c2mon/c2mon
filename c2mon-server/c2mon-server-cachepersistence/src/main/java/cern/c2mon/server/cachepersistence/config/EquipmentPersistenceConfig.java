package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.equipment.Equipment;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class EquipmentPersistenceConfig {

  @Bean
  public CachePersistenceDAO equipmentPersistenceDAO(EquipmentMapper equipmentMapper, EquipmentCache equipmentCache) {
    return new CachePersistenceDAOImpl<>(equipmentMapper, equipmentCache);
  }

  @Bean
  public BatchPersistenceManager equipmentPersistenceManager(CachePersistenceDAO<Equipment> equipmentPersistenceDAO,
                                                             EquipmentCache equipmentCache,
                                                             Environment environment) {
    BatchPersistenceManagerImpl manager = new BatchPersistenceManagerImpl<>(equipmentPersistenceDAO, equipmentCache);
    manager.setTimeoutPerBatch(environment.getRequiredProperty("c2mon.server.cachepersistence.timeoutPerBatch", Integer.class));
    return manager;
  }

  @Bean
  public PersistenceSynchroListener equipmentPersistenceSynchroListener(BatchPersistenceManager equipmentPersistenceManager,
                                                                        EquipmentCache equipmentCache,
                                                                        Environment environment) {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(equipmentCache, equipmentPersistenceManager, pullFrequency);
  }
}
