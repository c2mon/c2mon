package cern.c2mon.server.jcacheref.prototype;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.*;

import cern.c2mon.server.jcacheref.prototype.alarm.AlarmCacheConfig;
import cern.c2mon.server.jcacheref.prototype.alive.AliveTimerCacheConfig;
import cern.c2mon.server.jcacheref.prototype.command.CommandTagCacheConfig;
import cern.c2mon.server.jcacheref.prototype.commfault.CommFaultTagCacheConfig;
import cern.c2mon.server.jcacheref.prototype.datatag.DataTagCacheConfig;

/**
 * @author Szymon Halastra
 */

@Configuration
@ComponentScan("cern.c2mon.server.jcacheref.prototype")
@PropertySource("classpath:c2mon-cache.properties")
@Import({
        DataTagCacheConfig.class,
        AlarmCacheConfig.class,
        CommFaultTagCacheConfig.class,
        AliveTimerCacheConfig.class,
        CommandTagCacheConfig.class
})
public class C2monCacheModule {

  @Value("${cache.provider}")
  private String cacheProvider ;

  @Bean(name = "springCacheManager")
  public JCacheCacheManager getSpringCacheManager(CacheManager cacheManager) {
    return new JCacheCacheManager(cacheManager);
  }

  @Bean(name = "cacheManager")
  public CacheManager getCacheManager() {
    CachingProvider provider = Caching.getCachingProvider(cacheProvider);

    return provider.getCacheManager();
  }
}

