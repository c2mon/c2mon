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
package cern.c2mon.server.elasticsearch.client;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
@Slf4j
public class ElasticsearchClientTests extends BaseElasticsearchIntegrationTest {

  @Autowired
  private ElasticsearchClient client;

  @Autowired
  private ElasticsearchProperties properties;

  @Test
  public void init() {
    client.waitForYellowStatus();

    assertTrue(client.isClusterYellow());
    assertNotNull(client.getClient());

    Settings settings = client.getClient().settings();
    assertEquals(properties.getNodeName(), settings.get("node.name"));
    assertEquals(properties.getClusterName(), settings.get("cluster.name"));
  }
}
