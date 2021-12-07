package cern.c2mon.server.supervision.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.cache.common.DefaultCacheImpl;
import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.impl.InMemoryCache;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class SupervisionCacheConfig {

    @Autowired
    private CacheProperties properties;

    @Autowired
    private CacheManager cacheManager;


    @Bean
    public Ehcache processEventEhcache(){
        return new InMemoryCache("processEventCache");
    }

    @Bean
    public DefaultCacheImpl processEventCache(Ehcache processEventEhcache) {
        return new DefaultCacheImpl(processEventEhcache, properties);
    }

    @Bean
    public Ehcache equipmentEventEhcache(){
        return new InMemoryCache("equipmentEventCache");
    }

    @Bean
    public DefaultCacheImpl equipmentEventCache(Ehcache equipmentEventEhcache) {
        return new DefaultCacheImpl(equipmentEventEhcache, properties);
    }

    @Bean
    public Ehcache subEquipmentEventEhcache(){
        return new InMemoryCache("subEquipmentEventCache");
    }

    @Bean
    public DefaultCacheImpl subEquipmentEventCache(Ehcache subEquipmentEventEhcache) {
        return new DefaultCacheImpl(subEquipmentEventEhcache, properties);
    }
}
