package cern.c2mon.server.rule.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.rule")
public class RuleProperties {

  /**
   * Number of threads that the rule evaluation engine will use
   */
  private int numEvaluationThreads = 1;
}
