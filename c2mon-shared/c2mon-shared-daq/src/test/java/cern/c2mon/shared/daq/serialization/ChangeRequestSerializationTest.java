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

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;
import cern.c2mon.shared.common.datatag.util.JmsMessagePriority;
import cern.c2mon.shared.daq.config.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ChangeRequestSerializationTest {


  @Test
  public void testDataTagUpdateElement() {
    DataTagUpdate dataTagUpdate = createDataTagUpdate();
    ChangeRequest changeRequest = new ChangeRequest();
    changeRequest.addChange(dataTagUpdate);

    String jsonRequest = MessageConverter.requestToJson(changeRequest);
    ChangeRequest daqRequest = (ChangeRequest) MessageConverter.requestFromJson(jsonRequest);

    assertEquals(changeRequest, daqRequest);
  }

  @Test
  public void testDataTagAddNode() {
    DataTagAdd dataTagAdd = createDataTagAdd();
    DataTagAdd anotherDataTagAdd = createDataTagAdd();

    ChangeRequest changeRequest = new ChangeRequest();
    changeRequest.addChange(dataTagAdd);
    changeRequest.addChange(anotherDataTagAdd);

    String jsonRequest = MessageConverter.requestToJson(changeRequest);
    ChangeRequest daqRequest = (ChangeRequest) MessageConverter.requestFromJson(jsonRequest);

    assertEquals(changeRequest, daqRequest);
  }

  @Test
  public void testRemoveElement() {
    CommandTagRemove commandTagRemove = new CommandTagRemove(25L, 23L, 234L);
    DataTagRemove dataTagRemove = new DataTagRemove(24L, 22L, 324L);

    ChangeRequest changeRequest = new ChangeRequest();
    changeRequest.addChange(commandTagRemove);
    changeRequest.addChange(dataTagRemove);

    String jsonRequest = MessageConverter.requestToJson(changeRequest);
    ChangeRequest daqRequest = (ChangeRequest) MessageConverter.requestFromJson(jsonRequest);

    assertEquals(changeRequest, daqRequest);
  }

  @Test
  public void testMultipleElements() {
    CommandTagRemove commandTagRemove = new CommandTagRemove(25L, 234L, 23L);
    DataTagRemove dataTagRemove = new DataTagRemove(24L, 22L, 324L);
    DataTagAdd dataTagAdd = createDataTagAdd();
    DataTagAdd anotherDataTagAdd = createDataTagAdd();
    DataTagUpdate dataTagUpdate = createDataTagUpdate();
    EquipmentConfigurationUpdate equipmentConfigurationUpdate = new EquipmentConfigurationUpdate(8324L, 38432L);
    equipmentConfigurationUpdate.setAliveInterval(1000L);

    ChangeRequest changeRequest = new ChangeRequest();
    changeRequest.addChange(commandTagRemove);
    changeRequest.addChange(dataTagRemove);
    changeRequest.addChange(dataTagAdd);
    changeRequest.addChange(anotherDataTagAdd);
    changeRequest.addChange(dataTagUpdate);
    changeRequest.addChange(equipmentConfigurationUpdate);

    String jsonRequest = MessageConverter.requestToJson(changeRequest);
    ChangeRequest daqRequest = (ChangeRequest) MessageConverter.requestFromJson(jsonRequest);

    assertEquals(changeRequest, daqRequest);
  }

  @Test
  public void testChangeEventReport() {
    ConfigurationChangeEventReport changeEventReport = new ConfigurationChangeEventReport();

    ChangeReport changeReport = new ChangeReport(1L);
    changeReport.appendError("<asd");
    changeEventReport.appendChangeReport(changeReport);

    String jsonReport = MessageConverter.responseToJson(changeEventReport);
    ConfigurationChangeEventReport report = MessageConverter.responseFromJson(jsonReport, ConfigurationChangeEventReport.class);

    assertEquals(changeEventReport, report);
  }

  @Test
  public void serialize() throws IOException {
    SourceDataTag sdt = createSourceDataTag();
    sdt.setDataType(Long.class.getName());

    String sdtString = MessageConverter.mapper.writeValueAsString(sdt);
    SourceDataTag sdtFromJseon = MessageConverter.mapper.readValue(sdtString, SourceDataTag.class);
  }


  @Test
  public void testEquipmentUnitAdd() {
    EquipmentUnitAdd eqUnitAdd = createEquipmentUnitAdd();
    ChangeRequest request = new ChangeRequest(Arrays.asList(eqUnitAdd));

    String jsonRequest = MessageConverter.requestToJson(request);
    ChangeRequest serverRequest = (ChangeRequest) MessageConverter.requestFromJson(jsonRequest);

    assertEquals(serverRequest.getChangeList().get(0), eqUnitAdd);
  }


  private DataTagAdd createDataTagAdd() {
    DataTagAdd dataTagAdd = new DataTagAdd(2L, 5L, createSourceDataTag());
    return dataTagAdd;
  }

  private SourceDataTag createSourceDataTag() {
    SourceDataTag dataTag = new SourceDataTag(27L, "Test", false);
    dataTag.setDataType("Integer");
    dataTag.setMaxValue(10);
    try {
      DataTagAddress dataTagAddress = new DataTagAddress();
      dataTagAddress.setPriority(JmsMessagePriority.PRIORITY_MEDIUM);
      OPCHardwareAddressImpl opcHardwareAddressImpl = new OPCHardwareAddressImpl("lala", 12);
      dataTagAddress.setHardwareAddress(opcHardwareAddressImpl);
      dataTag.setAddress(dataTagAddress);
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
    return dataTag;
  }

  private DataTagUpdate createDataTagUpdate() {
    DataTagUpdate dataTagUpdate = new DataTagUpdate(324234L, 2L, 5L);
    DataTagAddressUpdate dataTagAddressUpdate = createDataTagAddressUpdate();
    dataTagUpdate.setDataType("Integer");
    dataTagUpdate.setDataTagAddressUpdate(dataTagAddressUpdate);
    dataTagUpdate.setMaxValue(5);
    dataTagUpdate.addFieldToRemove("minValue");
    return dataTagUpdate;
  }

  private DataTagAddressUpdate createDataTagAddressUpdate() {
    DataTagAddressUpdate dataTagAddressUpdate = new DataTagAddressUpdate();
    dataTagAddressUpdate.setPriority(2);
    HardwareAddressUpdate hardwareAddressUpdate = createHardwareAddressUpdate();
    dataTagAddressUpdate.setHardwareAddressUpdate(hardwareAddressUpdate);
    return dataTagAddressUpdate;
  }

  private HardwareAddressUpdate createHardwareAddressUpdate() {
    HardwareAddressUpdate hardwareAddressUpdate = new HardwareAddressUpdate();
    hardwareAddressUpdate.getChangedValues().put("long", 123);
    hardwareAddressUpdate.getChangedValues().put("string", "asd");
    return hardwareAddressUpdate;
  }

  private EquipmentUnitAdd createEquipmentUnitAdd() {
    EquipmentUnitAdd eqUnitAdd = new EquipmentUnitAdd();
    eqUnitAdd.setChangeId(123L);
    eqUnitAdd.setEquipmentId(100L);
    eqUnitAdd.setEquipmentUnitXml("<EquipmentUnit>test</EquipmentUnit>");
    return eqUnitAdd;
  }

}
