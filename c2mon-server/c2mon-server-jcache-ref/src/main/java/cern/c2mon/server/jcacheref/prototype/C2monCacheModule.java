package cern.c2mon.server.jcacheref.prototype;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
@EnableAutoConfiguration(exclude = {HazelcastAutoConfiguration.class})
@Import({
        DataTagCacheConfig.class,
        AlarmCacheConfig.class,
        CommFaultTagCacheConfig.class,
        AliveTimerCacheConfig.class,
        CommandTagCacheConfig.class
})
@EnableConfigurationProperties(CacheProperties.class)
public class C2monCacheModule {

  @Value("${cache.provider}")
  private String cacheProvider;

  @Bean(name = "springCacheManager")
  public JCacheCacheManager getSpringCacheManager(CacheManager cacheManager) {
    JCacheCacheManager jCacheCacheManager = new JCacheCacheManager(cacheManager);

    return jCacheCacheManager;
  }

  @Bean(name = "cacheManager")
  public CacheManager getCacheManager() {
    CachingProvider provider = Caching.getCachingProvider(cacheProvider);

    return provider.getCacheManager();
  }
}

