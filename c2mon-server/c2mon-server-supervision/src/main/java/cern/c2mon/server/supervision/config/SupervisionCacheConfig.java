package cern.c2mon.server.supervision.config;

import cern.c2mon.server.cache.common.DefaultCacheImpl;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class SupervisionCacheConfig {

  @Autowired
  private CacheManager cacheManager;

  @Bean
  public EhCacheFactoryBean processEventEhcache() {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
    factory.setCacheName("processEventCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public DefaultCacheImpl processEventCache(Ehcache processEventEhcache) {
    return new DefaultCacheImpl(processEventEhcache);
  }

  @Bean
  public EhCacheFactoryBean equipmentEventEhcache() {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
    factory.setCacheName("equipmentEventCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public DefaultCacheImpl equipmentEventCache(Ehcache equipmentEventEhcache) {
    return new DefaultCacheImpl(equipmentEventEhcache);
  }

  @Bean
  public EhCacheFactoryBean subEquipmentEventEhcache() {
    EhCacheFactoryBean factory = new EhCacheFactoryBean();
    factory.setCacheName("subEquipmentEventCache");
    factory.setCacheManager(cacheManager);
    return factory;
  }

  @Bean
  public DefaultCacheImpl subEquipmentEventCache(Ehcache subEquipmentEventEhcache) {
    return new DefaultCacheImpl(subEquipmentEventEhcache);
  }
}
