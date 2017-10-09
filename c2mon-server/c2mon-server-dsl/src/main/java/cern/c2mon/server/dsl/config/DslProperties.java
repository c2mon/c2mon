package cern.c2mon.server.dsl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Martin Flamm
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.dsl")
public class DslProperties {

  /**
   * Script evaluation cycle in milliseconds
   */
  private long evaluationCycle = 5000;
  private boolean autoStartup = true;
}
