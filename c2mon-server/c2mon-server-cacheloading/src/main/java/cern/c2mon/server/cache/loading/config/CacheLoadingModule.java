package cern.c2mon.server.cache.loading.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Justin Lewis Salmon
 */
@ComponentScan("cern.c2mon.server.cache.loading")
public class CacheLoadingModule {

  @Autowired
  private Environment environment;

  @Bean
  public ThreadPoolTaskExecutor cacheLoadingThreadPoolTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(environment.getRequiredProperty("c2mon.server.cacheloading.maxThreads", Integer.class));
    executor.setMaxPoolSize(environment.getRequiredProperty("c2mon.server.cacheloading.maxThreads", Integer.class));
    executor.setKeepAliveSeconds(5);
    executor.setQueueCapacity(environment.getRequiredProperty("c2mon.server.cacheloading.queueSize", Integer.class));
    return executor;
  }
}
