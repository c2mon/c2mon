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
package cern.c2mon.server.test.device;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.shared.client.device.Command;
import cern.c2mon.shared.client.device.Property;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;

/**
 * @author Justin Lewis Salmon
 */
public class ObjectComparison {

  public static void assertDevicePropertyEquals(DeviceProperty expectedObject, DeviceProperty cacheObject) throws ClassNotFoundException {
    assertEquals(expectedObject.getId(), cacheObject.getId());
    assertEquals(expectedObject.getName(), cacheObject.getName());
    assertEquals(expectedObject.getCategory(), cacheObject.getCategory());

    if ((expectedObject.getCategory() != null && expectedObject.getCategory().equals("mappedProperty")) || expectedObject.getFields() != null) {
      for (DeviceProperty field : expectedObject.getFields().values()) {
        assertDevicePropertyListContains(new ArrayList<>(cacheObject.getFields().values()), field);
      }
    } else {
      assertEquals(expectedObject.getValue(), cacheObject.getValue());
      assertEquals(expectedObject.getResultType(), cacheObject.getResultType());
    }
  }

  public static void assertDevicePropertyListContains(List<DeviceProperty> deviceProperties, DeviceProperty expectedObject) throws ClassNotFoundException {
    for (DeviceProperty deviceProperty : deviceProperties) {
      if (deviceProperty.getId().equals(expectedObject.getId())) {
        assertDevicePropertyEquals(expectedObject, deviceProperty);
      }
    }
  }

  public static void assertPropertyEquals(Property expectedObject, Property cacheObject) throws ClassNotFoundException {
    assertEquals(expectedObject.getId(), cacheObject.getId());
    assertEquals(expectedObject.getName(), cacheObject.getName());
    assertEquals(expectedObject.getDescription(), cacheObject.getDescription());

    if (expectedObject.getFields() != null) {
      for (Property field : expectedObject.getFields()) {
        assertPropertyListContains(cacheObject.getFields(), field);
      }
    }
  }

  public static void assertPropertyListContains(List<Property> properties, Property expectedObject) throws ClassNotFoundException {
    for (Property property : properties) {
      if (property.getName().equals(expectedObject.getName())) {
        assertPropertyEquals(expectedObject, property);
      }
    }
  }

  public static void assertDeviceCommandEquals(DeviceCommand expectedObject, DeviceCommand cacheObject) {
    assertEquals(expectedObject.getId(), cacheObject.getId());
    assertEquals(expectedObject.getName(), cacheObject.getName());
    assertEquals(expectedObject.getValue(), cacheObject.getValue());
    assertEquals(expectedObject.getCategory(), cacheObject.getCategory());
    assertEquals(expectedObject.getResultType(), cacheObject.getResultType());
  }

  public static void assertDeviceCommandListContains(List<DeviceCommand> deviceCommands, DeviceCommand expectedObject) {
    for (DeviceCommand deviceCommand : deviceCommands) {
      if (deviceCommand.getName().equals(expectedObject.getName())) {
        assertDeviceCommandEquals(expectedObject, deviceCommand);
      }
    }
  }

  public static void assertCommandEquals(Command expectedObject, Command actualObject) throws ClassNotFoundException {
    assertEquals(expectedObject.getName(), actualObject.getName());
    assertEquals(expectedObject.getId(), actualObject.getId());
    assertEquals(expectedObject.getDescription(), actualObject.getDescription());
  }

  public static void assertCommandListContains(List<Command> commands, Command expectedObject) throws ClassNotFoundException {
    for (Command command : commands) {
      if (command.getName().equals(expectedObject.getName())) {
        assertCommandEquals(expectedObject, command);
      }
    }
  }
}
