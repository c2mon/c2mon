/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch.supervision;

import cern.c2mon.server.elasticsearch.util.EntityUtils;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.TestCase.assertEquals;

/**
 * Tests for {@link SupervisionEventDocument}, executed by {@link cern.c2mon.server.elasticsearch.ElasticsearchSuiteTest}.
 *
 * NOTE: The naming convention (&lt;class name&gt;TestSuite) is used specifically to prevent test execution plugins
 * (like Surefire) to execute the tests individually.
 *
 * @author Alban Marguet
 * @author Serhiy Boychenko
 */
@RunWith(JUnit4.class)
public class SupervisionEventDocumentTestSuite {

  private SupervisionEventDocumentConverter converter = new SupervisionEventDocumentConverter();

  @Test
  public void toAndFromJson() {
    SupervisionEvent event = EntityUtils.createSupervisionEvent();
    SupervisionEventDocument document = converter.convert(event);

    // Serialize
    String json = document.toString();

    // Deserialize
    document = (SupervisionEventDocument) document.getObject(json);

    assertEquals(event.getEntity().name(), document.get("entity"));
    assertEquals(event.getEntityId(), document.get("id"));
    assertEquals(event.getEventTime().getTime(), document.get("timestamp"));
    assertEquals(event.getName(), document.get("name"));
    assertEquals(event.getStatus().name(), document.get("status"));
    assertEquals(event.getMessage(), document.get("message"));
  }
}
