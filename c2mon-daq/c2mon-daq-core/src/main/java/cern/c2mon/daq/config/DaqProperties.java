package cern.c2mon.daq.config;

import cern.c2mon.shared.daq.config.DaqJmsProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties("c2mon.daq")
public class DaqProperties {

  /**
   * Unique name of this DAQ process
   */
  private String name;

  /**
   * Time (in ms) which the DAQ waits for a server response
   */
  private long serverRequestTimeout = 120000;

  /**
   * Tolerance of the freshness monitor. A tag will be considered stale if it
   * is not updated within freshnessInterval * freshnessTolerance seconds. The
   * freshness interval is configured on the tag itself
   */
  private double freshnessTolerance = 1.5;

  /**
   * Path to a local configuration file. If set, the DAQ will load its
   * configuration from this file rather than retrieving it from the server.
   */
  private String localConfigFile = null;

  /**
   * Path on the local machine to which to save the remote configuration. This
   * can then subsequently be modified and used as a local configuration.
   */
  private String saveRemoteConfig = null;

  /**
   * JMS properties
   */
  private final Jms jms = new Jms();

  @Data
  public static class Jms extends DaqJmsProperties {

    /**
     * Tag publication mode. Possible values are:
     *
     * single: publish to a single broker (default)
     * double: publish to two brokers (e.g for feeding a test server with
     *         operational data)
     * test:   do not publish at all
     */
    private String mode = "single";

    /**
     * URL of the secondary JMS broker to which to publish (only relevant when
     * running in double publication mode)
     */
    private String secondaryUrl = "tcp://0.0.0.0:61617";
    
    /**
     * Set the time-to-live in seconds for all requests that are sent via JMS to the C2MON server.
     * Default is 60 seconds
     */
    private int requestMsgtimeToLive = 60;
  }

  /**
   * Filtering properties
   */
  private final Filter filter = new Filter();

  @Data
  public static class Filter {

    /**
     * Maximum capacity of the filter buffer. If this capacity is exceeded, a
     * FIFO strategy will be applied to the buffer
     */
    private int bufferCapacity = 10000;

    /**
     * Dynamic deadband properties
     */
    private final DynamicDeadband dynamicDeadband = new DynamicDeadband();

    @Data
    public static class DynamicDeadband {

      /**
       * Enable/disable the dynamic deadband support
       */
      private boolean enabled = false;

      /**
       * Size of the moving average counter window
       */
      private int windowSize = 6;

      /**
       * Interval (in ms) at which the dynamic deadband will be checked
       */
      private int checkInterval = 600000;

      /**
       * Threshold at which the dynamic deadband will be activated. If there
       * are more than this number of updates within the window, the deadband
       * will activate
       */
      private int activationThreshold = 20;

      /**
       * Threshold at which the dynamic deadband will be deactivated. If there
       * are fewer than this number of updates within the window, the deadband
       * will deactivate
       */
      private int deactivationThreshold = 15;

      /**
       * The deadband interval that will be forced if the activation threshold
       * is exceeded
       */
      private int forcedDeadbandInterval = 30000;
    }

    /**
     * Enable/disable publication of filtered values to a broker. This is often
     * useful for gathering statistics about filtered data
     */
    private boolean publishFilteredValues = false;

    /**
     * Filtered data JMS settings
     */
    private final Jms jms = new Jms();

    @Data
    public static class Jms {

      /**
       * URL of the broker to which to publish filtered values. Only relevant
       * if c2mon.daq.filter.publishFilteredValues=true
       */
      private String url = "tcp://0.0.0.0:61616";
    }
  }
}
