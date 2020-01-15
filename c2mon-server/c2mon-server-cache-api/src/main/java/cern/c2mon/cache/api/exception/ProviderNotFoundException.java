package cern.c2mon.cache.api.exception;

public class ProviderNotFoundException extends RuntimeException {

  public ProviderNotFoundException() {
    super();
  }

  public ProviderNotFoundException(String message) {
    super(message);
  }
}
