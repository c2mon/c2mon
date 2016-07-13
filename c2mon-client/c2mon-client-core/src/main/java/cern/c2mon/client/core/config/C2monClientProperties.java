package cern.c2mon.client.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Contains default values for all configurable properties.
 *
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.client")
public class C2monClientProperties {

  /**
   * Maximum number of tags per request to a server
   */
  private int maxTagsPerRequest = 500;

  /**
   * Maximum number of threads used to request tags from server
   */
  private int maxRequestThreads = 5;

  /**
   * JMS properties
   */
  private Jms jms = new Jms();

  @Data
  public static class Jms {

    /**
     * URL of the JMS broker
     */
    private String url = "tcp://localhost:61616";

    /**
     * Username to authenticate with the broker
     */
    private String username = "";

    /**
     * Password to authenticate with the broker
     */
    private String password = "";

    /**
     * Name of the topic on which the server is publishing supervision events
     */
    private String supervisionTopic = "c2mon.client.supervision";

    /**
     * Name of the topic on which the server is publishing its heartbeat
     */
    private String heartbeatTopic = "c2mon.client.heartbeat";

    /**
     * Name of the topic on which the server is publishing alarms
     */
    private String alarmTopic = "c2mon.client.alarm";

    /**
     * Name of the queue on which to make normal requests to the server
     */
    private String requestQueue = "c2mon.client.request";

    /**
     * Name of the queue on which to make admin requests to the server
     */
    private String adminQueue = "c2mon.client.admin";

    /**
     * Name of the queue on which to make configuration requests to the server
     */
    private String configQueue = "c2mon.config";
  }
}
