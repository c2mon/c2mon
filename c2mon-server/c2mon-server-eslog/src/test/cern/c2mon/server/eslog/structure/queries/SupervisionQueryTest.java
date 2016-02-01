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

import cern.c2mon.server.eslog.structure.converter.SupervisionESConverter;
import cern.c2mon.server.eslog.structure.types.SupervisionES;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.when;

/**
 * Check that the SupervisionQuery class will bring the data effectively to ElasticSearch.
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
public class SupervisionQueryTest {
  private long id = 1L;
  private String entity = "PROCESS";
  private long timestamp = 123456L;
  private String message = "message";
  private String status = "RUNNING";
  private String jsonSource;
  private SupervisionESConverter supervisionESConverter = new SupervisionESConverter();
  private SupervisionEvent event;
  private SupervisionQuery query;
  private SupervisionES supervisionES;
  @Mock
  private Client client;

  @Before
  public void setup() throws ClusterNotAvailableException {
    event = new SupervisionEventImpl(SupervisionConstants.SupervisionEntity.PROCESS, id, SupervisionConstants.SupervisionStatus.RUNNING, new Timestamp(timestamp), message);
    supervisionES = supervisionESConverter.convertSupervisionEventToSupervisionES(event);
    jsonSource = supervisionES.toString();
    query = new SupervisionQuery(client, supervisionES);
  }

  @Test
  public void testCorrectOutput() {
    assertEquals(jsonSource, query.getJsonSource());
  }
}