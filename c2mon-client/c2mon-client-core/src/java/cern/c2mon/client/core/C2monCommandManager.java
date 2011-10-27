package cern.c2mon.client.core;

import java.util.Collection;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.tim.shared.client.command.CommandReport;
import cern.tim.shared.client.command.CommandTagHandle;
import cern.tim.shared.client.command.CommandTagHandleImpl;

public interface C2monCommandManager {
  /**
   * Creates a Collection of ClientCommandTags from a Collection of identifiers
   * 
   * @param pIds Collection of unique tag identifiers to create
   *        ClientCommandTags from
   * @return Collection of clientCommandTag instances
   **/
  public Collection<ClientCommandTag> getCommandTags(final Collection<Long> pIds);
  
  /**
   * Returns an {@link CommandTagHandle} object for every valid id on the list.
   * However, in case of a connection error or an unknown command id the corresponding
   * command handle might be missing.
   * 
   * @param commandIds Collection of unique tag identifiers 
   * @return A collection of all <code>CommandTagHandle</code> objects
   */  
  Collection<CommandTagHandle> getCommandTagHandles(final Collection<Long> commandIds);
  
  /**
   * Executes the command and returns a {@link CommandReport} object.
   * However, in case of a connection error or an unknown command id the corresponding
   * command handle might be missing.
   * 
   * @param handle the handle to execute the command
   * @return the report on the success/failure of the execution
   */  
  public CommandReport executeCommand(final CommandTagHandleImpl handle);
}
