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
package cern.c2mon.server.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;
import java.util.List;
import java.util.TimeZone;

import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
import cern.c2mon.server.command.config.CommandModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.history.config.HistoryModule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.shared.client.command.CommandRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.history.listener.CommandRecordListener;
import cern.c2mon.server.history.mapper.CommandRecordMapper;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.client.command.CommandExecutionStatus;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandReportImpl;
import cern.c2mon.shared.common.command.CommandTag;

/**
 * @author Mark Brightwell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    SupervisionModule.class,
    CommandModule.class,
    DaqModule.class,
    HistoryModule.class
})
public class HistoryModuleTest {

  @Autowired
  private CommandRecordListener commandRecordListener;

  @Autowired
  private CommandRecordMapper commandTagMapper;

  @Before
  public void setUp() {
    commandTagMapper.deleteAllLogs(CacheObjectCreation.createTestCommandTag().getId());
  }

  @After
  public void cleanUp() {
    commandTagMapper.deleteAllLogs(CacheObjectCreation.createTestCommandTag().getId());
  }

  /**
   * Tests logging of commands works.
   */
  @Test
  public void testCommandLogging() {
    CommandTag commandTag = CacheObjectCreation.createTestCommandTag();
    CommandReport report = new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_AUTHORISATION_FAILED,
        "report text", new Timestamp(System.currentTimeMillis()), new Short((short) 2));
    commandRecordListener.log(commandTag, report);

    List<CommandRecord> retrievedLogList = commandTagMapper.getCommandTagLog(commandTag.getId());

    assertNotNull(retrievedLogList);
    assertEquals(1, retrievedLogList.size());

    CommandRecord retrievedLog = retrievedLogList.get(0);

    assertEquals(commandTag.getId(), retrievedLog.getTagId());
    assertEquals(commandTag.getId().toString(), retrievedLog.getId());
    assertEquals(commandTag.getName(), retrievedLog.getName());
    assertEquals(commandTag.getDataType(), retrievedLog.getDataType());
    assertEquals(commandTag.getCommandExecutionDetails().getValue().toString(), retrievedLog.getValue());
    //assertEquals(commandTag.getHost(), retrievedLog.getHost());
    assertEquals(Short.valueOf(commandTag.getMode()), retrievedLog.getMode());
    //assertEquals(commandTag.getUser(), retrievedLog.getUser());
    assertEquals(report.getStatus(), retrievedLog.getReportStatus());
    assertEquals(report.getReportText(), retrievedLog.getReportDescription());

    //check time is logged in UTC format to DB
    int offset = TimeZone.getDefault().getOffset(commandTag.getCommandExecutionDetails().getExecutionStartTime().getTime());
    assertEquals(commandTag.getCommandExecutionDetails().getExecutionStartTime().getTime(), retrievedLog.getExecutionTime().getTime() + offset);

    //check time is logged in UTC format to DB
    int offset2 = TimeZone.getDefault().getOffset(report.getTimestamp().getTime());
    assertEquals(report.getTimestamp().getTime(), retrievedLog.getReportTime().getTime() + offset2);
  }


}
