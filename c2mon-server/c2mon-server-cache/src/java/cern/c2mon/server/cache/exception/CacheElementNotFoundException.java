package cern.c2mon.server.cache.exception;

/**
 * Unchecked exception thrown when a query to a cache is unsuccessful.
 * If it is not certain that a cache contains the sought-after element,
 * preferably use the hasKey() cache method before attempting to retrieve
 * the cache element.
 * 
 * @author mbrightw
 *
 */
public class CacheElementNotFoundException extends RuntimeException {

  /**
   * Calls corresponding RuntimeException constructor.
   */
  public CacheElementNotFoundException() {
    super();    
  }

  /**
   * Calls corresponding RuntimeException constructor.
   */
  public CacheElementNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Calls corresponding RuntimeException constructor.
   */
  public CacheElementNotFoundException(String message) {
    super(message);
  }

  /**
   * Calls corresponding RuntimeException constructor.
   */
  public CacheElementNotFoundException(Throwable cause) {
    super(cause);
  }

}
