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
package cern.c2mon.server.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.device.DeviceClassNameResponse;
import cern.c2mon.shared.client.device.DeviceClassNameResponseImpl;
import cern.c2mon.shared.client.device.TransferDevice;
import cern.c2mon.shared.client.device.TransferDeviceImpl;
import cern.c2mon.shared.client.tag.*;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

import static cern.c2mon.shared.common.type.TypeConverter.getType;
import static cern.c2mon.shared.common.type.TypeConverter.isKnownClass;

/**
 * Factory class for creating transfer objects for sending to the C2MON client layer
 *
 * @author Matthias Braeger
 */
public abstract class TransferObjectFactory {

  /**
   * Hidden default constructor
   */
  private TransferObjectFactory() {
    // Do nothing
  }

  /**
   * Creates a <code>TransferTagImpl</code> object for the given parameters
   *
   * @param tagWithAlarms A tag from the cache
   * @param aliveTag set to <code>true</code>, if tag is an Alive tag
   * @return The resulting <code>TransferTagImpl</code>
   */
  public static TransferTagImpl createTransferTag(final TagWithAlarms tagWithAlarms, boolean aliveTag, String topic) {
    Tag tag = tagWithAlarms.getTag();
    TransferTagImpl transferTag = null;
    if (tag != null) {
      transferTag =
          new TransferTagImpl(
              tag.getId(),
              tag.getValue(),
              tag.getValueDescription(),
              (DataTagQualityImpl) tag.getDataTagQuality(),
              getTagMode(tag),
              tag.getTimestamp(),
              tag instanceof DataTag ? ((DataTag) tag).getDaqTimestamp() : null,
              tag.getCacheTimestamp(),
              tag.getDescription(),
              tag.getName(),
              topic);

      String dataType = isKnownClass(tag.getDataType()) ? getType(tag.getDataType()).getName() : tag.getDataType();
      transferTag.setValueClassName(dataType);

      addAlarmValues(transferTag, tagWithAlarms.getAlarms());
      transferTag.setSimulated(tag.isSimulated());
      transferTag.setUnit(tag.getUnit());
      transferTag.addEquipmentIds(tag.getEquipmentIds());
      transferTag.addSubEquipmentIds(tag.getSubEquipmentIds());
      transferTag.addProcessIds(tag.getProcessIds());
      if (tag.getMetadata() != null) {
        transferTag.setMetadata(tag.getMetadata().getMetadata());
      }

      if (tag instanceof RuleTag) {
        transferTag.defineRuleExpression(((RuleTag) tag).getRuleExpression());
      } else if (tag instanceof ControlTag) {
        transferTag.setControlTagFlag(true);
        transferTag.setAliveTagFlag(aliveTag);
      }
    }

    return transferTag;
  }

  /**
   * Creates a <code>TransferTagValueImpl</code> object for the given parameters
   *
   * @param tagWithAlarms A tag from the cache
   * @return The resulting <code>TransferTagValueImpl</code>
   */
  public static TransferTagValueImpl createTransferTagValue(final TagWithAlarms tagWithAlarms) {
    Tag tag = tagWithAlarms.getTag();
    TransferTagValueImpl tagValue = null;
    if (tag != null) {
      tagValue =
          new TransferTagValueImpl(
              tag.getId(),
              tag.getValue(),
              tag.getValueDescription(),
              (DataTagQualityImpl) tag.getDataTagQuality(),
              getTagMode(tag),
              tag.getTimestamp(),
              tag instanceof DataTag ? ((DataTag) tag).getDaqTimestamp() : null,
              tag.getCacheTimestamp(),
              tag.getDescription());

      String dataType = isKnownClass(tag.getDataType()) ? getType(tag.getDataType()).getName() : tag.getDataType();
      tagValue.setValueClassName(dataType);
      addAlarmValues(tagValue, tagWithAlarms.getAlarms());
      tagValue.setSimulated(tag.isSimulated());
    }

    return tagValue;
  }

  /**
   * Creates an <code>AlarmValueImpl</code> object for the given parameters
   *
   * @param alarm An alarm object
   * @return The resulting <code>AlarmValueImpl</code>
   */
  public static AlarmValueImpl createAlarmValue(final Alarm alarm) {
    AlarmValueImpl alarmValueImpl = null;

    if (alarm != null) {
      alarmValueImpl = AlarmValueImpl.builder()
              .id(alarm.getId())
              .faultCode(alarm.getFaultCode())
              .faultMemeber(alarm.getFaultMember())
              .faultFamily(alarm.getFaultFamily())
              .info(alarm.getInfo())
              .alarmConditionDescription(alarm.getCondition().getDescription())
              .alarmConditionXml(alarm.getCondition().getXMLCondition())
              .tagId(alarm.getTagId())
              .timestamp(alarm.getTimestamp())
              .active(alarm.isActive())
              .oscillating(alarm.isOscillating())
              .sourceTimestamp(alarm.getSourceTimestamp()).build();

      if (alarm.getMetadata() != null) {
        alarmValueImpl.setMetadata(alarm.getMetadata().getMetadata());
      }
    }

    return alarmValueImpl;
  }

  /**
   * Creates an <code>AlarmValueImpl</code> object.
   * Also adds Tag Description information.
   *
   * @param alarm An alarm object
   * @param tag   A tag object. Used to get Tag Description information.
   * @return The resulting <code>AlarmValueImpl</code>
   */
  public static AlarmValueImpl createAlarmValue(Alarm alarm, Tag tag) {
    AlarmValueImpl alarmValueImpl = createAlarmValue(alarm);
    alarmValueImpl.setTagDescription(tag.getDescription());
    return alarmValueImpl;
  }

  /**
   * Creates a <code>TagConfigImpl</code> object for the given parameters
   *
   * @param tagWithAlarms A tag from the cache
   * @param tagProcesses  all processes that this tag is attached to (single one for DataTag, multiple for Rules)
   * @return The resulting <code>TransferTagValueImpl</code>
   */
  public static TagConfigImpl createTagConfiguration(final TagWithAlarms tagWithAlarms, final Collection<Process> tagProcesses) {

    Tag tag = tagWithAlarms.getTag();
    TagConfigImpl tagConfig = null;

    if (tag != null) {

      tagConfig = new TagConfigImpl(tag.getId());
      tagConfig.setAlarmIds(new ArrayList<>(tag.getAlarmIds()));

      Boolean controlTag = Boolean.FALSE;
      if (tag instanceof ControlTag) {
        controlTag = Boolean.TRUE;
      }
      tagConfig.setControlTag(controlTag);

      if (tag instanceof DataTag || tag instanceof ControlTag) {
        DataTag dataTag = (DataTag) tag;

        // check if min. value is defined, since it is not mandatory
        if (dataTag.getMinValue() != null)
          tagConfig.setMinValue(dataTag.getMinValue().toString());

        // check if max. value is defined, since it is not mandatory
        if (dataTag.getMaxValue() != null)
          tagConfig.setMaxValue(dataTag.getMaxValue().toString());

        if (dataTag.getAddress() != null) {

          tagConfig.setValueDeadbandType(dataTag.getAddress().getValueDeadbandType());
          tagConfig.setValueDeadband(dataTag.getAddress().getValueDeadband());
          tagConfig.setTimeDeadband(dataTag.getAddress().getTimeDeadband());
          tagConfig.setGuaranteedDelivery(dataTag.getAddress().isGuaranteedDelivery());
          tagConfig.setPriority(dataTag.getAddress().getPriority());
          tagConfig.setAddressParameters(dataTag.getAddress().getAddressParameters());
          if(dataTag.getAddress().getHardwareAddress() != null){
            tagConfig.setHardwareAddress(dataTag.getAddress().getHardwareAddress().toConfigXML());
          }
        }
      }

      if (tag instanceof RuleTag) {
        RuleTag ruleTag = (RuleTag) tag;
        tagConfig.setRuleExpressionStr(ruleTag.getRuleText());
      }
      if (!tag.getRuleIds().isEmpty()) {
        tagConfig.addRuleIds(tag.getRuleIds());
      }
      if (tag.getDipAddress() != null) {
        tagConfig.addPublication(Publisher.DIP, tag.getDipAddress());
      }
      if (tag.getJapcAddress() != null) {
        tagConfig.addPublication(Publisher.JAPC, tag.getJapcAddress());
      }
      if (tag.isLogged()) {
        tagConfig.setLogged(Boolean.TRUE);
      } else {
        tagConfig.setLogged(Boolean.FALSE);
      }
      ArrayList<String> processNames = new ArrayList<>();
      for (Process process : tagProcesses) {
        processNames.add(process.getName());
      }
      tagConfig.setProcessNames(processNames);
    }
    return tagConfig;
  }

  /**
   * Inner method to determine the actual tag mode
   *
   * @param tag The tag for which the mode has to be determined
   * @return The tag mode
   */
  private static TagMode getTagMode(final Tag tag) {
    TagMode mode;
    if (tag.isInOperation()) {
      mode = TagMode.OPERATIONAL;
    } else if (tag.isInMaintenance()) {
      mode = TagMode.MAINTENANCE;
    } else {
      mode = TagMode.TEST;
    }

    return mode;
  }


  /**
   * Private helper method for creating and adding <code>AlarmValueImpl</code> objects to the
   * transfer tag.
   *
   * @param tagValue The tag value to which the alarms will be added
   * @param alarms   The alarms from which the <code>AlarmValueImpl</code> are created from
   */
  private static void addAlarmValues(final TransferTagValueImpl tagValue, final Collection<Alarm> alarms) {
    if (alarms != null) {
      List<AlarmValueImpl> alarmValues = new ArrayList<>(alarms.size());
      for (Alarm alarm : alarms) {
        alarmValues.add(createAlarmValue(alarm));
      }
      tagValue.addAlarmValues(alarmValues);
    }
  }

  /**
   * Creates a <code>DeviceClassNameResponse</code> object for the given device class name.
   *
   * @param name the name of the device class
   * @return the resulting <code>DeviceClassNameResponse</code> object
   */
  public static DeviceClassNameResponse createTransferDeviceName(String name) {
    return new DeviceClassNameResponseImpl(name);
  }

  /**
   * Creates a <code>TransferDevice</code> object for the given device.
   *
   * @param device    the device object to be transferred
   * @param className the name of the device class
   * @return the resulting <code>TransferDevice</code> object
   */
  public static TransferDevice createTransferDevice(Device device, String className) {
    TransferDeviceImpl transferDevice = new TransferDeviceImpl(device.getId(), device.getName(), device.getDeviceClassId(), className);
    transferDevice.addDeviceProperties(device.getDeviceProperties());
    transferDevice.addDeviceCommands(device.getDeviceCommands());
    return transferDevice;
  }
}
