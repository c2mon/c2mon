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
package cern.c2mon.server.elasticsearch.tag.config;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.InjectMocks;

import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.elasticsearch.tag.BaseTagDocumentConverterTest;
import cern.c2mon.server.elasticsearch.util.EntityUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


/**
 * @author Szymon Halastra
 * @author Justin Lewis Salmon
 */
public class TagConfigDocumentConverterTests extends BaseTagDocumentConverterTest {

  @InjectMocks
  private TagConfigDocumentConverter converter;

  @Test
  public void toAndFromJson() throws Exception {
    DataTag tag = EntityUtils.createDataTag();

    ProcessCacheObject process = new ProcessCacheObject(1L);
    process.setName("P_TEST");

    EquipmentCacheObject equipment = new EquipmentCacheObject(1L);
    equipment.setName("E_TEST");

    when(processCache.get(any())).thenReturn(process);
    when(equipmentCache.get(any())).thenReturn(equipment);
    TagConfigDocument document = converter.convert(tag)
        .orElseThrow(() -> new Exception("Tag conversion failed"));

    // Serialize
    String json = document.toString();

    // Deserialize
    document = new ObjectMapper().readValue(json, TagConfigDocument.class);

    super.assertBaseFieldsMatch(tag, document);
    Map<String, Object> c2monMetadata = (Map<String, Object>) document.get("c2mon");
    assertEquals(tag.isLogged(), c2monMetadata.get("logged"));
  }
}
