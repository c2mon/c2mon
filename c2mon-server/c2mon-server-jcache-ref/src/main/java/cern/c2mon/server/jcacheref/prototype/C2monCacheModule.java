package cern.c2mon.server.jcacheref.prototype;

import java.util.AbstractList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.spring.SpringCacheManager;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.jcacheref.prototype.alive.AliveTimerCacheRef;
import cern.c2mon.server.jcacheref.prototype.alive.C2monCacheConfiguration;
import cern.c2mon.server.jcacheref.prototype.common.BasicCache;
import cern.c2mon.server.jcacheref.various.C2monCache;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Configuration
@ComponentScan("cern.c2mon.server.jcacheref.prototype")
//@PropertySource("classpath:c2mon-cache.properties")
public class C2monCacheModule {

//  private List<C2monCacheConfiguration> cacheList;

  private IgniteConfiguration configureIgnite() {
    IgniteConfiguration configuration = new IgniteConfiguration();

    configuration.setClientMode(true);
    configuration.setMetricsLogFrequency(0); // Add that field to C2monCacheProperties

//    List<CacheConfiguration> configurations = cacheList.stream().map(C2monCacheConfiguration::getCacheConfiguration).collect(Collectors.toList());
//
//    configuration.setCacheConfiguration(configurations.toArray(new CacheConfiguration[configurations.size()]));

    return configuration;
  }

  @Bean(name = "C2monIgnite")
  public IgniteSpringBean createIgniteSpringBean() {
    IgniteSpringBean igniteSpringBean = new IgniteSpringBean();
    igniteSpringBean.setConfiguration(configureIgnite());
    return igniteSpringBean;
  }
}

