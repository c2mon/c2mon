package cern.c2mon.shared.daq.config;

import lombok.Data;

import cern.c2mon.shared.common.config.CommonJmsProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
public class DaqJmsProperties extends CommonJmsProperties {

  /**
   * The queue prefix used to publish data tags to the server. The process ID
   * will be appended to this value
   */
  private String queuePrefix = "c2mon.process";
}
