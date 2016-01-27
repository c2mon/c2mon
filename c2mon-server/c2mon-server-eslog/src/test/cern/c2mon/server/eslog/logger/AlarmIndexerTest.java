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
package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.eslog.structure.converter.AlarmESLogConverter;
import cern.c2mon.server.eslog.structure.mappings.AlarmMapping;
import cern.c2mon.server.eslog.structure.mappings.Mapping;
import cern.c2mon.server.eslog.structure.types.AlarmES;
import cern.c2mon.server.test.CacheObjectCreation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Timestamp;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the AlarmIndexer methods.
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
public class AlarmIndexerTest {
  private Alarm alarm;
  private AlarmES alarmES;
  private AlarmMapping mapping;
  private Timestamp timestamp;
  @InjectMocks
  private AlarmIndexer indexer;
  @Mock
  private TransportConnector connector;
  private AlarmESLogConverter alarmESLogConverter = new AlarmESLogConverter();

  @Before
  public void setup() {
    alarm = CacheObjectCreation.createTestAlarm1();
    alarmES = alarmESLogConverter.convertAlarmToAlarmES(alarm);
    timestamp = alarm.getTimestamp();
    when(connector.handleAlarmQuery(anyString(), anyString(), eq(alarmES))).thenReturn(true);
    when(connector.getEsPersistenceManager()).thenReturn(new ESPersistenceManager());
    indexer.setAlarmPrefix(indexer.alarmPrefix);
    indexer.setIndexFormat("M");
    mapping = new AlarmMapping();
    mapping.setProperties(Mapping.ValueType.alarmType);
  }

  @After
  public void cleanUp() {
    indexer.getIndices().clear();
  }

  @Test
  public void testWaitForConnection() {
    when(connector.isConnected()).thenReturn(true);
    indexer.waitForConnection();
    assertTrue(indexer.isAvailable());
  }

  @Test
  public void testGenerateSupervisionIndex() {
    String expected = indexer.alarmPrefix + indexer.millisecondsToYearMonth(timestamp.getTime());
    assertEquals(expected, indexer.generateAlarmIndex(timestamp.getTime()));
  }

  @Test
  public void testLogSupervisionEvent() {
    String expectedMapping = mapping.getMapping();

    indexer.logAlarm(alarmES);
    verify(connector).handleAlarmQuery(eq(indexer.generateAlarmIndex(timestamp.getTime())), eq(expectedMapping), eq(alarmES));
  }
}