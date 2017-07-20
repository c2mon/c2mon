package cern.c2mon.server.jcacheref.prototype;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Configuration
@ComponentScan("cern.c2mon.server.jcacheref.prototype")
//@PropertySource("classpath:c2mon-cache.properties")
public class C2monCacheModule {

  private IgniteConfiguration configureIgnite() {
    IgniteConfiguration configuration = new IgniteConfiguration();

    configuration.setClientMode(true);
    configuration.setMetricsLogFrequency(0); // Add that field to C2monCacheProperties

    return configuration;
  }

  @Bean(name = "C2monIgnite")
  public IgniteSpringBean createIgniteSpringBean() {
    IgniteSpringBean igniteSpringBean = new IgniteSpringBean();
    igniteSpringBean.setConfiguration(configureIgnite());
    return igniteSpringBean;
  }
}

