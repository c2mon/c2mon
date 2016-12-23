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
import cern.c2mon.server.elasticsearch.alarm.AlarmIndexer;
import cern.c2mon.server.elasticsearch.alarm.EsAlarm;
import org.elasticsearch.ElasticsearchException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
public class FallBackTest {

  @InjectMocks
  private AlarmIndexer alarmIndexer;

  @Test(expected = IDBPersistenceException.class)
  public void exceptionIsThrown() throws IDBPersistenceException {
    doThrow(new ElasticsearchException("testException")).when(alarmIndexer).storeData(any(EsAlarm.class));
    alarmIndexer.storeData(new EsAlarm());
  }
}
