/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.shared.daq.serialization;

import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.daq.command.SourceCommandTagReport;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueRequest;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueResponse;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Franz Ritter
 */
public class ServerRequestSerializationTest {

  @Test
  public void
  serializeSourceCommandTagValue(){
    SourceCommandTagValue request = createSourceCommandTagValue();
    String jsonResponse = MessageConverter.requestToJson(request);
    SourceCommandTagValue responseFromJson = (SourceCommandTagValue) MessageConverter.requestFromJson(jsonResponse);

    assertEquals(request, responseFromJson);
  }

  @Test
  public void
  serializeSourceDataTagValueRequest(){
    SourceDataTagValueRequest request = createSourceDataTagValueRequest();
    String jsonResponse = MessageConverter.requestToJson(request);
    SourceDataTagValueRequest responseFromJson = (SourceDataTagValueRequest) MessageConverter.requestFromJson(jsonResponse);

    assertEquals(request, responseFromJson);
  }


  private SourceCommandTagValue createSourceCommandTagValue(){
    return new SourceCommandTagValue(1L, "name", 2L, (short)0, 1234, Integer.class.getName());
  }

  private SourceDataTagValueRequest createSourceDataTagValueRequest(){
    return new SourceDataTagValueRequest(SourceDataTagValueRequest.DataTagRequestType.DATATAG, 1L);
  }
}
