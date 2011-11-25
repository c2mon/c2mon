package cern.c2mon.server.shorttermlog.listener;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.tim.server.command.CommandExecutionManager;
import cern.tim.server.command.CommandPersistenceListener;
import cern.tim.shared.client.command.CommandReport;
import cern.tim.shared.client.command.CommandTagLog;
import cern.tim.shared.daq.command.CommandTag;

/**
 * Bean listening to the command module and logging command executions to
 * STL account.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class LogCommandListener implements CommandPersistenceListener {

  /**
   * The fallback persistence manager.
   */
  private IPersistenceManager persistenceManager;
  
  /**
   * For registering for command execution callbacks.
   */
  private CommandExecutionManager commandExecutionManager;
  
  /**
   * Registers as command persistence listener.
   */
  @PostConstruct
  public void init() {
    commandExecutionManager.registerAsPersistenceListener(this);
  }
  
  /**
   * Autowired constructor.
   * @param persistenceManager persistence manager instantiated in XML
   * @param commandExecutionManager from command module
   */
  @Autowired
  public LogCommandListener(@Qualifier("commandPersistenceManager") final IPersistenceManager persistenceManager,
                            final CommandExecutionManager commandExecutionManager) {
    super();
    this.persistenceManager = persistenceManager;
    this.commandExecutionManager = commandExecutionManager;
  }

  @Override
  public <T> void log(final CommandTag<T> commandTag, final CommandReport report) {
    CommandTagLog commandLog = new CommandTagLog();
    commandLog.setId(commandTag.getId());
    commandLog.setName(commandTag.getName());
    commandLog.setMode(commandTag.getMode());
    commandLog.setDataType(commandTag.getDataType());
    commandLog.setExecutionTime(commandTag.getCommandExecutionDetails().getExecutionStartTime());
    commandLog.setValue(commandTag.getCommandExecutionDetails().getValue().toString());
    //log.setHost(commandTag.getCommandExecutionDetails()) TODO host
    //TODO user
    commandLog.setReportStatus(report.getStatus());
    commandLog.setReportTime(report.getTimestamp());
    commandLog.setReportDescription(report.getReportText());
    persistenceManager.storeData(commandLog);
  }

}
