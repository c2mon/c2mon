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
package cern.c2mon.server.elasticsearch.listener;

import java.sql.Timestamp;

import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocumentConverter;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocument;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test if the the SupervisionEvent are well translated into SupervisionEventDocument for Elasticsearch.
 *
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
public class SupervisionEventDocumentLogListenerTest {
  private SupervisionConstants.SupervisionEntity entity = SupervisionConstants.SupervisionEntity.PROCESS;
  private SupervisionConstants.SupervisionStatus status = SupervisionConstants.SupervisionStatus.RUNNING;
  private Timestamp timestamp = new Timestamp(123456789);
  private long id = 1L;
  private String name = "P_TEST";
  private String message = "message";
  private SupervisionEvent event = new SupervisionEventImpl(entity, id, name, status, timestamp, message);
  private SupervisionEventDocument supervisionEventDocument;

  @Mock
  private SupervisionNotifier supervisionNotifier;

  @Mock
  private IPersistenceManager persistenceManager;

  @InjectMocks
  private SupervisionEventListener listener;

  @Mock
  private SupervisionEventDocumentConverter supervisionEventDocumentConverter;

  @Test
  public void testNotifySupervisionEvent() throws IDBPersistenceException {
    when(supervisionEventDocumentConverter.convert(eq(event))).thenReturn(supervisionEventDocument);
    listener.notifySupervisionEvent(event);
  }

}
