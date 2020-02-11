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
package cern.c2mon.server.configuration.helper;

import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.common.equipment.AbstractEquipmentCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.test.device.ObjectComparison;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;

import static org.junit.Assert.assertEquals;

/**
 * Junit helper class for comparing cache objects.
 *
 * @author Mark Brightwell
 */
public class ObjectEqualityComparison {


  public static void assertAliveTimerValuesEquals(AliveTag expectedObject, AliveTag object) {
    assertEquals(expectedObject.getId(), object.getId());
    assertEquals(expectedObject.getSupervisedId(), object.getSupervisedId());
    assertEquals(expectedObject.getSupervisedName(), object.getSupervisedName());
    assertEquals(expectedObject.getStateTagId(), object.getStateTagId());
    assertEquals(expectedObject.getSupervisedEntity(), object.getSupervisedEntity());
    assertEquals(expectedObject.getAliveInterval(), object.getAliveInterval());
  }

  public static void assertCommandTagEquals(CommandTagCacheObject expectedObject, CommandTagCacheObject object) {
    assertEquals(expectedObject.getId(), object.getId());
    assertEquals(expectedObject.getName(), object.getName());
    assertEquals(expectedObject.getDescription(), object.getDescription());
    assertEquals(expectedObject.getMode(), object.getMode());
    assertEquals(expectedObject.getDataType(), object.getDataType());
    assertEquals(expectedObject.getAuthorizationDetails().getRbacClass(), object.getAuthorizationDetails().getRbacClass());
    assertEquals(expectedObject.getAuthorizationDetails().getRbacDevice(), object.getAuthorizationDetails().getRbacDevice());
    assertEquals(expectedObject.getAuthorizationDetails().getRbacProperty(), object.getAuthorizationDetails().getRbacProperty());
    assertEquals(expectedObject.getSourceRetries(), object.getSourceRetries());
    assertEquals(expectedObject.getSourceTimeout(), object.getSourceTimeout());
    assertEquals(expectedObject.getExecTimeout(), object.getExecTimeout());
    assertEquals(expectedObject.getClientTimeout(), object.getClientTimeout());
    assertEquals(expectedObject.getEquipmentId(), object.getEquipmentId());
    assertEquals(expectedObject.getHardwareAddress().toConfigXML(), object.getHardwareAddress().toConfigXML());
  }

  public static void assertEquipmentEquals(EquipmentCacheObject expectedObject, EquipmentCacheObject actualObject) {
    assertAbstractEquipmentEquals(expectedObject, actualObject);
    assertEquals(expectedObject.getProcessId(), actualObject.getProcessId());
    assertEquals(expectedObject.getAddress(), actualObject.getAddress());
    assertEquals(expectedObject.getCommandTagIds(), actualObject.getCommandTagIds());
  }

  public static void assertAbstractEquipmentEquals(AbstractEquipmentCacheObject expectedObject, AbstractEquipmentCacheObject actualObject) {
    assertEquals(expectedObject.getId(), actualObject.getId());
    assertEquals(expectedObject.getName(), actualObject.getName());
    assertEquals(expectedObject.getDescription(), actualObject.getDescription());
    assertEquals(expectedObject.getHandlerClassName(), actualObject.getHandlerClassName());
    assertEquals(expectedObject.getAliveInterval(), actualObject.getAliveInterval());
    assertEquals(expectedObject.getStateTagId(), actualObject.getStateTagId());
    assertEquals(expectedObject.getAliveTagId(), actualObject.getAliveTagId());
    assertEquals(expectedObject.getCommFaultTagId(), actualObject.getCommFaultTagId());
    assertEquals(expectedObject.getCommFaultTagValue(), actualObject.getCommFaultTagValue());
  }

  public static void assertProcessEquals(ProcessCacheObject expectedObject, ProcessCacheObject cacheObject) {
    assertEquals(expectedObject.getId(), cacheObject.getId());
    assertEquals(expectedObject.getName(), cacheObject.getName());
    assertEquals(expectedObject.getDescription(), cacheObject.getDescription());
    assertEquals(expectedObject.getAliveInterval(), cacheObject.getAliveInterval());
    assertEquals(expectedObject.getAliveTagId(), cacheObject.getAliveTagId());
    assertEquals(expectedObject.getMaxMessageDelay(), cacheObject.getMaxMessageDelay());
    assertEquals(expectedObject.getMaxMessageSize(), cacheObject.getMaxMessageSize());
    assertEquals(expectedObject.getStateTagId(), cacheObject.getStateTagId());
    assertEquals(expectedObject.getEquipmentIds(), cacheObject.getEquipmentIds());
  }

  public static void assertAlarmEquals(AlarmCacheObject expectedObject, AlarmCacheObject cacheObject) {
    assertEquals(expectedObject.getId(), cacheObject.getId());
    assertEquals(expectedObject.getFaultFamily(), cacheObject.getFaultFamily());
    assertEquals(expectedObject.getFaultMember(), cacheObject.getFaultMember());
    assertEquals(expectedObject.getFaultCode(), cacheObject.getFaultCode());
    assertEquals(expectedObject.getCondition(), cacheObject.getCondition());
    assertEquals(expectedObject.getDataTagId(), cacheObject.getDataTagId());
  }

  public static void assertDeviceEquals(DeviceCacheObject expectedObject, DeviceCacheObject cacheObject) throws ClassNotFoundException {
    assertEquals(expectedObject.getId(), cacheObject.getId());
    assertEquals(expectedObject.getName(), cacheObject.getName());
    assertEquals(expectedObject.getDeviceClassId(), cacheObject.getDeviceClassId());
    assertEquals(expectedObject.getDeviceProperties().size(), cacheObject.getDeviceProperties().size());
    for (DeviceProperty property : expectedObject.getDeviceProperties()) {
      ObjectComparison.assertDevicePropertyListContains(cacheObject.getDeviceProperties(), property);
    }
    for (DeviceCommand deviceCommand : expectedObject.getDeviceCommands()) {
      ObjectComparison.assertDeviceCommandListContains(cacheObject.getDeviceCommands(), deviceCommand);
    }
  }
}
