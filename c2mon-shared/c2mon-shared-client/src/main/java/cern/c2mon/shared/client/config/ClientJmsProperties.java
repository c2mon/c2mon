package cern.c2mon.shared.client.config;

import lombok.Data;

/**
 * @author Justin Lewis Salmon
 */
@Data
public class ClientJmsProperties extends ClientProperties {

  /**
   * URL of the JMS broker
   */
  private String url = "tcp://localhost:61616";
}
