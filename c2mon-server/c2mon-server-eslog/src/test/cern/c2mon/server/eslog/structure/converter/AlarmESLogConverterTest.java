/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.structure.converter;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.eslog.structure.types.AlarmES;
import cern.c2mon.server.test.CacheObjectCreation;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertEquals;

/**
 * @author Alban Marguet
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AlarmESLogConverterTest {
  @InjectMocks
  AlarmESLogConverter alarmESLogConverter;
  Alarm alarm;
  AlarmES alarmES;

  @Test
  public void testNullConvertsToNull() {
    alarmES = alarmESLogConverter.convertAlarmToAlarmES(alarm);
    assertNull(alarmES);
  }

  @Test
  public void testDataIsPreserved() {
    alarm = CacheObjectCreation.createTestAlarm1();
    alarmES = alarmESLogConverter.convertAlarmToAlarmES(alarm);

    log.debug(alarmES.toString());
    assertEquals(alarm.getTagId().longValue(), alarmES.getTagId());
    assertEquals(alarm.getId().longValue(), alarmES.getAlarmId());
    assertEquals(alarm.getFaultFamily(), alarmES.getFaultFamily());
    assertEquals(alarm.getFaultMember(), alarmES.getFaultMember());
    assertEquals(alarm.getFaultCode(), alarmES.getFaultCode());
    assertEquals(alarm.isActive(), alarmES.isActive());
    assertEquals(alarm.getInfo(), alarmES.getInfo());
    assertEquals(alarm.getTimestamp(), alarmES.getServerTimestamp());
  }
}
