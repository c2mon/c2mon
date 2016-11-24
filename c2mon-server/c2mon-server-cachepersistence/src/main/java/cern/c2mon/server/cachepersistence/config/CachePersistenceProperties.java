package cern.c2mon.server.cachepersistence.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Justin Lewis Salmon
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "c2mon.server.cachepersistence")
public class CachePersistenceProperties {

  /**
   * Timeout (in milliseconds) for a single batch to persist
   */
  private int timeoutPerBatch = 30000;

  private int numExecutorThreads = 1;

  private int keepAliveSeconds = 5;

  private int queueCapacity = 1000;
}
