package cern.c2mon.server.cache.loader.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.inject.Inject;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableConfigurationProperties(CacheLoaderProperties.class)
@ComponentScan("cern.c2mon.server.cache.loader")
public class CacheLoaderModuleRef {

  @Inject
  private CacheLoaderProperties properties;

  @Bean(name = "cacheLoaderTaskExecutor")
  public ThreadPoolTaskExecutor cacheLoaderTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(properties.getMaxThreads());
    executor.setMaxPoolSize(properties.getMaxThreads());
    executor.setQueueCapacity(properties.getQueueSize());
    executor.setKeepAliveSeconds(5);

    return executor;
  }
}
