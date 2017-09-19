package cern.c2mon.server.cache.loader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.cacheloading")
public class CacheLoadingProperties {

  /**
   * Max number of threads used to load caches from the database. For best
   * performance, ensure there are as many (or more) database connections
   * available
   */
  private int maxThreads = 20;

  /**
   * Number of cache objects to be loaded in a single task. This results in
   * one DB query in single thread.
   */
  private int batchSize = 1000;

  /**
   * Size of the loader task queue. This should be large enough to contain all
   * the batches, i.e. queueSize and batchSize must be set so that
   * queueSize > #(cache id range) / batchSize (otherwise exceptions will be
   * thrown at startup)
   */
  private int queueSize = 1000;
}
