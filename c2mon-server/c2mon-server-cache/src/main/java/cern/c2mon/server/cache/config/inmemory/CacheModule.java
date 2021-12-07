package cern.c2mon.server.cache.config.inmemory;

import cern.c2mon.server.cache.config.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * @author Justin Lewis Salmon
 */
@Profile("!ignite")
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
@EnableConfigurationProperties(CacheProperties.class)
@ComponentScan("cern.c2mon.server.cache")
public class CacheModule {}
