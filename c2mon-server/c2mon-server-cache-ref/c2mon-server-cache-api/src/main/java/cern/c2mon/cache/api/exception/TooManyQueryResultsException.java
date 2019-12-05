package cern.c2mon.cache.api.exception;

/**
 * Thrown when a {@link cern.c2mon.cache.api.spi.CacheQuery} produces more
 * matches than expected
 *
 * @author Alexandros Papageorgiou Koufidis
 */
public class TooManyQueryResultsException extends RuntimeException {

  public TooManyQueryResultsException() {
    super();
  }

  public TooManyQueryResultsException(String message) {
    super(message);
  }
}
