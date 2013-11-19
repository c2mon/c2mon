package cern.c2mon.shared.client.command;

import java.io.Serializable;

/**
 * Command execution request sent by the C2MON Client API.
 * @param <T> The Object type of the command value
 *
 * @author Matthias Braeger, Mark Brightwell
 */
public interface CommandExecuteRequest<T> extends Serializable {

  /**
   * @return The id of the command that shall be executed
   */
  Long getId();
  
  /**
   * @return The value that shall be used to execute the command.
   */
  T getValue();
  
  /**
   * @return the client timeout for this command (in milliseconds)
   */
  int getTimeout();

  /**
   * @return the name of the user executing the command
   */
  String getUsername();

  /**
   * @return the host the execute request is made from
   */
  String getHost();
}
