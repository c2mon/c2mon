package cern.c2mon.client.core;

import java.util.Set;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.tim.shared.client.command.CommandReport;
import cern.tim.shared.client.command.CommandTagValueException;

/**
 * The <code>C2monCommandManager</code> allows to retrieve command
 * information from the server and to send an execute request. Please
 *
 * @author Matthias Braeger
 */
public interface C2monCommandManager {
  /**
   * Creates a Collection of ClientCommandTags from a Collection of identifiers
   * 
   * @param <T> The value type of the command
   * @param pCommandIds Collection of unique tag identifiers to create
   *        ClientCommandTags from
   * @return Collection of clientCommandTag instances
   **/
  <T> Set<ClientCommandTag<T>> getCommandTags(final Set<Long> pCommandIds);
  
  /**
   * Returns the {@link ClientCommandTag} object for the given
   * command id. If the command is unknown to the system it will
   * nevertheless return a {@link ClientCommandTag} instance but
   * with most of the fields left uninitialized. 
   * @param <T> The value type of the command
   * @param commandId The command tag id
   * @return A copy of the {@link ClientCommandTag} instance in the
   *         command cache.
   */
  <T> ClientCommandTag<T> getCommandTag(final Long commandId);
  
  /**
   * Executes the command and returns a {@link CommandReport} object.
   * 
   * @param commandId The id of the command that shall be executed
   * @param value The command value that shall be used for execution 
   * @return the report on the success/failure of the execution
   * @throws CommandTagValueException In case the method is called with a
   *         value object which is not of expected type of the specified
   *         {@link ClientCommandTag}.
   * @see ClientCommandTag#getType()
   */  
  CommandReport executeCommand(Long commandId, Object value) throws CommandTagValueException;
  
  /**
   * Checks whether the logged user is authorized to execute a given command.
   * @param commandId the command that shall be ckecked
   * @return <code>true</code>, if a user is logged in and has the priviledges
   *         to execute the command specified by the <code>commandId</code>
   *         parameter.
   */
  boolean isAuthorized(Long commandId);
}
