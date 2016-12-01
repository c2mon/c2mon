package cern.c2mon.server.common.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

/**
 * This class is responsible for configuring the C2MON server environment,
 * which involves processing user-defined property overrides.
 *
 * @author Justin Lewis Salmon
 */
@EnableConfigurationProperties(ServerProperties.class)
@PropertySources({
    @PropertySource("classpath:c2mon-server-default.properties"),
    @PropertySource(value = "${c2mon.server.properties}", ignoreResourceNotFound = true)
})
public class CommonModule implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  /**
   * Listens for the {@link ApplicationEnvironmentPreparedEvent} and injects
   * ${c2mon.server.properties} into the environment with the highest precedence
   * (if it exists). This is in order to allow users to point to an external
   * properties file via ${c2mon.server.properties}.
   */
  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    ConfigurableEnvironment environment = event.getEnvironment();
    String propertySource = environment.getProperty("c2mon.server.properties");

    if (propertySource != null) {
      try {
        environment.getPropertySources().addAfter("systemEnvironment", new ResourcePropertySource(propertySource));
      } catch (IOException e) {
        throw new RuntimeException("Could not read property source", e);
      }
    }
  }
}
