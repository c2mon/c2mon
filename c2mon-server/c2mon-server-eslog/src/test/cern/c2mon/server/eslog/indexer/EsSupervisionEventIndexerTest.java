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

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.eslog.connector.TransportConnector;
import cern.c2mon.server.eslog.structure.converter.EsSupervisionEventConverter;
import cern.c2mon.server.eslog.structure.mappings.EsMapping;
import cern.c2mon.server.eslog.structure.mappings.EsSupervisionMapping;
import cern.c2mon.server.eslog.structure.types.EsSupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

/**
 * Test the EsSupervisionEventIndexer class.
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
public class EsSupervisionEventIndexerTest {
  private SupervisionConstants.SupervisionEntity entity = SupervisionConstants.SupervisionEntity.PROCESS;
  private SupervisionConstants.SupervisionStatus status = SupervisionConstants.SupervisionStatus.RUNNING;
  private Timestamp timestamp = new Timestamp(123456789);
  private long id = 1L;
  private String message = "message";
  private EsSupervisionMapping mapping;
  private SupervisionEvent event;
  private EsSupervisionEvent esSupervisionEvent;

  @InjectMocks
  private EsSupervisionEventIndexer indexer;

  @Mock
  private TransportConnector connector;

  @Mock
  private EsSupervisionEventConverter esSupervisionEventConverter;

  @Before
  public void setup() throws IDBPersistenceException {
    event = new SupervisionEventImpl(entity, id, status, timestamp, message);
    esSupervisionEvent = new EsSupervisionEvent();
    esSupervisionEvent.setEntityId(id);
    esSupervisionEvent.setEntityName(entity.name());
    esSupervisionEvent.setEventTime(timestamp.getTime());
    esSupervisionEvent.setMessage(message);
    esSupervisionEvent.setStatusName(status.name());
    when(esSupervisionEventConverter.convertSupervisionEventToSupervisionES(eq(event))).thenReturn(esSupervisionEvent);
    when(connector.handleSupervisionQuery(anyString(), anyString(), eq(esSupervisionEvent))).thenReturn(true);
    indexer.setSupervisionPrefix("prevision_");
    indexer.setIndexFormat("M");
    mapping = new EsSupervisionMapping();
    mapping.setProperties(EsMapping.ValueType.supervisionType);
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
  public void testLogSupervisionEvent() throws IDBPersistenceException {
    String expectedMapping = mapping.getMapping();

    indexer.logSupervisionEvent(esSupervisionEvent);
    verify(connector).handleSupervisionQuery(eq(indexer.getSupervisionPrefix() + indexer.millisecondsToYearMonth(timestamp.getTime())), eq(expectedMapping), eq(esSupervisionEvent));
  }
}