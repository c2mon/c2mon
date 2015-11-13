package cern.c2mon.server.configuration.parser.exception;

public class ConfigurationParseException extends RuntimeException {

  private static final long serialVersionUID = -6828942813238503050L;

  public ConfigurationParseException(String message) {
    super(message);
  }

  public ConfigurationParseException(Throwable cause) {
    super(cause);
  }

  public ConfigurationParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigurationParseException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
