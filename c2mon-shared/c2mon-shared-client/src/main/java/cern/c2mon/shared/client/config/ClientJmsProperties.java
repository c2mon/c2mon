package cern.c2mon.shared.client.config;

import lombok.Data;

/**
 * @author Justin Lewis Salmon
 */
@Data
public class ClientJmsProperties {

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
   * Topic on which all control tags are published
   */
  private String controlTagTopic = "c2mon.client.controltag";

  /**
   * Name of the queue on which to make normal requests to the server
   */
  private String requestQueue = "c2mon.client.request";

  /**
   * Name of the queue on which to make admin requests to the server
   */
  private String adminRequestQueue = "c2mon.client.admin";

  /**
   * Name of the queue on which to make configuration requests to the server
   */
  private String configRequestQueue = "c2mon.client.config";
}
