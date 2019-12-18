package cern.c2mon.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "c2mon.server.cache")
public class CacheProperties {

  /**
   * How often the BufferedListeners should run
   */
  private int bufferedListenerPullFrequency = 5000;
}
