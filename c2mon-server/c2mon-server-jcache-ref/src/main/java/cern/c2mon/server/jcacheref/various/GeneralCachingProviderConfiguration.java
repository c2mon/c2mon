package cern.c2mon.server.jcacheref.various;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Szymon Halastra
 */
public class GeneralCachingProviderConfiguration {

  @Value("${cache.provider}")
  private String provider;

  public void checkProvider() {
    if(provider.contains("Hazelcast")) {
      //DO Hzelcast configuration
    } else if(provider.contains("Ignite")) {
      //DO Ignite configuration
    }
  }
}
