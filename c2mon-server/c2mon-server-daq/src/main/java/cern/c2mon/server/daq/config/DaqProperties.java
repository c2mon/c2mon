package cern.c2mon.server.daq.config;

import cern.c2mon.shared.daq.config.DaqJmsProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.daq")
public class DaqProperties {

  /**
   * JMS properties
   */
  private final Jms jms = new Jms();

  @Data
  public class Jms extends DaqJmsProperties {

    private int configurationTimeout = 60000;

    private Update update = new Update();

    private Request request = new Request();

    /**
     * JMS properties for incoming tag updates
     */
    @Data
    public class Update {

      private int initialConsumers = 1;
      int maxConsumers = 50;

      /**
       * Time before all consumers become active (in seconds)
       */
      int consumerWarmupTime = 120;

      /**
       * Should the broker wait for the server to finish message processing
       * call (i.e. put in cache and notify listeners)
       */
      boolean transacted = true;
      int idleTaskExecutionLimit = 5;
      int maxMessagesPerTask = 1;
      int receiveTimeout = 1000;
      int numExecutorThreads = 250;
      int keepAliveSeconds = 60;
    }

    /**
     * JMS properties for incoming requests from DAQs
     */
    @Data
    public class Request {
      int initialConsumers = 1;
      int maxConsumers = 5;
      boolean transacted = true;
    }
  }
}
