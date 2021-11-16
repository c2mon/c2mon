package cern.c2mon.server.supervision.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
// import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.cache.common.DefaultCacheImpl;
import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.ehcache.CacheFactory;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class SupervisionCacheConfig {

    @Autowired
    private CacheProperties properties;

    @Autowired
    private CacheManager cacheManager;

    // @Bean
    // public EhCacheFactoryBean processEventEhcache() {
    // EhCacheFactoryBean factory = new EhCacheFactoryBean();
    // factory.setCacheName("processEventCache");
    // factory.setCacheManager(cacheManager);
    // return factory;
    // }

    @Bean
    public CacheFactory processEventEhcache() {
        CacheFactory factory = new CacheFactory();
        factory.setCacheName("processEventCache");
        factory.setCacheManager(cacheManager);
        return factory;
    }

    @Bean
    public DefaultCacheImpl processEventCache(Ehcache processEventEhcache) {
        return new DefaultCacheImpl(processEventEhcache, properties);
    }

    @Bean
    public CacheFactory equipmentEventEhcache() {
        CacheFactory factory = new CacheFactory();
        factory.setCacheName("equipmentEventCache");
        factory.setCacheManager(cacheManager);
        return factory;
    }

    @Bean
    public DefaultCacheImpl equipmentEventCache(Ehcache equipmentEventEhcache) {
        return new DefaultCacheImpl(equipmentEventEhcache, properties);
    }

    @Bean
    public CacheFactory subEquipmentEventEhcache() {
        CacheFactory factory = new CacheFactory();
        factory.setCacheName("subEquipmentEventCache");
        factory.setCacheManager(cacheManager);
        return factory;
    }

    @Bean
    public DefaultCacheImpl subEquipmentEventCache(Ehcache subEquipmentEventEhcache) {
        return new DefaultCacheImpl(subEquipmentEventEhcache, properties);
    }
}
