package cern.c2mon.cache.api;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.exception.MoreThanOneProviderFoundException;
import cern.c2mon.cache.api.exception.ProviderNotFoundException;
import cern.c2mon.cache.api.spi.C2monCacheProvider;

@Configuration
@EnableConfigurationProperties(C2monCacheProperties.class)
@ComponentScan("cern.c2mon.cache.api")
public class C2monCacheModule {

  @Bean
  public C2monCacheProvider provider() {
    List<C2monCacheProvider> services = new ArrayList<>();

    ServiceLoader<C2monCacheProvider> loader = ServiceLoader.load(C2monCacheProvider.class);

    loader.forEach(c2monCacheProvider -> services.add(c2monCacheProvider));

    if(services.isEmpty()) {
      throw new ProviderNotFoundException("No provider has been found.");
    }

    if(services.size() != 1) {
      throw new MoreThanOneProviderFoundException("You cannot provide more than one Cache implementation.");
    }

    return services.get(0);
  }
}
