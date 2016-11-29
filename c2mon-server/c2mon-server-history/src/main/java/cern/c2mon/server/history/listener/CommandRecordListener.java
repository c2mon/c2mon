/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.history.listener;

import javax.annotation.PostConstruct;

import cern.c2mon.shared.client.command.CommandRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.command.CommandExecutionManager;
import cern.c2mon.server.command.CommandPersistenceListener;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.common.command.CommandTag;

/**
 * Bean listening to the command module and logging command executions to
 * the history database.
 *
 * @author Mark Brightwell
 *
 */
@Service
public class CommandRecordListener implements CommandPersistenceListener {

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
  public CommandRecordListener(@Qualifier("commandHistoryPersistenceManager") final IPersistenceManager persistenceManager,
                               final CommandExecutionManager commandExecutionManager) {
    super();
    this.persistenceManager = persistenceManager;
    this.commandExecutionManager = commandExecutionManager;
  }

  @Override
  public <T> void log(final CommandTag<T> commandTag, final CommandReport report) {
    CommandRecord commandLog = new CommandRecord();
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
