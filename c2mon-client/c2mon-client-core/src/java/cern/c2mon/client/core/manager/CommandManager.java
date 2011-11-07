package cern.c2mon.client.core.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.auth.AuthorizationManager;
import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.core.C2monCommandManager;
import cern.c2mon.client.core.C2monSessionManager;
import cern.c2mon.client.core.tag.ClientCommandTagImpl;
import cern.c2mon.client.jms.RequestHandler;
import cern.tim.shared.client.command.CommandExecuteRequest;
import cern.tim.shared.client.command.CommandExecuteRequestImpl;
import cern.tim.shared.client.command.CommandExecutionStatus;
import cern.tim.shared.client.command.CommandReport;
import cern.tim.shared.client.command.CommandReportImpl;
import cern.tim.shared.client.command.CommandTagHandle;
import cern.tim.shared.client.command.CommandTagValueException;
import cern.tim.shared.common.command.AuthorizationDetails;

@Service
public class CommandManager implements C2monCommandManager {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(CommandManager.class);
  
  /** Default for an uninitialized unknown tag id  */
  private static final Long UNKNOWN_TAG_ID = -1L;
  
  /**
   * The local cache for commands that have already been retrieved from the
   * server.
   */
  private final Map<Long, ClientCommandTagImpl<Object>> commandCache = 
    new ConcurrentHashMap<Long, ClientCommandTagImpl<Object>>();
  
  /**
   * The C2MON session manager
   */
  private final C2monSessionManager sessionManager;
  
  /** 
   * Provides methods for requesting commands information and sending
   * an execute request to the C2MON server.
   */
  private final RequestHandler clientRequestHandler;
  
  /**
   * The authorization manager is needed for checking 
   * whether the logged user is allowed to execute a given command.
   */
  private final AuthorizationManager authorizationManager;
  
  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   * 
   * @param pRequestHandler
   *          Provides methods for requesting tag information from the C2MON
   *          server
   *          
   * @param pAuthorizationManager The authorization manager which is needed to 
   *        check whether the logged user is allowed to execute a given command.
   */
  @Autowired
  protected CommandManager(final C2monSessionManager pSessionManager, final RequestHandler pRequestHandler, final AuthorizationManager pAuthorizationManager) {
    this.sessionManager = pSessionManager;
    this.clientRequestHandler = pRequestHandler;
    this.authorizationManager = pAuthorizationManager;
  }
  
  @Override
  public CommandReport executeCommand(Long commandId, Object value) throws CommandTagValueException {
    if (!sessionManager.isUserLogged()) {
      return new CommandReportImpl(commandId, 
          CommandExecutionStatus.STATUS_AUTHORISATION_FAILED, "No user is logged-in.");
    }
    else if (!isAuthorized(commandId)) {
      return new CommandReportImpl(commandId,
          CommandExecutionStatus.STATUS_AUTHORISATION_FAILED,
          "The logged user has not the priviledges to execute command " + commandId + ".");
    }
    else {
      if (!commandCache.containsKey(commandId)) {
        getCommandTag(commandId);
      }
        
      ClientCommandTag<Object> cct = commandCache.get(commandId);
      CommandExecuteRequest<Object> executeRequest = createCommandExecuteRequest(cct, value);
      try {
        return clientRequestHandler.executeCommand(executeRequest);
      }
      catch (JMSException e) {
        LOG.error("executeCommand() - Catched JMS execption while trying to execute command "
                  + commandId + ". ", e);
        return new CommandReportImpl(commandId,
            CommandExecutionStatus.STATUS_SERVER_ERROR, 
            "Could not execute the command due to a communication error with the server. Error: " + e.getMessage());
      }
    }
  }

  
  @SuppressWarnings("unchecked")
  @Override
  public <T> Set<ClientCommandTag<T>> getCommandTags(final Set<Long> pIds) {
    Set<ClientCommandTag<T>> resultSet = new HashSet<ClientCommandTag<T>>();
    Set<Long> newCommandTagIds = new HashSet<Long>();

    if (LOG.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("getCommandTags() - creating ");
      str.append(pIds.size());
      str.append(" command tags.");
      LOG.debug(str);
    }

    // Create ClientDataTags for all IDs and keep in hash table
    ClientCommandTagImpl commandTag;
    for (Long commandId : pIds) {
      // skip all fake tags
      if (!commandId.equals(UNKNOWN_TAG_ID)) {
        commandTag = this.commandCache.get(commandId);
        if (commandTag == null) {
          commandTag = new ClientCommandTagImpl(commandId);
          // Add the new tag to the global store
          this.commandCache.put(commandId, commandTag);
          
          // Add this id to the list to request
          newCommandTagIds.add(commandId);
        }
      }
    }

    if (newCommandTagIds.size() > 0) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(new StringBuffer("getCommandTags() - ").append(newCommandTagIds.size()).append(" commands to be requested."));
      }
      
      Collection<CommandTagHandle> commandTagHandles = clientRequestHandler.requestCommandTagHandles(newCommandTagIds);
      if (commandTagHandles != null) {
        ClientCommandTagImpl cct = null;
        for (CommandTagHandle<Object> tagHandle : commandTagHandles) {
          cct = this.commandCache.get(tagHandle.getId());
          // update ClientCommandTag object
          if (cct != null) { // found the ClientCommandTag object
            cct.update(tagHandle);
          }
          else {
            LOG.error("getCommandTags() - Received unknown command tag: " + tagHandle.getId());
          }
        }
      }
    }
    else {
      LOG.debug("getCommandTags() - No commands to be requested from the server.");
    }
    
    // Clone command tags for result set
    for (Long commandId : pIds) {
      try {
        resultSet.add((ClientCommandTag<T>) commandCache.get(commandId).clone());
      }
      catch (CloneNotSupportedException e) {
        LOG.error("getCommandTags() - Error while cloning command tag with id " + commandId);
        throw new RuntimeException("Cloning not supported by ClientCommandTagImpl with id " + commandId, e);
      }
    }
 
    return resultSet;
  }

  

  /**
   * Inner method for creating a command execution request.
   * @param <T> The value type of the command
   * @param commandTag The command tag for which the request shall be created
   * @param value The value that shall be used for the command execution
   * @return An instance of {@link CommandExecuteRequest}
   * @throws CommandTagValueException Thrown in case an incompatible value type.
   */
  private <T> CommandExecuteRequest<T> createCommandExecuteRequest(final ClientCommandTag<T> commandTag, T value) throws CommandTagValueException {
    // Check if value is NOT NULL
    if (value == null) {
      throw new CommandTagValueException("Null value : command values cannot be set to null");
    }

    if (!value.getClass().equals(commandTag.getType())) {
      throw new CommandTagValueException("Data type : " + commandTag.getType() + " expected. Cannot set value of type " + value.getClass().getName() + ".");
    }

    try {
      if ((commandTag.getMinValue() != null) && commandTag.getMinValue().compareTo(value) > 0) {
        throw new CommandTagValueException("Out of range : " + value + " is less than the authorized minimum value " + commandTag.getMinValue() + ".");
      }
    }
    catch (ClassCastException ce) {
      throw new CommandTagValueException("CONFIGURATION ERROR: The minValue for the command is of type " + commandTag.getType().getName()
          + ". It cannot be compared to a value of type " + value.getClass().getName() + ". Contact the configuration responsible for correcting this problem");
    }

    try {
      if ((commandTag.getMaxValue() != null) && commandTag.getMaxValue().compareTo(value) < 0) {
        throw new CommandTagValueException("Out of range : " + value + " is greater than the authorized maximum value " + commandTag.getMaxValue() + ".");
      }
    }
    catch (ClassCastException ce) {
      throw new CommandTagValueException("CONFIGURATION ERROR: The minValue for the command is of type " + commandTag.getType().getName()
          + ". It cannot be compared to a value of type " + value.getClass().getName() + ". Contact the configuration responsible for correcting this problem");
    }
    
    return new CommandExecuteRequestImpl<T>(commandTag.getId(), value);
  }
  
  /**
   * @param commandTag the command to be checked
   * @param <T> The value type of the command
   * @return <code>true</code>, if the user who requested the command
   *         tag from the server is authorised to execute the command. 
   */
  private <T> boolean isAuthorized(final ClientCommandTag<T> commandTag) {
    AuthorizationDetails authorizationDetails = commandTag.getAuthorizationDetails();
    if (authorizationDetails != null) {
      return authorizationManager.isAuthorized(commandTag.getAuthorizationDetails());
    }
    else {
      return false;
    }
  }

  @Override
  public <T> ClientCommandTag<T> getCommandTag(final Long commandId) {
    Set<Long> commandTagIds = new HashSet<Long>();
    commandTagIds.add(commandId);
    Set<ClientCommandTag<T>> commandTags = getCommandTags(commandTagIds);
    
    return commandTags.iterator().next();
  }

  @Override
  public boolean isAuthorized(final Long commandId) {
    if (!commandCache.containsKey(commandId)) {
      getCommandTag(commandId);
    }
    
    if (sessionManager.isUserLogged()) {
      ClientCommandTagImpl<Object> cct = commandCache.get(commandId);
      if (cct.isExistingCommand()) {
        if (cct.getAuthorizationDetails() != null) {
          return authorizationManager.isAuthorized(cct.getAuthorizationDetails());
        }
        else {
          LOG.warn("isAuthorized() - No authorization details received for command "
              + commandId + ". Please contact the support team to solve this problem!");
        }
      }
    }
    
    return false;
  }
}
