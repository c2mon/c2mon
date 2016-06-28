package cern.c2mon.server.cache.config;

import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static java.lang.String.format;

/**
 * This class is responsible for configuring the in-memory cache.
 *
 * There are currently three possible cache "modes" available: single,
 * single-nonpersistent and multi.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
public class CacheConfig {

  @javax.annotation.Resource
  private Environment environment;

  @Bean(name = "cacheManager")
  public EhCacheManagerFactoryBean cacheManager() throws IOException {
    String cacheMode = environment.getRequiredProperty("c2mon.server.cache.mode");

    switch (cacheMode) {
      case "single-nonpersistent":
        return getEhCacheManagerFactoryBean("ehcache/ehcache-notTCmode-nonpersistent.xml");
      case "single":
        return getEhCacheManagerFactoryBean("ehcache/ehcache-notTCmode.xml");
      case "multi":
        return getEhCacheManagerFactoryBean("ehcache/ehcache-TCmode.xml");
    }

    throw new RuntimeException(format("Unsupported cache mode specified: '%s'", cacheMode));
  }

  private EhCacheManagerFactoryBean getEhCacheManagerFactoryBean(String configLocation) throws IOException {
    EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
    bean.setConfigLocation(new ClassPathResource(configLocation));
    bean.setShared(true);
    return bean;
  }
}
