package cern.c2mon.client.core.manager;

import java.util.Collection;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.core.C2monCommandManager;
import cern.c2mon.client.jms.RequestHandler;
import cern.tim.shared.client.command.CommandReport;
import cern.tim.shared.client.command.CommandTagHandle;
import cern.tim.shared.client.command.CommandTagHandleImpl;

public class CommandManager implements C2monCommandManager {
  
  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(TagManager.class);
  
  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;
  
  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   * 
   * @param pRequestHandler
   *          Provides methods for requesting tag information from the C2MON
   *          server
   */
  @Autowired
  protected CommandManager(final RequestHandler pRequestHandler) {
    this.clientRequestHandler = pRequestHandler;
  }
  
  @Override
  public Collection<CommandTagHandle> getCommandTagHandles(final Collection<Long> commandIds) {

    return clientRequestHandler.requestCommandTagHandles(commandIds);
  }  
  
  @Override
  public CommandReport executeCommand(final CommandTagHandleImpl handle) {

    try {
      return clientRequestHandler.executeCommand(handle);
    } catch (JMSException e) {
      LOG.error("getAlarms() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return null;
  }

  @Override
  public Collection<ClientCommandTag> getCommandTags(Collection<Long> pIds) {
    // TODO Auto-generated method stub
    return null;
  }
}
