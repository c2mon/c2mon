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
package cern.c2mon.server.elasticsearch.listener;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import cern.c2mon.server.elasticsearch.alarm.AlarmListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.elasticsearch.alarm.AlarmDocumentConverter;
import cern.c2mon.server.test.CacheObjectCreation;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test if the Alarms are sent to Elasticsearch when received.
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
public class AlarmListenerTest {
  @InjectMocks
  private AlarmListener listener;
  @Mock
  private IPersistenceManager persistenceManager;
  @Mock
  private AlarmDocumentConverter alarmDocumentConverter;
  private Alarm alarm = CacheObjectCreation.createTestAlarm1();
  private AlarmDocument AlarmDocument = new AlarmDocument();

  @Before
  public void setup() {
    when(alarmDocumentConverter.convert(eq(alarm))).thenReturn(AlarmDocument);
  }

  @Test
  public void testAlarmIsSentToIndexer() throws IDBPersistenceException {
    listener.notifyElementUpdated(alarm);
    verify(alarmDocumentConverter).convert(eq(alarm));
  }
}
