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
package cern.c2mon.server.configuration.parser.toJSON;


import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.common.metadata.Metadata;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static cern.c2mon.server.configuration.parser.util.ConfigurationAllTogetherUtil.buildAllMandatoryWithMetadata;
import static org.junit.Assert.assertEquals;

/**
 * Created by fritter on 30/11/15.
 */
public class ParseToJSON {

  @Test
  public void parseComplexConfiguration() {
    Configuration insert = buildAllMandatoryWithMetadata();

    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(new File("test.json"), insert);
      Configuration confRead = mapper.readValue(new File("test.json"), Configuration.class);

      assertEquals(confRead, insert);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void simpleConf(){
    Configuration conf = Configuration.builder()
        .process(Process.builder().id(1L)
            .equipment(Equipment.builder().id(2L)
                .dataTag(DataTag.builder().id(1377L).metadata(Metadata.builder().addMetadata("Str",1).build()).build()).build()).build()).build();
  }
}
