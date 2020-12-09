package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.dbaccess.DeviceClassMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.device.DeviceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceClassPersistenceConfig extends AbstractPersistenceConfig {

  @Autowired
  private DeviceClassMapper deviceClassMapper;

  @Autowired
  private DeviceClassCache deviceClassCache;

  @Bean
  public CachePersistenceDAO<DeviceClass> deviceClassPersistenceDAO() {
    return new CachePersistenceDAOImpl<>(deviceClassMapper, deviceClassCache);
  }

  @Bean
  public BatchPersistenceManager deviceClassPersistenceManager() {
    BatchPersistenceManagerImpl<DeviceClass> manager = new BatchPersistenceManagerImpl<>(deviceClassPersistenceDAO(), deviceClassCache,
            clusterCache, cachePersistenceThreadPoolTaskExecutor);
    manager.setTimeoutPerBatch(properties.getTimeoutPerBatch());
    return manager;
  }

  @Bean
  public PersistenceSynchroListener deviceCachePersistenceSynchroListener() {
    Integer pullFrequency = cacheProperties.getBufferedListenerPullFrequency();
    return new PersistenceSynchroListener(deviceClassCache, deviceClassPersistenceManager(), pullFrequency);
  }
}
