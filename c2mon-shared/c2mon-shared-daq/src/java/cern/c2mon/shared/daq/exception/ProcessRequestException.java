package cern.c2mon.shared.daq.exception;

/**
 * Exception thrown within the server if a request made to the DAQ layer is
 * unsuccessful (e.g. data tag request by the server or command).
 * 
 * @author Mark Brightwell
 *
 */
public class ProcessRequestException extends RuntimeException {


  /**
   * Serial key.
   */
  private static final long serialVersionUID = -1457578848865365203L;

  public ProcessRequestException() {
    // TODO Auto-generated constructor stub
  }

  public ProcessRequestException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  public ProcessRequestException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

  public ProcessRequestException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

}
