package cern.c2mon.client.core.config;

import org.springframework.context.annotation.*;
import org.springframework.context.support.DefaultLifecycleProcessor;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@PropertySources({
    @PropertySource("classpath:c2mon-client.properties"),
    @PropertySource(value = "${c2mon.client.conf.url}", ignoreResourceNotFound = true)}
)
@ImportResource("classpath:c2mon-client.xml")
public class C2monAutoConfiguration {

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
