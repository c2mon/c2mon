package cern.c2mon.shared.client.config;

import lombok.Data;

@Data
public class ClientAmqpProperties extends ClientProperties {

  /**
   * URL of the AMQP broker
   */
  private String url = "amqp://0.0.0.1:5672";

}
