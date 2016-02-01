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
package cern.c2mon.server.eslog.structure.queries;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.eslog.structure.converter.AlarmESLogConverter;
import cern.c2mon.server.eslog.structure.types.AlarmES;
import cern.c2mon.server.test.CacheObjectCreation;
import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/**
 * Check that the AlarmQuery class will bring the data effectively to ElasticSearch.
 * @author Alban Marguet
 */
public class AlarmESQueryTest {
  private String jsonSource;
  private AlarmESQuery query;
  private Client client;
  private AlarmESLogConverter alarmESLogConverter;
  private Alarm alarm;
  private AlarmES alarmES;

  @Before
  public void setup() throws ClusterNotAvailableException {
    alarm = CacheObjectCreation.createTestAlarm1();
    alarmESLogConverter = new AlarmESLogConverter();
    alarmES = alarmESLogConverter.convertAlarmToAlarmES(alarm);
    jsonSource = alarmES.toString();

    query = new AlarmESQuery(client, alarmES);
  }

  @Test
  public void testCorrectOutput() {
    assertEquals(jsonSource, query.getJsonSource());
  }
}