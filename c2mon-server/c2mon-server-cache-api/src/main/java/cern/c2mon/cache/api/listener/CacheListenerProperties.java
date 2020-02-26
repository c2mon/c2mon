package cern.c2mon.cache.api.listener;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

/**
 * Controls the basic properties for cache listeners
 *
 * Should be moved to the spring context eventually
 */
@Getter
@Setter
public class CacheListenerProperties {

  private long shutdownWait = 1;

  private TimeUnit shutdownWaitUnits = TimeUnit.SECONDS;

  private int concurrency = 4;

  private boolean enabled = true;

  private int batchSize = 10_000;

  private long batchSchedulePeriodMillis = 1_000;
}
