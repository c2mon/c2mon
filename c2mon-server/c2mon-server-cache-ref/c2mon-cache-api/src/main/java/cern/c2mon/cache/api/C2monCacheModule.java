package cern.c2mon.cache.api;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import cern.c2mon.cache.api.exception.MoreThanOneProviderFoundException;
import cern.c2mon.cache.api.exception.ProviderNotFoundException;
import cern.c2mon.cache.api.spi.C2monCacheProvider;

@Slf4j
@Configuration
@EnableConfigurationProperties(C2monCacheProperties.class)
@ComponentScan("cern.c2mon.cache.api")
public class C2monCacheModule {

  @Bean
  public C2monCacheProvider c2monCacheProvider() {
    C2monCacheProvider provider;
    String providerName = this.getLoadedProviderName();
    try {
      Class<?> instance = ClassUtils.forName(providerName, null);
      Assert.isAssignable(C2monCacheProvider.class, instance);
      provider = (C2monCacheProvider) instance.newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot instantiate " + providerName, e);
    }

    return provider;
  }

  private String getLoadedProviderName() {
    List<String> names = new ArrayList<>(SpringFactoriesLoader.loadFactoryNames(C2monCacheProvider.class, null));

    if(names.isEmpty()) {
      throw new ProviderNotFoundException("Cannot find any C2monCacheProvider implementation");
    }

    if(names.size() > 1) {
      throw new MoreThanOneProviderFoundException("Found more than one provider while only one implementation is allowed");
    }

    return names.get(0);
  }
}
