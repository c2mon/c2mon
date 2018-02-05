package cern.c2mon.server.drools.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * This class is responsible for configuring the Spring context for the
 * Elasticsearch module.
 *
 * @author Justin Lewis Salmon
 * @author Alban Marguet
 */
@Configuration
@EnableConfigurationProperties(DroolsProperties.class)
@ComponentScan("cern.c2mon.server.drools")
public class DroolsModule {

  @PostConstruct
  public void init() {
    System.out.println("TEST");
  }

}