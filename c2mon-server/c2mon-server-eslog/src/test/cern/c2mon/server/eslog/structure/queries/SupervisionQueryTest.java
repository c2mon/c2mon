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
package cern.c2mon.server.eslog.structure.queries;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Alban Marguet
 */
public class SupervisionQueryTest {
  private long id = 1L;
  private String entity = "PROCESS";
  private long timestamp = 123456L;
  private String message = "message";
  private String status = "RUNNING";
  private Map<String, Object> jsonSource = new HashMap<>();
  private SupervisionEvent event;
  SupervisionQuery query;
  Client client;

  @Before
  public void setup() {
    jsonSource.put("id", id);
    jsonSource.put("entity", entity);
    jsonSource.put("timestamp", timestamp);
    jsonSource.put("message", message);
    jsonSource.put("status", status);
    event = new SupervisionEventImpl(SupervisionConstants.SupervisionEntity.PROCESS, id, SupervisionConstants.SupervisionStatus.RUNNING, new Timestamp(timestamp), message);
    query = new SupervisionQuery(client, event);
  }

  @Test
  public void testCorrectOutput() {
    assertEquals(id, query.getId());
    assertEquals(entity, query.getEntity());
    assertEquals(timestamp, query.getTimestamp());
    assertEquals(message, query.getMessage());
    assertEquals(status, query.getStatus());
    assertEquals(jsonSource, query.getJsonSource());
  }
}