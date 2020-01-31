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
package cern.c2mon.server.elasticsearch;

import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import cern.c2mon.server.elasticsearch.util.EmbeddedElasticsearchManager;

import static org.junit.Assert.assertEquals;

/**
 * Test the startup of embedded ES instance
 *
 * NOTE: The naming convention (&lt;class name&gt;TestSuite) is used specifically to prevent test execution plugins
 * (like Surefire) to execute the tests individually.
 *
 * @author Alban Marguet
 * @author Serhiy Boychenko
 */
@Slf4j
public class ElasticsearchModuleIntegrationTestSuite extends ElasticsearchTestDefinition {

  @Test
  public void testModuleStartup() throws IOException {
    List<String> indexData = EmbeddedElasticsearchManager.getEmbeddedNode().fetchAllDocuments();
    assertEquals("Embedded node should not contain any documents before each test and start successfully.",
        0, indexData.size());
  }
}
