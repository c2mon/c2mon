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
package cern.c2mon.server.configuration.parser.util;

import java.util.Properties;

import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;

public class ConfigurationEquipmentUtil {

  /**
   * Expected generated id is 10.
   * Expected process id is 1.
   * Expected status tag id is 101
   * Expected commFault tag id is 100
   */
  public static Equipment buildCreateBasicEquipment(Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Equipment equipment = Equipment.create("E_TEST", "cern.c2mon.daq.testhandler.TestMessageHandler").build();
    equipment.setProcessId(1L);

    properties.setProperty("name", "E_TEST");
    properties.setProperty("description", "<no description provided>");
    properties.setProperty("statusTagId", String.valueOf(101l));
    properties.setProperty("commFaultTagId", String.valueOf(100l));
    properties.setProperty("aliveInterval", String.valueOf(60000));
    properties.setProperty("processId", String.valueOf(1l));
    properties.setProperty("handlerClass", "cern.c2mon.daq.testhandler.TestMessageHandler");

    return equipment;
  }

  /**
   * Expected process id is 1.
   * Expected alive tag id is 100
   * Expected commFault tag id is 101
   * Expected status tag id is 102
   */
  public static Equipment buildCreateAllFieldsEquipment(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Equipment equipment = Equipment.create("E_TEST"+id, "cern.c2mon.daq.testhandler.TestMessageHandler")
        .id(id)
        .description("foo")
        .commFaultTag(CommFaultTag.create("commFaultTag").build())
        .aliveTag(AliveTag.create("aliveTag").build(), 70000)
        .statusTag(StatusTag.create("statusTag").build())
        .address("testAddress")
        .build();
    equipment.setProcessId(1L);

    properties.setProperty("name", "E_TEST"+id);
    properties.setProperty("description", "foo");
    properties.setProperty("statusTagId", String.valueOf(101l));
    properties.setProperty("commFaultTagId", String.valueOf(102l));
    properties.setProperty("aliveInterval", String.valueOf(70000));
    properties.setProperty("aliveTagId", String.valueOf(100l));
    properties.setProperty("processId", String.valueOf(1l));
    properties.setProperty("handlerClass", "cern.c2mon.daq.testhandler.TestMessageHandler");
    properties.setProperty("address", "testAddress");

    return equipment;
  }
  public static Equipment buildUpdateEquipmentWithAllFields(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Equipment equipment = Equipment.update(id)
        .name("E_UPDATE")
        .description("foo")
        .aliveInterval(70000)
        .handlerClass("cern.c2mon.daq.testhandler.TestMessageHandler.Update")
        .address("updateAddress")
        .build();

    properties.setProperty("description", "foo");
    properties.setProperty("aliveInterval", String.valueOf(70000));
    properties.setProperty("name", "E_UPDATE");
    properties.setProperty("handlerClass", "cern.c2mon.daq.testhandler.TestMessageHandler.Update");
    properties.setProperty("address", "updateAddress");

    return equipment;
  }

  public static Equipment buildUpdateEquipmentWithSomeFields(Long id, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Equipment equipment = Equipment.update(id)
        .description("foo_Update")
        .address("updateAddress")
        .build();

    properties.setProperty("description", "foo_Update");
    properties.setProperty("address", "updateAddress");

    return equipment;
  }

  public static Equipment buildUpdateEquipmentWithSomeFields(String name, Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }

    Equipment equipment = Equipment.update(name)
        .description("foo_Update")
        .address("updateAddress")
        .build();

    properties.setProperty("name", name);
    properties.setProperty("description", "foo_Update");
    properties.setProperty("address", "updateAddress");

    return equipment;
  }

  public static Equipment buildDeleteEquipment(Long id) {
    Equipment deleteEquipment = new Equipment();
    deleteEquipment.setId(id);
    deleteEquipment.setDeleted(true);

    return deleteEquipment;
  }
}
