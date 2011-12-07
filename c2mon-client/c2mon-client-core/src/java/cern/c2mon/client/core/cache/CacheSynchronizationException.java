package cern.c2mon.client.core.cache;

/**
 * This {@link RuntimeException} is thrown whenever a problem
 * occures while trying to synchronize the C2MON client cache
 * with the C2MON server.
 *
 * @author Matthias Braeger
 */
public class CacheSynchronizationException extends RuntimeException {

  /**
   * Generated serial version UID
   */
  private static final long serialVersionUID = 2101954776238429455L;

  protected CacheSynchronizationException(String message, Throwable cause) {
    super(message, cause);
  }

  protected CacheSynchronizationException(String message) {
    super(message);
  }

  protected CacheSynchronizationException(Throwable cause) {
    super(cause);
  }
}
