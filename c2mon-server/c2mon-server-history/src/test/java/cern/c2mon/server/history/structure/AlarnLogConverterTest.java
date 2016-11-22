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

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.server.common.alarm.Alarm;

public class AlarnLogConverterTest {

    AlarmLog getAlarmLog() {
        AlarmLog result = new AlarmLog();
        return result;
    }

    @Test
    public void testAlarmLogConverter() {
        AlarmLogConverter converter = new AlarmLogConverter();

        Alarm alarm = EasyMock.createMock(Alarm.class);
        EasyMock.expect(alarm.getId()).andReturn(10L).times(2);
        EasyMock.expect(alarm.getFaultCode()).andReturn(2);
        EasyMock.expect(alarm.getFaultFamily()).andReturn("FF");
        EasyMock.expect(alarm.getFaultMember()).andReturn("FM");
        EasyMock.expect(alarm.getTagId()).andReturn(1L);
        EasyMock.expect(alarm.getInfo()).andReturn("");
        EasyMock.expect(alarm.getTimestamp()).andReturn(new Timestamp(System.currentTimeMillis()));
        EasyMock.expect(alarm.isActive()).andReturn(true);
        EasyMock.replay(alarm);

        AlarmLog l = (AlarmLog) converter.convertToLogged(alarm);
        Long.valueOf(l.getId()).equals(alarm.getId());

        EasyMock.verify(alarm);

    }

    @Test
    public void testStringEncoding() throws DataFallbackException {

        AlarmLog al = getAlarmLog();
        al.setActive(true);
        al.setAlarmId(1234L);
        al.setServerTimestamp(new Timestamp(System.currentTimeMillis()));
        al.setInfo("!@#$%^&*(}{[]\\  \"  |");
        al.setTagId(10L);
        al.setFaultFamily("FF");
        al.setFaultMember("FM");
        al.setFaultCode(1234);

        String encoded = al.toString();
        System.out.println(encoded);
        AlarmLog decoded = (AlarmLog) al.getObject(encoded);

        assertEquals(al, decoded);
    }

}
