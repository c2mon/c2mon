package cern.c2mon.server.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import cern.c2mon.shared.client.config.ClientJmsProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.client")
public class ClientProperties {

  /**
   * JMS properties
   */
  private Jms jms = new Jms();

  @Data
  public class Jms extends ClientJmsProperties {

    /**
     * The topic prefix used to publish data tags and rules to the client. The
     * process ID will be appended.
     */
    private String tagTopicPrefix = "c2mon.client.tag";

    /** Specify the initial number of concurrent consumers to receive client requests */
    private int initialConsumers = 5;

    /** Specify the maximum number of concurrent consumers to receive client requests */
    private int maxConsumers = 10;
  }
}
