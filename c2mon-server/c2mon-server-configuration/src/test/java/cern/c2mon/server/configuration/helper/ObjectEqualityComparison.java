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
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.device.Command;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.server.common.device.Property;
import cern.c2mon.server.common.equipment.AbstractEquipmentCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.test.device.ObjectComparison;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Junit helper class for comparing cache objects.
 *
 * @author Mark Brightwell
 */
public class ObjectEqualityComparison {


  public static void assertAliveTimerValuesEquals(AliveTimerCacheObject expectedObject, AliveTimerCacheObject object) {
    assertEquals(expectedObject.getId(), object.getId());
    assertEquals(expectedObject.getRelatedId(), object.getRelatedId());
    assertEquals(expectedObject.getRelatedName(), object.getRelatedName());
    assertEquals(expectedObject.getRelatedStateTagId(), object.getRelatedStateTagId());
    assertEquals(expectedObject.getAliveType(), object.getAliveType());
    assertEquals(expectedObject.getAliveInterval(), object.getAliveInterval());
  }

  public static void assertDataTagConfigEquals(DataTagCacheObject expectedObject, DataTagCacheObject object) {
    assertTagConfigEquals(expectedObject, object);
    assertEquals(expectedObject.getEquipmentId(), object.getEquipmentId());
    assertEquals(expectedObject.getProcessId(), object.getProcessId());
    assertEquals(expectedObject.getMinValue(), object.getMinValue());
    assertEquals(expectedObject.getMaxValue(), object.getMaxValue());
    if (expectedObject.getAddress() != null) {
      assertEquals(expectedObject.getAddress().toConfigXML(), object.getAddress().toConfigXML());
    }
    if (expectedObject.getMetadata() != null) {
      assertTrue(expectedObject.getMetadata().equals(object.getMetadata()));
    }
  }

  public static void assertTagConfigEquals(AbstractTagCacheObject expectedObject, AbstractTagCacheObject object) {
    assertEquals(expectedObject.getId(), object.getId());
    assertEquals(expectedObject.getName(), object.getName());
    assertEquals(expectedObject.getDescription(), object.getDescription());
    assertEquals(expectedObject.getMode(), object.getMode());
    assertEquals(expectedObject.getDataType(), object.getDataType());
    assertEquals(expectedObject.isLogged(), object.isLogged());
    assertEquals(expectedObject.getUnit(), object.getUnit());
    assertEquals(expectedObject.getDipAddress(), object.getDipAddress());
    assertEquals(expectedObject.getJapcAddress(), object.getJapcAddress());
    assertEquals(expectedObject.isSimulated(), object.isSimulated());
    assertEquals(expectedObject.getMetadata(), object.getMetadata());
    // assertEquals(expectedObject.getValueDictionary().toXML(),
    // object.getValueDictionary().toXML()); //compare XML of value dictionary
    assertEquals(expectedObject.getDataTagQuality(), object.getDataTagQuality());
    assertEquals(expectedObject.getRuleIdsString(), object.getRuleIdsString());
    assertEquals(expectedObject.getRuleIds(), object.getRuleIds());
    assertEquals(((Tag) expectedObject).getProcessIds(), ((Tag) object).getProcessIds());
    assertEquals(((Tag) expectedObject).getEquipmentIds(), ((Tag) object).getEquipmentIds());
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

  public static void assertRuleTagConfigEquals(RuleTagCacheObject expectedObject, RuleTagCacheObject cacheObject) {
    assertTagConfigEquals(expectedObject, cacheObject);
    assertEquals(expectedObject.getRuleText(), cacheObject.getRuleText());
  }

  public static void assertEquipmentEquals(EquipmentCacheObject expectedObject, EquipmentCacheObject actualObject) {
    assertAbstractEquipmentEquals(expectedObject, actualObject);
    assertEquals(expectedObject.getProcessId(), actualObject.getProcessId());
    assertEquals(expectedObject.getAddress(), actualObject.getAddress());
    assertEquals(expectedObject.getCommandTagIds(), actualObject.getCommandTagIds());
    assertEquals(expectedObject.getSubEquipmentIds(), actualObject.getSubEquipmentIds());
  }

  public static void assertSubEquipmentEquals(SubEquipmentCacheObject expectedObject, SubEquipmentCacheObject actualObject) {
    assertAbstractEquipmentEquals(expectedObject, actualObject);
    assertEquals(expectedObject.getParentId(), actualObject.getParentId());
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

  public static void assertDeviceClassEquals(DeviceClassCacheObject expectedObject, DeviceClassCacheObject cacheObject) throws ClassNotFoundException {
    assertEquals(expectedObject.getId(), cacheObject.getId());
    assertEquals(expectedObject.getName(), cacheObject.getName());
    assertEquals(expectedObject.getDescription(), cacheObject.getDescription());
    assertEquals(expectedObject.getProperties().size(), cacheObject.getProperties().size());

    for (Property property : expectedObject.getProperties()) {
      ObjectComparison.assertPropertyListContains(cacheObject.getProperties(), property);
    }

    assertEquals(expectedObject.getCommands().size(), cacheObject.getCommands().size());
    for (Command command : expectedObject.getCommands()) {
      ObjectComparison.assertCommandListContains(cacheObject.getCommands(), command);
    }
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
