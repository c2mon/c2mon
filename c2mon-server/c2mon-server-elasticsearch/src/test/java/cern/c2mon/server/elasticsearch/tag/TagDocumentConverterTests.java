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
package cern.c2mon.server.elasticsearch.tag;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.elasticsearch.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Alban Marguet
 * @author Matthias Braeger
 * @author Justin Lewis Salmon
 */
@RunWith(MockitoJUnitRunner.class)
public class TagDocumentConverterTests {

  @InjectMocks
  private TagDocumentConverter converter;

  @Mock
  private ProcessCache processCache;

  @Mock
  private EquipmentCache equipmentCache;

  @Test
  public void toAndFromJson() {
    DataTag tag = EntityUtils.createDataTag();
    when(processCache.get(any())).thenReturn(new ProcessCacheObject(1L));
    when(equipmentCache.get(any())).thenReturn(new EquipmentCacheObject(1L));
    TagDocument document = converter.convert(tag);

    // Serialize
    String json = document.toString();

    // Deserialize
    document = (TagDocument) document.getObject(json);

    assertEquals(tag.getId().intValue(), document.get("id"));
    assertEquals(tag.getName(), document.get("name"));
    assertEquals(tag.getTimestamp().getTime(), document.get("timestamp"));
    assertEquals(tag.getUnit(), document.get("unit"));
    assertEquals(tag.getValueDescription(), document.get("valueDescription"));

    Map<String, Object> quality = (Map<String, Object>) document.get("quality");
    assertEquals(tag.getDataTagQuality().isValid(), quality.get("valid"));

    Map<String, Object> c2monMetadata = (Map<String, Object>) document.get("c2mon");
    assertEquals(tag.getDataType(), c2monMetadata.get("dataType"));
    assertEquals(null, c2monMetadata.get("process"));
    assertEquals(null, c2monMetadata.get("equipment"));
    assertEquals(tag.getCacheTimestamp().getTime(), c2monMetadata.get("serverTimestamp"));
    assertEquals(tag.getDaqTimestamp().getTime(), c2monMetadata.get("daqTimestamp"));
    assertEquals(tag.getSourceTimestamp().getTime(), c2monMetadata.get("sourceTimestamp"));

    Map<String, Object> metadata = (Map<String, Object>) document.get("metadata");
    assertEquals(tag.getMetadata().getMetadata().get("building"), metadata.get("building"));
    assertEquals(tag.getMetadata().getMetadata().get("array"), metadata.get("array"));
    assertEquals(tag.getMetadata().getMetadata().get("responsiblePerson"), metadata.get("responsiblePerson"));
  }
}
