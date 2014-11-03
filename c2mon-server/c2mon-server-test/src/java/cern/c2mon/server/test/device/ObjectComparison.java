/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.test.device;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.server.common.device.Command;
import cern.c2mon.server.common.device.Property;
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

    if (expectedObject.getCategory().equals("mappedProperty") || expectedObject.getFields() != null) {
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
      if (deviceProperty.getName().equals(expectedObject.getName())) {
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
