package cern.c2mon.server.cache.config.ignite;

import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * @author Justin Lewis Salmon
 */
@Profile("ignite")
@Configuration
@Import({
    CacheManagerConfig.class,
    ClusterCacheConfig.class,
    ProcessCacheConfig.class,
    EquipmentCacheConfig.class,
    SubEquipmentCacheConfig.class,
    DataTagCacheConfig.class,
    AlarmCacheConfig.class,
    RuleTagCacheConfig.class,
    ControlTagCacheConfig.class,
    CommFaultTagCacheConfig.class,
    AliveTimerCacheConfig.class,
    CommandTagCacheConfig.class,
    DeviceClassCacheConfig.class,
    DeviceCacheConfig.class
})
@EnableConfigurationProperties({CacheProperties.class, IgniteCacheProperties.class})
@ComponentScan({"cern.c2mon.server.cache", "cern.c2mon.server.ehcache"})
public class CacheModule {}
