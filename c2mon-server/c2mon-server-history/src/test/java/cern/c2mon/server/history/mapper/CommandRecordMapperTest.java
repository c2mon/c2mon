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
package cern.c2mon.server.history.mapper;

import cern.c2mon.server.history.MapperTest;
import cern.c2mon.shared.client.command.CommandExecutionStatus;
import cern.c2mon.shared.client.command.CommandRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test of Mybatis Mapper implementation.
 *
 * @author Mark Brightwell
 *
 */
public class CommandRecordMapperTest extends MapperTest {

  private static final Long COMMAND_ID = 1111L;

  /**
   * To test.
   */
  @Autowired
  private CommandRecordMapper commandRecordMapper;

  /**
   * Removes test values from previous tests in case clean up failed.
   */
  @Before
  public void beforeTest() {
    removeTestData();
  }

  /**
   * Removes test values after test.
   */
  @After
  public void afterTest() {
    removeTestData();
  }

  private void removeTestData() {
    commandRecordMapper.deleteAllLogs(COMMAND_ID);
  }

  /**
   * Assumes no logs in test table for this command.
   */
  @Test
  public void testLogCommand() {
    CommandRecord commandLog = new CommandRecord();
    commandLog.setId(COMMAND_ID);
    commandLog.setName("command name");
    commandLog.setDataType("datatype");
    commandLog.setMode(Short.valueOf((short) 2));
    commandLog.setExecutionTime(new Timestamp(System.currentTimeMillis()));
    commandLog.setValue("value");
    commandLog.setHost("host");
    commandLog.setUser("user");
    commandLog.setReportStatus(CommandExecutionStatus.STATUS_CMD_UNKNOWN);
    commandLog.setReportTime(new Timestamp(System.currentTimeMillis() + 1000));
    commandLog.setReportDescription("report text");

    commandRecordMapper.insertLog(commandLog);

    List<CommandRecord> retrievedLogList = commandRecordMapper.getCommandTagLog(COMMAND_ID);

    assertNotNull(retrievedLogList);
    assertEquals(1, retrievedLogList.size());

    CommandRecord retrievedLog = retrievedLogList.get(0);

    assertEquals(commandLog.getId(), retrievedLog.getId());
    assertEquals(commandLog.getName(), retrievedLog.getName());
    assertEquals(commandLog.getDataType(), retrievedLog.getDataType());
    assertEquals(commandLog.getValue(), retrievedLog.getValue());
    assertEquals(commandLog.getHost(), retrievedLog.getHost());
    assertEquals(commandLog.getMode(), retrievedLog.getMode());
    assertEquals(commandLog.getUser(), retrievedLog.getUser());
    assertEquals(commandLog.getReportStatus(), retrievedLog.getReportStatus());
    assertEquals(commandLog.getReportDescription(), retrievedLog.getReportDescription());

    //check time is logged in UTC format to DB
    int offset = TimeZone.getDefault().getOffset(commandLog.getReportTime().getTime());
    assertEquals(commandLog.getReportTime().getTime(), retrievedLog.getReportTime().getTime() + offset);

  //check time is logged in UTC format to DB
    int offset2 = TimeZone.getDefault().getOffset(commandLog.getExecutionTime().getTime());
    assertEquals(commandLog.getExecutionTime().getTime(), retrievedLog.getExecutionTime().getTime() + offset2);

  }

}
