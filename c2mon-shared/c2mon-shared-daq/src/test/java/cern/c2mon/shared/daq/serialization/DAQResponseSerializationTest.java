/*******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.daq.command.SourceCommandTagReport;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueResponse;

import static org.junit.Assert.assertEquals;

/**
 * @author Franz Ritter
 */
public class DAQResponseSerializationTest {

  @Test
  public void
  serializeSourceDataTagValueResponse() {
    SourceDataTagValueResponse response = createDataTagResponse();
    String jsonResponse = MessageConverter.responseToJson(response);
    SourceDataTagValueResponse responseFromJson = MessageConverter.responseFromJson(jsonResponse, SourceDataTagValueResponse.class);

    assertEquals(response.getAllDataTagValueObjects().size(), responseFromJson.getAllDataTagValueObjects().size());
    assertEquals(response.getAllDataTagValueObjects(), responseFromJson.getAllDataTagValueObjects());
    assertEquals(response.getStatus(), responseFromJson.getStatus());
    assertEquals(response.getErrorMessage(), responseFromJson.getErrorMessage());
  }

  @Test
  public void
  serializeSourceCommandTagReport() {
    SourceCommandTagReport response = createSourceCommandTagReport();
    String jsonResponse = MessageConverter.responseToJson(response);
    SourceCommandTagReport responseFromJson = MessageConverter.responseFromJson(jsonResponse, SourceCommandTagReport.class);

    assertEquals(response, responseFromJson);
  }

  @Test
  public void
  serializeConfigurationChangeEventReport() {
    ConfigurationChangeEventReport response = createConfigurationChangeEventReport();
    String jsonResponse = MessageConverter.responseToJson(response);
    ConfigurationChangeEventReport responseFromJson = MessageConverter.responseFromJson(jsonResponse, ConfigurationChangeEventReport.class);

    assertEquals(response, responseFromJson);
  }

  private SourceDataTagValueResponse createDataTagResponse() {
    SourceDataTagValue sourceDataTagValue = SourceDataTagValue.builder()
        .id(10L)
        .name("DataTag name")
        .controlTag(false)
        .value(null)
        .quality(new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE))
        .timestamp(new Timestamp(System.currentTimeMillis()))
        .daqTimestamp(new Timestamp(System.currentTimeMillis()))
        .priority(DataTagConstants.PRIORITY_LOW)
        .valueDescription("test description")
        .timeToLive(DataTagAddress.TTL_FOREVER)
        .build();
    ArrayList<SourceDataTagValue> values = new ArrayList<>(Collections.singletonList(sourceDataTagValue));

    DataTagValueUpdate update = new DataTagValueUpdate(1L, 2L, values);
    return new SourceDataTagValueResponse(update);
  }

  private SourceCommandTagReport createSourceCommandTagReport() {

    return new SourceCommandTagReport(
        1L,
        "name",
        SourceCommandTagReport.Status.STATUS_OK,
        "cmd execute description",
        "returnValue",
        System.currentTimeMillis());
  }


  private ConfigurationChangeEventReport createConfigurationChangeEventReport() {
    ChangeReport report = new ChangeReport();
    report.setChangeId(1l);
    report.setErrorMessage("errorMessage");
    report.setInfoMessage("infoMessage");
    report.setState(ChangeReport.CHANGE_STATE.FAIL);
    report.setWarnMessage("WarnMessage");

    ConfigurationChangeEventReport result = new ConfigurationChangeEventReport();
    result.appendChangeReport(report);

    return result;
  }


}
