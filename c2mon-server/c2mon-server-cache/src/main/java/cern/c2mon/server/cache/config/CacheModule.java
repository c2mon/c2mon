package cern.c2mon.server.cache.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Justin Lewis Salmon
 */
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
@ComponentScan("cern.c2mon.server.cache")
public class CacheModule {}
