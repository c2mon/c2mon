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

package cern.c2mon.server.history.structure;

import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.server.common.alarm.Alarm;
import org.easymock.EasyMock;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;

public class AlarmRecordConverterTest {

  AlarmRecord getAlarmLog() {
    AlarmRecord result = new AlarmRecord();
    return result;
  }

  @Test
  public void testAlarmLogConverter() {
    AlarmRecordConverter converter = new AlarmRecordConverter();

    Alarm alarm = EasyMock.createMock(Alarm.class);
    EasyMock.expect(alarm.getId()).andReturn(10L).times(2);
    EasyMock.expect(alarm.getFaultCode()).andReturn(2);
    EasyMock.expect(alarm.getFaultFamily()).andReturn("FF");
    EasyMock.expect(alarm.getFaultMember()).andReturn("FM");
    EasyMock.expect(alarm.getDataTagId()).andReturn(1L);
    EasyMock.expect(alarm.getInfo()).andReturn("");
    EasyMock.expect(alarm.getTriggerTimestamp()).andReturn(new Timestamp(System.currentTimeMillis()));
    EasyMock.expect(alarm.getSourceTimestamp()).andReturn(new Timestamp(System.currentTimeMillis()));
    EasyMock.expect(alarm.isActive()).andReturn(true);
    EasyMock.expect(alarm.isOscillating()).andReturn(false);
    EasyMock.replay(alarm);

    AlarmRecord l = (AlarmRecord) converter.convertToLogged(alarm);
    Long.valueOf(l.getId()).equals(alarm.getId());

    EasyMock.verify(alarm);
  }

  @Test
  public void testStringEncoding() throws DataFallbackException {

    AlarmRecord al = getAlarmLog();
    al.setActive(true);
    al.setAlarmId(1234L);
    al.setServerTimestamp(new Timestamp(System.currentTimeMillis()));
    al.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    al.setInfo("!@#$%^&*(}{[]\\  \"  |");
    al.setTagId(10L);
    al.setFaultFamily("FF");
    al.setFaultMember("FM");
    al.setFaultCode(1234);

    AlarmRecord decoded = (AlarmRecord) al.getObject(al.toString());

    assertEquals(al, decoded);
  }
}
