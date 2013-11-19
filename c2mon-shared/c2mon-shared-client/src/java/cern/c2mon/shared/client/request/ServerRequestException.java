package cern.c2mon.shared.client.request;

public class ServerRequestException extends RuntimeException {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Indicates an eror occurred while proccessing a request in the server.
   * @param pErrorMessage Error description.
   */
  public ServerRequestException(final String pErrorMessage) {
    
    super(pErrorMessage); 
  }
}
