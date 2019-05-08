package cern.c2mon.cache.api.exception;

public class MoreThanOneProviderFoundException extends RuntimeException {

  public MoreThanOneProviderFoundException() {
    super();
  }

  public MoreThanOneProviderFoundException(String message) {
    super(message);
  }
}
