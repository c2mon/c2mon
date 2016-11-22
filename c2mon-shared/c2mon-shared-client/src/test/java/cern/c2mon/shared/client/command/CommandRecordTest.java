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

import org.junit.Test;

import cern.c2mon.pmanager.fallback.exception.DataFallbackException;

/**
 * Unit test of Command object for logging to history.
 *
 * @author Mark Brightwell
 *
 */
public class CommandRecordTest {

  /**
   * @return a test object
   */
  public static CommandRecord createLogObject() {
    CommandRecord commandRecord = new CommandRecord();
    commandRecord.setDataType("String");
    commandRecord.setExecutionTime(new Timestamp(System.currentTimeMillis()));
    commandRecord.setHost("host");
    commandRecord.setUser("user");
    commandRecord.setId(10L);
    commandRecord.setMode(Short.valueOf("2"));
    commandRecord.setName("name");
    commandRecord.setValue("value");
    commandRecord.setReportDescription("report-desc");
    commandRecord.setReportStatus(CommandExecutionStatus.STATUS_AUTHORISATION_FAILED);
    commandRecord.setReportTime(new Timestamp(System.currentTimeMillis() + 1000));
    commandRecord.setTagId(20L);
    return commandRecord;
  }

  @Test
  public void testFallbackEncoding() throws DataFallbackException {
    CommandRecord commandLog = createLogObject();
    String encoded = commandLog.toString();
    CommandRecord decoded = (CommandRecord) commandLog.getObject(encoded);
    assertEquals(commandLog, decoded);
  }

}
