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
package cern.c2mon.server.client.request.util;

import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagUpdate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by fritter on 09/12/15.
 */
public abstract class CompareClientRequestResult {

  public static void compareTagUpdateWithDataTagCacheObject(TagUpdate valueClient, DataTagCacheObject valueServer) {
    assertEquals(valueClient.getId(), valueServer.getId());
    assertEquals(valueClient.getValue(), valueServer.getValue());
    assertEquals(valueClient.getValueDescription(), valueServer.getValueDescription());
    assertEquals(valueClient.getDataTagQuality(), valueServer.getDataTagQuality());
    assertEquals(valueClient.getSourceTimestamp(), valueServer.getTimestamp());
    assertEquals(valueClient.getDaqTimestamp(), valueServer.getDaqTimestamp());
    assertEquals(valueClient.getServerTimestamp(), valueServer.getCacheTimestamp());
    assertEquals(valueClient.getDescription(), valueServer.getDescription());
    assertEquals(valueClient.getName(), valueServer.getName());
    assertEquals(valueClient.getMetadata(), valueServer.getMetadata().getMetadata());
  }

  public static void compareAlarmValuesWithAlarCacheObject(AlarmValue valueClient, AlarmCacheObject valueServer) {
    assertEquals(valueClient.getId(), valueServer.getId());
    assertEquals(valueClient.getFaultCode(), valueServer.getFaultCode());
    assertEquals(valueClient.getFaultMember(), valueServer.getFaultMember());
    assertEquals(valueClient.getFaultFamily(), valueServer.getFaultFamily());
    assertEquals(valueClient.getInfo(), valueServer.getInfo());
    assertEquals(valueClient.getTagId(), valueServer.getDataTagId());
    assertEquals(valueClient.getTimestamp(), valueServer.getTriggerTimestamp());
    assertEquals(valueClient.isActive(), valueServer.isActive());
    if (valueServer.getMetadata() != null && valueClient.getMetadata().isEmpty()) {
      compareMetadata(valueClient.getMetadata(), valueServer.getMetadata().getMetadata());
    }
  }

  public static void compareTagUpdates(TagUpdate client, TagUpdate server) {
    assertEquals(client.getId(), server.getId());
    assertEquals(client.getValue(), server.getValue());
    assertEquals(client.getValueDescription(), server.getValueDescription());
    assertEquals(client.getDataTagQuality(), server.getDataTagQuality());
    assertEquals(client.getSourceTimestamp(), server.getSourceTimestamp());
    assertEquals(client.getDaqTimestamp(), server.getDaqTimestamp());
    assertEquals(client.getServerTimestamp(), server.getServerTimestamp());
    assertEquals(client.getDescription(), server.getDescription());
    assertEquals(client.getName(), server.getName());
    assertEquals(client.getTopicName(), server.getTopicName());
    assertEquals(client.getMetadata().size(), server.getMetadata().size());

    compareMetadata(client.getMetadata(), server.getMetadata());
  }

  public static void compareMetadata(Map<String, Object> metadataClient, Map<String, Object> metadataServer) {
    for (String key : metadataClient.keySet()) {
      Object valueSever = metadataServer.get(key);
      Object valueClient = metadataClient.get(key);
      if (valueSever instanceof Number && valueClient instanceof Number) {
        assertTrue(new BigDecimal(valueSever.toString()).compareTo(new BigDecimal(valueClient.toString())) == 0);
      } else {
        assertEquals(valueClient, valueSever);
      }
    }
  }
}
