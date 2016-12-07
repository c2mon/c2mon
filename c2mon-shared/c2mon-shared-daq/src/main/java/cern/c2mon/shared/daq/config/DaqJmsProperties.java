package cern.c2mon.shared.daq.config;

import lombok.Data;

/**
 * @author Justin Lewis Salmon
 */
@Data
public class DaqJmsProperties {

  /**
   * URL of the primary JMS broker to which to publish
   */
  private String url = "tcp://localhost:61616";

  /**
   * The queue prefix used to publish data tags to the server. The process ID
   * will be appended to this value
   */
  private String queuePrefix = "c2mon.process";
}
