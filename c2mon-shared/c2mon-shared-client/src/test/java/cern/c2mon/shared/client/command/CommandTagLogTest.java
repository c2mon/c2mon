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
package cern.c2mon.shared.client.command;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import cern.c2mon.shared.client.command.CommandExecutionStatus;
import cern.c2mon.shared.client.command.CommandTagLog;
import org.junit.Test;

import cern.c2mon.pmanager.fallback.exception.DataFallbackException;

/**
 * Unit test of Command object for logging to history.
 *
 * @author Mark Brightwell
 *
 */
public class CommandTagLogTest {

  /**
   * @return a test object
   */
  public static CommandTagLog createLogObject() {
    CommandTagLog commandTagLog = new CommandTagLog();
    commandTagLog.setDataType("String");
    commandTagLog.setExecutionTime(new Timestamp(System.currentTimeMillis()));
    commandTagLog.setHost("host");
    commandTagLog.setUser("user");
    commandTagLog.setId(10L);
    commandTagLog.setMode(Short.valueOf("2"));
    commandTagLog.setName("name");
    commandTagLog.setValue("value");
    commandTagLog.setReportDescription("report-desc");
    commandTagLog.setReportStatus(CommandExecutionStatus.STATUS_AUTHORISATION_FAILED);
    commandTagLog.setReportTime(new Timestamp(System.currentTimeMillis() + 1000));
    commandTagLog.setTagId(20L);
    return commandTagLog;
  }

  @Test
  public void testFallbackEncoding() throws DataFallbackException {
    CommandTagLog commandLog = createLogObject();
    String encoded = commandLog.toString();
    CommandTagLog decoded = (CommandTagLog) commandLog.getObject(encoded);
    assertEquals(commandLog, decoded);
  }

}
