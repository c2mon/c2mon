package cern.c2mon.server.cache.config.inmemory;

import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.ehcache.CacheManager;
import cern.c2mon.server.ehcache.CacheManagerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.IOException;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
//@Configuration
public class CacheManagerConfig {

  @Resource
  private CacheProperties properties;

  @Bean
  public CacheManagerFactory ehCacheManagerFactoryBean() throws IOException {
    String cacheMode = properties.getMode();

    switch (cacheMode) {
      case "single-nonpersistent":
        return getEhCacheManagerFactoryBean("ehcache/ehcache-single-nonpersistent.xml");
      case "single":
        return getEhCacheManagerFactoryBean("ehcache/ehcache-single.xml");
      case "multi":
        return getEhCacheManagerFactoryBean("ehcache/ehcache-multi.xml");
      default:
        throw new IOException(format("Unsupported cache mode specified: '%s'", cacheMode));
    }
  }

  @Bean
  public CacheManager cacheManager(){
    return new CacheManager();
  }

  private CacheManagerFactory getEhCacheManagerFactoryBean(String configLocation) {
    CacheManagerFactory bean = new CacheManagerFactory();
    //bean.setConfigLocation(new ClassPathResource(configLocation));
    //bean.setShared(true);
    return bean;
  }
}
