package cern.c2mon.server.cache.loader.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableConfigurationProperties(CacheLoadingProperties.class)
@ComponentScan("cern.c2mon.server.cache.loader")
public class CacheLoadingModuleRef {

  @Autowired
  private CacheLoadingProperties properties;

  @Bean
  public ThreadPoolTaskExecutor cacheLoadingThreadPoolTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(properties.getMaxThreads());
    executor.setMaxPoolSize(properties.getMaxThreads());
    executor.setKeepAliveSeconds(5);
    executor.setQueueCapacity(properties.getQueueSize());
    return executor;
  }
}
