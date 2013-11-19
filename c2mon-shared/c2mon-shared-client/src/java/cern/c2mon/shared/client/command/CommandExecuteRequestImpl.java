package cern.c2mon.shared.client.command;

import javax.validation.constraints.NotNull;

/**
 * This class implements the {@link CommandExecuteRequest} interface and is used
 * on the client side for sending an EXECUTE_COMMAND request to the C2MON server.
 * @param <T> The Object type of the command value
 *
 * @author Matthias Braeger, Mark Brightwell
 */
public class CommandExecuteRequestImpl<T> implements CommandExecuteRequest<T> {

  /** This object is serialized when sent to the server */
  private static final long serialVersionUID = 717165763310657558L;

  /** The id of the command */
  @NotNull
  private final Long commandId;
  
  /** The value that shall be used for the execution of the command */
  private final T commandValue;
  
  /** Timeout in milliseconds the client waits for a response from the server */
  private final int clientTimeout;
  
  /** Name of the user wishing to execute the command */
  private final String username;
  
  /** The host the execute request is made from */
  private final String host;
  
  /**
   * Default Constructor
   * @param commandId The id of the command
   * @param commandValue The value that shall be used for the execution of the command
   * @param clientTimeout timeout the client waits for a response (in milliseconds)
   * @param username the user who is trying to execute the command
   * @param host the host the execute request is made from
   * @throws IllegalArgumentException In case the <code>commandId</code> parameter is
   *         <code>null</code>
   */
  public CommandExecuteRequestImpl(final Long commandId, final T commandValue, final int clientTimeout, final String username, final String host) {
    if (commandId == null) {
      throw new IllegalArgumentException("Command id cannot be left null");
    }
    this.commandId = commandId;
    this.commandValue = commandValue;
    this.clientTimeout = clientTimeout;
    this.username = username;
    this.host = host;
  }
  
  @Override
  public Long getId() {
    return commandId;
  }

  @Override
  public T getValue() {
    return commandValue;
  }

  @Override
  public int getTimeout() {
    return clientTimeout;
  }

  @Override
  public String getUsername() {
    return username;
  }
  
  @Override
  public String getHost() {
    return host;
  }
}
