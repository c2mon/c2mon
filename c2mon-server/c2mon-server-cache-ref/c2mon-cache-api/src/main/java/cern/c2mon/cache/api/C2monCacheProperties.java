package cern.c2mon.cache.api;


import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "c2mon.server.cache")
public class C2monCacheProperties {

  @Value("${c2mon.cache.provider}")
  private static String PROVIDER;

  /**
   * How long (in ms) the buffered cache listener should sleep between pulls
   */
  @Getter
  private int bufferedListenerPullFrequency = 5000;
}
