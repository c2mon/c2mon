package cern.c2mon.server.daqcommunication.in.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.daqcommunication")
public class DaqCommunicationInProperties {

  /**
   * JMS properties
   */
  private Jms jms = new Jms();



  /**
   c2mon.server.daqcommunication.jms.url=${c2mon.server.jms.url}
   c2mon.server.daqcommunication.jms.queue.trunk = ${c2mon.domain}.process
   c2mon.server.daqcommunication.jms.configurationTimeout = 60000

   # JMS properties for tag update messages
   c2mon.server.daqcommunication.jms.update.consumers.initial = 1
   c2mon.server.daqcommunication.jms.update.consumers.max = 50
   # time before all consumers become active (in seconds)
   c2mon.server.daqcommunication.jms.update.consumers.warmuptime = 120
   # should broker wait for server to finish message processing call (i.e. put in
   # cache and notify listeners)
   c2mon.server.daqcommunication.jms.update.transacted = true
   c2mon.server.daqcommunication.jms.update.idleTaskExecutionLimit = 5
   c2mon.server.daqcommunication.jms.update.maxMessagesPerTask = 1
   c2mon.server.daqcommunication.jms.update.receiveTimeout = 1000
   c2mon.server.daqcommunication.jms.update.numExecutorThreads = 250
   c2mon.server.daqcommunication.jms.update.keepAliveSeconds = 60

   # JMS properties for DAQ requests to the server
   c2mon.server.daqcommunication.jms.request.transacted = true
   c2mon.server.daqcommunication.jms.request.consumers.initial = 1
   c2mon.server.daqcommunication.jms.request.consumers.max = 5
   */

  @Data
  public class Jms {

    // TODO: somehow allow using ${c2mon.server.jms.url} here
    private String url = "tcp://localhost:61616";

    private String queuePrefix = "${c2mon.domain}.process";

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
