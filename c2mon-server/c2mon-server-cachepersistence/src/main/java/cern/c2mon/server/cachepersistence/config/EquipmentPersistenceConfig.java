package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.equipment.Equipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class EquipmentPersistenceConfig {

  @Autowired
  private Environment environment;

  @Autowired
  private EquipmentMapper equipmentMapper;

  @Autowired
  private EquipmentCache equipmentCache;

  @Bean
  public CachePersistenceDAO<Equipment> equipmentPersistenceDAO() {
    return new CachePersistenceDAOImpl<>(equipmentMapper, equipmentCache);
  }

  @Bean
  public BatchPersistenceManager equipmentPersistenceManager() {
    BatchPersistenceManagerImpl manager = new BatchPersistenceManagerImpl<>(equipmentPersistenceDAO(), equipmentCache);
    manager.setTimeoutPerBatch(environment.getRequiredProperty("c2mon.server.cachepersistence.timeoutPerBatch", Integer.class));
    return manager;
  }

  @Bean
  public PersistenceSynchroListener equipmentPersistenceSynchroListener() {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(equipmentCache, equipmentPersistenceManager(), pullFrequency);
  }
}
