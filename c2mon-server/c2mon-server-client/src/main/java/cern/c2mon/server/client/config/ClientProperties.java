package cern.c2mon.server.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
  public class Jms {

    private String url = "${c2mon.server.jms.url}";

    private int initialConsumers = 5;

    private int maxConsumers = 10;

    /**
     * The topic prefix used to publish data tags and rules to the client. The
     * process ID will be appended.
     */
    private String tagTopicPrefix = "${c2mon.domain}.client.tag";

    /**
     * Topic on which active alarms are published
     */
    private String alarmTopic = "${c2mon.domain}.client.alarm";

    /**
     * Topic on which all control tags are published
     */
    private String controlTagTopic = "${c2mon.domain}.client.controltag";

    /**
     * Topic on which supervision events are published
     */
    private String supervisionTopic = "${c2mon.domain}.client.supervision";

    /**
     * Requests from client to server
     */
    private String requestQueue = "${c2mon.domain}.client.request";

    /**
     * Configuration requests from client to server
     */
    private String configRequestQueue = "${c2mon.domain}.client.config";

    /**
     * Admin requests from client to server
     */
    private String adminRequestQueue = "${c2mon.domain}.client.admin";
  }
}
