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
package cern.c2mon.server.eslog.indexer;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.eslog.connector.TransportConnector;
import cern.c2mon.server.eslog.structure.converter.EsAlarmLogConverter;
import cern.c2mon.server.eslog.structure.mappings.EsAlarmMapping;
import cern.c2mon.server.eslog.structure.mappings.EsMapping;
import cern.c2mon.server.eslog.structure.types.EsAlarm;
import cern.c2mon.server.test.CacheObjectCreation;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Timestamp;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the EsAlarmIndexer methods.
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class EsAlarmIndexerTest {
  private Alarm alarm;
  private EsAlarm esAlarm;
  private EsAlarmMapping esAlarmMapping;
  private Timestamp timestamp;
  @InjectMocks
  private EsAlarmIndexer indexer;
  @Mock
  private TransportConnector connector;
  private EsAlarmLogConverter esAlarmLogConverter = new EsAlarmLogConverter();

  @Before
  public void setup() throws IDBPersistenceException {
    alarm = CacheObjectCreation.createTestAlarm1();
    esAlarm = esAlarmLogConverter.convert(alarm);
    timestamp = alarm.getTimestamp();
    when(connector.handleAlarmQuery(anyString(), anyString(), eq(esAlarm))).thenReturn(true);
    indexer.setAlarmPrefix(indexer.alarmPrefix);
    indexer.setIndexFormat("M");
    esAlarmMapping = new EsAlarmMapping();
    esAlarmMapping.setProperties(EsMapping.ValueType.ALARM);
  }

  @After
  public void cleanUp() {
    indexer.getCacheIndices().clear();
  }

  @Test
  public void testInitWell() throws IDBPersistenceException {
    when(connector.isConnected()).thenReturn(true);
    indexer.init();
    assertTrue(indexer.isAvailable());
  }

  @Test
  public void testLogAlarm() throws IDBPersistenceException {
    String expectedMapping = esAlarmMapping.getMapping();

    indexer.logAlarm(esAlarm);
    verify(connector).handleAlarmQuery(eq(indexer.getAlarmPrefix() + indexer.millisecondsToYearMonth(timestamp.getTime())), eq(expectedMapping), eq(esAlarm));
  }

}