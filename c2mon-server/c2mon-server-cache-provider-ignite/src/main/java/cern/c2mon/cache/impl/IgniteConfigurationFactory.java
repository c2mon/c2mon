package cern.c2mon.cache.impl;

import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static cern.c2mon.cache.impl.C2monCacheProperties.METRICS_LOG_FREQUENCY;

@Configuration
public class IgniteConfigurationFactory {

  @Bean
  public IgniteConfiguration defaultConfiguration() {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ignite-config.xml");

    IgniteConfiguration config = (IgniteConfiguration) context.getBean("base-ignite.cfg");

    config.setGridLogger(new Slf4jLogger());

    config.setMetricsLogFrequency(METRICS_LOG_FREQUENCY);

    return config;
  }
}
