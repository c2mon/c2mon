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

import cern.c2mon.server.eslog.structure.converter.SupervisionESConverter;
import cern.c2mon.server.eslog.structure.mappings.Mapping;
import cern.c2mon.server.eslog.structure.mappings.SupervisionMapping;
import cern.c2mon.server.eslog.structure.types.SupervisionES;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
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
 * Test the SupervisionIndexer class.
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
public class SupervisionIndexerTest {
  private SupervisionConstants.SupervisionEntity entity = SupervisionConstants.SupervisionEntity.PROCESS;
  private SupervisionConstants.SupervisionStatus status = SupervisionConstants.SupervisionStatus.RUNNING;
  private Timestamp timestamp = new Timestamp(123456789);
  private long id = 1L;
  private String message = "message";
  private SupervisionMapping mapping;
  private SupervisionEvent event;
  private SupervisionES supervisionES;

  @InjectMocks
  private SupervisionIndexer indexer;

  @Mock
  private TransportConnector connector;

  @Mock
  private SupervisionESConverter supervisionESConverter;

  @Before
  public void setup() {
    event = new SupervisionEventImpl(entity, id, status, timestamp, message);
    supervisionES = new SupervisionES();
    supervisionES.setEntityId(id);
    supervisionES.setEntityName(entity.name());
    supervisionES.setEventTime(timestamp.getTime());
    supervisionES.setMessage(message);
    supervisionES.setStatusName(status.name());
        when(supervisionESConverter.convertSupervisionEventToSupervisionES(eq(event))).thenReturn(supervisionES);
    when(connector.handleSupervisionQuery(anyString(), anyString(), eq(supervisionES))).thenReturn(true);
    indexer.setSupervisionPrefix("prevision_");
    indexer.setIndexFormat("M");
    mapping = new SupervisionMapping();
    mapping.setProperties(Mapping.ValueType.supervisionType);
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
    String expected = indexer.supervisionPrefix + indexer.millisecondsToYearMonth(timestamp.getTime());
    assertEquals(expected, indexer.generateSupervisionIndex(timestamp.getTime()));
  }

  @Test
  public void testLogSupervisionEvent() {
    String expectedMapping = mapping.getMapping();

    indexer.logSupervisionEvent(supervisionES);
    verify(connector).handleSupervisionQuery(eq(indexer.getSupervisionPrefix() + indexer.millisecondsToYearMonth(timestamp.getTime())), eq(expectedMapping), eq(supervisionES));
  }
}