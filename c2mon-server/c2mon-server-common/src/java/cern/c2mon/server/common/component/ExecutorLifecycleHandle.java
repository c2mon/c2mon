package cern.c2mon.server.common.component;

import java.util.concurrent.ExecutorService;

/**
 * Allows executor shutdown via the C2MON {@link Lifecycle} interface.
 * Used in particular for listeners to be able to shutdown notification
 * threads running as ExecutorService's.
 * 
 * <p>Only attempts smooth shutdown. Stop() will not return if unsuccessful.
 * 
 * @author Mark Brightwell
 *
 */
public class ExecutorLifecycleHandle implements Lifecycle {

  /**
   * Service to manage.
   */
  private ExecutorService executor;
  
  /**
   * Constructor
   * @param executor service whose lifecycle is to be managed
   */
  public ExecutorLifecycleHandle(final ExecutorService executor) {
    super();
    this.executor = executor;
  }

  @Override
  public boolean isRunning() {
    return !executor.isShutdown();
  }

  @Override
  public void start() {
    //do nothing
  }

  @Override
  public void stop() {
    executor.shutdown();
  }

}
