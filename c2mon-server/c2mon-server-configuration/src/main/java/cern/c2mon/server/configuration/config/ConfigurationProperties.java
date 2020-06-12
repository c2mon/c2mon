package cern.c2mon.server.configuration.config;

import lombok.Data;

/**
 * @author Justin Lewis Salmon
 */
@Data
@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "c2mon.server.configuration")
public class ConfigurationProperties {

  /**
   * Enable/Disable sending configuration events to Process. If disabled, the
   * configuration report will always tell the user to restart the DAQ Process manually.
   */
  private boolean daqConfigEnabled = true;

  /**
   * Prevents from removing the DAQ Process configurations, if the DAQ is still running.
   * By default this is allowed.
   */
  private boolean allowRunningProcessRemoval = true;


}
