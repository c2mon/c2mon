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
package cern.c2mon.server.elasticsearch.indexer;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.elasticsearch.connector.Connector;
import cern.c2mon.server.elasticsearch.connector.TransportConnector;
import cern.c2mon.server.elasticsearch.structure.types.EsAlarm;
import org.elasticsearch.ElasticsearchException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
public class FallBackTest {

  @InjectMocks
  private EsAlarmIndexer esAlarmIndexer;

  @Mock
  private TransportConnector connector;

  @Mock
  private Environment environment;

  @Before
  public void setup() {
//    esAlarmIndexer.setIndexFormat("M");
    when(connector.logAlarmEvent(anyString(), anyString(), any(EsAlarm.class))).thenThrow(new ElasticsearchException("testException"));
  }

  @Test(expected = IDBPersistenceException.class)
  public void ElasticsearchExceptionTriggersIDBPersistenceException() throws IDBPersistenceException {
    esAlarmIndexer.storeData(new EsAlarm());
  }
}
