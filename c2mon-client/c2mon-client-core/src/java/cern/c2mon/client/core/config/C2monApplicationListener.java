package cern.c2mon.client.core.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

/**
 * Listens for the {@link ApplicationEnvironmentPreparedEvent} and injects ${c2mon.client.conf.url}
 * into the environment with the highest precedence (if it exists)
 *
 * @author Justin Lewis Salmon
 */
@Configuration
public class C2monApplicationListener implements ApplicationListener {

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (event instanceof ApplicationEnvironmentPreparedEvent) {
      ConfigurableEnvironment environment = ((ApplicationEnvironmentPreparedEvent) event).getEnvironment();
      String propertySource = environment.getProperty("c2mon.client.conf.url");

      if (propertySource != null) {
        try {
          environment.getPropertySources().addAfter("systemEnvironment", new ResourcePropertySource(propertySource));
        } catch (IOException e) {
          throw new RuntimeException("Could not read property source", e);
        }
      }
    }
  }
}

