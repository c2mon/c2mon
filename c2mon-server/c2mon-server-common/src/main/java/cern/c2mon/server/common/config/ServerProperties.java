package cern.c2mon.server.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server")
public class ServerProperties {

  /**
   * The name of this C2MON server node
   */
  private String name = "C2MON-SERVER";

  /**
   * Absolute path the the installation directory of this node
   */
  private String home = "/tmp";

  /**
   * Enable/disable test mode. In test mode, all DAQ connections are accepted
   * regardless of whether another DAQ with the same name is already connected
   */
  private boolean testMode = false;

  /**
   * JMS properties
   */
  private Jms jms = new Jms();

  @Data
  public static class Jms {

    /**
     * URL of a JMS broker to connect to. If you need to use separate brokers
     * for clients and DAQs in a production setup, override
     * c2mon.server.daqcommunication.jms.url and c2mon.server.client.jms.url
     */
    private String url = "tcp://localhost:61616";

    /**
     * Enable/disable the embedded broker
     */
    private boolean embedded = true;
  }
}
