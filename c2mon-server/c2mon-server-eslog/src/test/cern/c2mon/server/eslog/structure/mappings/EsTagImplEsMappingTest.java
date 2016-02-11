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
package cern.c2mon.server.eslog.structure.mappings;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Verify that we always get a EsMapping. Important for a good indexing in ElasticSearch.
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class EsTagImplEsMappingTest {
  private String expectedMapping = "{\n" +
      "  \"_routing\": {\n" +
      "    \"required\": \"true\"\n" +
      "  }\n" +
      "}";

  @Test
  public void testGetESMapping() {
    EsTagMapping mapping = new EsTagMapping();
    assertNotNull(mapping.getMapping());
  }

  @Test
  public void testExpectedOutput() {
    EsTagMapping mapping = new EsTagMapping();
    assertEquals(expectedMapping, mapping.getMapping());
  }
}