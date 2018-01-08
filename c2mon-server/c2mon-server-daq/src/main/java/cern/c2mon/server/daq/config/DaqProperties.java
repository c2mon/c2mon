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

    /**
     * The timeout in milliseconds, which the server shall wait for a reply after
     * sending a DAQ (re-)configuration
     */
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
      
      /**
       * Specify the limit for idle executions of a consumer task, not having received
       * any message within its execution. If this limit is reached, the task will shut
       * down and leave receiving to other executing tasks.
       * Raise this limit if you encounter too frequent scaling up and down. With this
       * limit being higher, an idle consumer will be kept around longer, avoiding the
       * restart of a consumer once a new load of messages comes in.
       */
      int idleTaskExecutionLimit = 5;
      
      /**
       * Specify the maximum number of messages to process in one task. More
       * concretely, this limits the number of message reception attempts per task,
       * which includes receive iterations that did not actually pick up a message
       * until they hit their timeout (see the "receiveTimeout" property).
       */
      int maxMessagesPerTask = 1;
      
      /** Set the timeout to use for receive calls, in milliseconds */
      int receiveTimeout = 1000;
      
      /** Set the global TagUpdater's pool size shared by all DAQ update queues */
      int numExecutorThreads = 100;
      
      /** Set the TagUpdater's ThreadPoolTaskExecutor keep-alive seconds */
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
