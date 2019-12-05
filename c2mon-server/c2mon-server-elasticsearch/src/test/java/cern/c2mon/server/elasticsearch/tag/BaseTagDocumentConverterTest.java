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
package cern.c2mon.server.elasticsearch.tag;

import java.util.Map;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTag;

import static org.junit.Assert.assertEquals;

/**
 * @author Justin Lewis Salmon
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class BaseTagDocumentConverterTest {

  @InjectMocks
  BaseTagDocumentConverter baseConverter;

  @Mock
  protected ProcessCache processCache;

  @Mock
  protected EquipmentCache equipmentCache;

  @Mock
  protected SubEquipmentCache subEquipmentCache;

  protected void assertBaseFieldsMatch(DataTag tag, Map<String, Object> document) {
    assertEquals(tag.getId().intValue(), document.get("id"));
    assertEquals(tag.getName(), document.get("name"));
    assertEquals(tag.getDescription(), document.get("description"));
    assertEquals(tag.getUnit(), document.get("unit"));
    assertEquals(tag.getMode(), ((Integer) document.get("mode")).shortValue());

    Map<String, Object> metadata = (Map<String, Object>) document.get("metadata");
    assertEquals(tag.getMetadata().getMetadata().get("building"), metadata.get("building"));
    assertEquals(tag.getMetadata().getMetadata().get("array"), metadata.get("array"));
    assertEquals(tag.getMetadata().getMetadata().get("responsiblePerson"), metadata.get("responsiblePerson"));
  }
}
