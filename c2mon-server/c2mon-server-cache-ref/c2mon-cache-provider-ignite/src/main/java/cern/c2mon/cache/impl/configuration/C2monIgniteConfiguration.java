package cern.c2mon.cache.impl.configuration;

import cern.c2mon.cache.impl.IgniteC2monBean;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static cern.c2mon.cache.impl.C2monCacheProperties.METRICS_LOG_FREQUENCY;

/**
 * @author Szymon Halastra
 */
@Configuration
@ComponentScan("cern.c2mon.cache.impl")
public class C2monIgniteConfiguration {

  private IgniteConfiguration configureIgnite() {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ignite-config.xml");

    IgniteConfiguration config = (IgniteConfiguration) context.getBean("base-ignite.cfg");

    config.setGridLogger(new Slf4jLogger());

    config.setMetricsLogFrequency(METRICS_LOG_FREQUENCY);

    return config;
  }

  @Bean(name = "C2monIgnite")
  public IgniteC2monBean createIgniteSpringBean() {
    return new IgniteC2monBean(configureIgnite());
  }
}
