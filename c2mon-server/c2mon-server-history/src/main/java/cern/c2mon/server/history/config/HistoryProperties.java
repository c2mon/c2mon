package cern.c2mon.server.history.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.history")
public class HistoryProperties {

  /** 
   * Location of the Tag history fallback file, which is used in case of a database connection loss.
   */
  private String tagFallbackFile = "/tmp/tag-fallback.txt";

  /** 
   * Location of the Alarm history fallback file, which is used in case of a database connection loss.
   */
  private String alarmFallbackFile = "/tmp/alarm-fallback.txt";

  /** 
   * Location of the Expression history fallback file, which is used in case of a database connection loss.
   */
  private String expressionFallbackFile = "/tmp/expression-fallback.txt";

  /** 
   * Location of the Command history fallback file, which is used in case of a database connection loss.
   */
  private String commandFallbackFile = "/tmp/command-fallback.txt";

}
