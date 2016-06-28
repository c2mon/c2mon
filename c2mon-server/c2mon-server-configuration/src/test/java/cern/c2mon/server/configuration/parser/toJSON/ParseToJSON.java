/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.configuration.parser.toJSON;


import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import cern.c2mon.shared.client.configuration.serialisation.HardwareAddressDeserializer;
import cern.c2mon.shared.client.configuration.serialisation.HardwareAddressSerializer;
import cern.c2mon.shared.common.datatag.address.ENSHardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.buildCreateBasicAlarm;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildCreateBasicDataTag;
import static cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil.buildCreateBasicEquipment;
import static cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil.buildCreateBasicProcess;
import static cern.c2mon.server.configuration.parser.util.ConfigurationRuleTagUtil.buildCreateBasicRuleTag;
import static cern.c2mon.server.configuration.parser.util.ConfigurationSubEquipmentUtil.buildCreateBasicSubEquipment;
import static cern.c2mon.shared.common.datatag.address.JMXHardwareAddress.ReceiveMethod.poll;
import static org.junit.Assert.assertEquals;

/**
 * @author Franz Ritter
 */
public class ParseToJSON {

  private static ObjectMapper mapper = new ObjectMapper();

  @BeforeClass
  public static void setUpParser(){
    SimpleModule module = new SimpleModule();
    module.addSerializer(HardwareAddress.class, new HardwareAddressSerializer());
    module.addDeserializer(HardwareAddress.class,new HardwareAddressDeserializer());
    mapper.registerModule(module);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  }

  @Test
  public void parseTagWithDBHardwareAddress(){

    HardwareAddress address = new DBHardwareAddressImpl("itemName");
    HardwareAddress readAddress = serializeDeserializeAddress(address);

    assertEquals(address, readAddress);
  }

  @Test
  public void parseTagWithDIPHardwareAddress(){

    HardwareAddress address = new DIPHardwareAddressImpl("itemName", "fieldName", 1);
    HardwareAddress readAddress = serializeDeserializeAddress(address);

    assertEquals(address, readAddress);

  }

  @Test
  public void parseTagWithENSHardwareAddress(){

    HardwareAddress address = new ENSHardwareAddressImpl("pAddress", ENSHardwareAddress.TYPE_ANALOG);
    HardwareAddress readAddress = serializeDeserializeAddress(address);

    assertEquals(address, readAddress);

  }

  @Test
  public void parseTagWithJAPCHardwareAddress(){

    HardwareAddress address = new JAPCHardwareAddressImpl("pDeviceName","pPropertyName", "dataFieldName", "commandType", "contextField",  "filter");
    HardwareAddress readAddress = serializeDeserializeAddress(address);

    assertEquals(address, readAddress);

  }

  @Test
  public void parseTagWithJMXHardwareAddress(){

    HardwareAddress address = new JMXHardwareAddressImpl("objectName", "attribute", "callMethod", 100, "compositeField","mapField", poll.toString());
    HardwareAddress readAddress = serializeDeserializeAddress(address);

    assertEquals(address, readAddress);

  }

  @Test
  public void parseTagWithOPCHardwareAddress(){

    HardwareAddress address = new OPCHardwareAddressImpl("pItemName", 100);
    HardwareAddress readAddress = serializeDeserializeAddress(address);

    assertEquals(address, readAddress);

  }

  @Test
  public void parseTagWithPLCHardwareAddress(){

    HardwareAddress address = new PLCHardwareAddressImpl(1, 2, 3, 4, 4.0f, 5.0f, "pNativeAddress", 5000);
    HardwareAddress readAddress = serializeDeserializeAddress(address);

    assertEquals(address, readAddress);

  }

  @Test
  public void parseTagWithSimpleHardwareAddress(){

    HardwareAddress address = new SimpleHardwareAddressImpl("simpleName");
    HardwareAddress readAddress = serializeDeserializeAddress(address);

    assertEquals(address, readAddress);

  }

  @Test
  public void parseTagWithSSHHardwareAddress(){

    HardwareAddress address = new SSHHardwareAddressImpl("pServerAlias","pUserName","pUserPasswd", "pSystemCall",
        100L, 50L,SSHHardwareAddressImpl.XML_PROTOCOL,"pSshKey", "pKeyPassphrase");
    HardwareAddress readAddress = serializeDeserializeAddress(address);

    assertEquals(address, readAddress);

  }

  private HardwareAddress serializeDeserializeAddress(HardwareAddress address){
    try {
      String jsonMessage = mapper.writeValueAsString(address);

      return mapper.readValue(jsonMessage, HardwareAddress.class);
    } catch (IOException e) {
      return null;
    }

  }


  @Test
  public void parseCrudConfiguration() {

    Configuration insertConfig = Configuration.builder().name("Default configuration").confId(-1L).application("No application defined").build();

    Process createProcess = Process.create("P_TEST").statusTag(StatusTag.create("Status_P").build()).build();
    Equipment createEquipment = Equipment.create("E_TEST", "handlerClassName").id(33L).build();
    createEquipment.setParentProcessName("P_TEST");
    DataTag updateTag = DataTag.update("The DataTag").maxValue(10).description("dataTagUpdate").build();

    List<ConfigurationEntity> configList = new ArrayList<>();
    configList.add(createProcess);
    configList.add(createEquipment);
    configList.add(updateTag);

    insertConfig.setEntities(configList);

    Configuration readConfig = serializeDeserializeConfiguration(insertConfig);

    assertEquals(insertConfig, readConfig);
  }

  @Test
  public void parseComplexConfiguration2() {

    Process process = buildCreateBasicProcess(null);
    Equipment equipment = buildCreateBasicEquipment(null);
    SubEquipment subEquipment = buildCreateBasicSubEquipment(null);
    DataTag dataTag = buildCreateBasicDataTag(null);
    Alarm alarm = buildCreateBasicAlarm(null);
    RuleTag ruleTag = buildCreateBasicRuleTag(null);

    List<ConfigurationEntity> entities = Arrays.asList(process, equipment, subEquipment, dataTag, alarm, ruleTag);
    Configuration insert = new Configuration();
    insert.setEntities(entities);

    Configuration confRead = serializeDeserializeConfiguration(insert);

    assertEquals(insert, confRead);
  }

  private Configuration serializeDeserializeConfiguration(Configuration config){
    try {
      String jsonMessage = mapper.writeValueAsString(config);

      return mapper.readValue(jsonMessage, Configuration.class);
    } catch (IOException e) {
      return null;
    }

  }

}
