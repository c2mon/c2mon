package cern.c2mon.client.core.config;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@PropertySource("classpath:c2mon-client.properties")
@ImportResource("classpath:c2mon-client.xml")
@ComponentScan("cern.c2mon")
public class C2monAutoConfiguration {

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
