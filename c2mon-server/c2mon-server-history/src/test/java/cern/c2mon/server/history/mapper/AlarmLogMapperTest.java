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

import java.sql.Timestamp;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.history.structure.AlarmLog;

/**
 * Tests the iBatis mapper against the Oracle DB for Alarms.
 *
 * @author Felix Ehm
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
    "classpath:config/server-history-mapper-test.xml",
})
@TestPropertySource("classpath:c2mon-server-default.properties")
public class AlarmLogMapperTest {

  /**
   * Test data tag fields.
   */
  private static final long ALARM_ID = 1234;

  /**
   * To test.
   */
  @Autowired
  private AlarmLogMapper alarmLogMapper;

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

  /**
   * Removes test data.
   */
  private void removeTestData() {
    alarmLogMapper.deleteAlarmLog(ALARM_ID);
  }

  /**
   * Tests insertion completes successfully when fallback not
   * active (so no logtime set in object).
   */
  @Test
  public void testInsertAlarmLog() {
    AlarmLog alarmToLog = new AlarmLog();
    alarmToLog.setTagId(999L);
    alarmToLog.setActive(true);
    alarmToLog.setFaultFamily("FF");
    alarmToLog.setFaultMember("FM");
    alarmToLog.setFaultCode(2);
    alarmToLog.setPriority(1);
    alarmToLog.setAlarmId(ALARM_ID);
    alarmToLog.setServerTimestamp(new Timestamp(System.currentTimeMillis()));
    System.out.println(alarmToLog);
    alarmLogMapper.insertLog(alarmToLog);
  }

  /**
   * Tests insertion completes successfully when fallback is
   * active. In this case, the logtime needs including as does
   * a specification of the timezone of the DB.
   */
  @Test
  public void testInsertDataTagLogFromFallback() {
    AlarmLog alarmToLog = new AlarmLog();
    alarmToLog.setTagId(999L);
    alarmToLog.setActive(true);
    alarmToLog.setFaultFamily("FF");
    alarmToLog.setFaultMember("FM");
    alarmToLog.setFaultCode(2);
    alarmToLog.setPriority(1);
    alarmToLog.setAlarmId(ALARM_ID);
    alarmToLog.setServerTimestamp(new Timestamp(System.currentTimeMillis()));
    System.out.println(alarmToLog);

    // additional the log date to indicate that it came from the fallback file
    alarmToLog.setLogDate(new Timestamp(System.currentTimeMillis()));
    alarmToLog.setTimezone(TimeZone.getDefault().getID());

    alarmLogMapper.insertLog(alarmToLog);
  }
}
