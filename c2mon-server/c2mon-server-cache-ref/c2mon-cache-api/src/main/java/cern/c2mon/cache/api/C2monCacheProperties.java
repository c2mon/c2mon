package cern.c2mon.cache.api;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "c2mon.server.cache.provider")
public class C2monCacheProperties {

  @Value("${c2mon.cache.provider}")
  private static String PROVIDER;
}
