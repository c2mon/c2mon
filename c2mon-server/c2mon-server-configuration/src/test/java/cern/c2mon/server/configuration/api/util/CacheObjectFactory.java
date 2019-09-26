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
package cern.c2mon.server.configuration.api.util;

import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Franz Ritter
 */
@Service
public class CacheObjectFactory {

  public ProcessCacheObject buildProcessCacheObject(Long id, Process configProcess) {
    ProcessCacheObject expectedObject = new ProcessCacheObject(id);
    expectedObject = setCacheProcessCacheObjectFields(expectedObject, configProcess);

    return expectedObject;
  }

  public ProcessCacheObject buildProcessUpdateCacheObject(ProcessCacheObject originalObject, Process configProcess) {
    ProcessCacheObject result = (ProcessCacheObject) originalObject.clone();
    setCacheProcessCacheObjectFields(result, configProcess);

    return result;
  }

  private ProcessCacheObject setCacheProcessCacheObjectFields(ProcessCacheObject cacheObject, Process configObject) {

    if (configObject.getName() != null) {
      cacheObject.setName(configObject.getName());
    }
    if (configObject.getAliveInterval() != null) {
      cacheObject.setAliveInterval(configObject.getAliveInterval());
    }
    if (configObject.getAliveTag() != null) {
      cacheObject.setAliveTagId(configObject.getAliveTag().getId());
    }
    if (configObject.getStatusTag() != null) {
      cacheObject.setStateTagId(configObject.getStatusTag().getId());
    }
    if (configObject.getMaxMessageSize() != null) {
      cacheObject.setMaxMessageSize(configObject.getMaxMessageSize());
    }
    if (configObject.getMaxMessageDelay() != null) {
      cacheObject.setMaxMessageDelay(configObject.getMaxMessageDelay());
    }
    if (configObject.getDescription() != null) {
      cacheObject.setDescription(configObject.getDescription());
    }
    return cacheObject;
  }

  public EquipmentCacheObject buildEquipmentCacheObject(Long id, Equipment configEquipment) {
    EquipmentCacheObject expectedObject = new EquipmentCacheObject(id);
    expectedObject = setCacheEquipmentCacheObjectFields(expectedObject, configEquipment);

    return expectedObject;
  }

  public EquipmentCacheObject buildEquipmentUpdateCacheObject(EquipmentCacheObject originalObject, Equipment configObject) {
    EquipmentCacheObject result = originalObject.clone();
    setCacheEquipmentCacheObjectFields(result, configObject);

    return result;
  }

  private EquipmentCacheObject setCacheEquipmentCacheObjectFields(EquipmentCacheObject cacheObject, Equipment configObject) {

    if (configObject.getName() != null) {
      cacheObject.setName(configObject.getName());
    }
    if (configObject.getAliveInterval() != null) {
      cacheObject.setAliveInterval(configObject.getAliveInterval());
    }
    if (configObject.getAliveTag() != null) {
      cacheObject.setAliveTagId(configObject.getAliveTag().getId());
    }
    if (configObject.getStatusTag() != null) {
      cacheObject.setStateTagId(configObject.getStatusTag().getId());
    }
    if (configObject.getCommFaultTag() != null) {
      cacheObject.setCommFaultTagId(configObject.getCommFaultTag().getId());
    }
    if (configObject.getHandlerClass() != null) {
      cacheObject.setHandlerClassName(configObject.getHandlerClass());
    }
    if (configObject.getAddress() != null) {
      cacheObject.setAddress(configObject.getAddress());
    }
    if (configObject.getProcessId() != null) {
      cacheObject.setProcessId(configObject.getProcessId());
    }
    if (configObject.getDescription() != null) {
      cacheObject.setDescription(configObject.getDescription());
    }
    return cacheObject;
  }

  public SubEquipmentCacheObject buildSubEquipmentCacheObject(Long id, SubEquipment configEquipment) {
    SubEquipmentCacheObject expectedObject = new SubEquipmentCacheObject(id);
    expectedObject = setCacheSubEquipmentCacheObjectFields(expectedObject, configEquipment);

    return expectedObject;
  }

  public SubEquipmentCacheObject buildSubEquipmentUpdateCacheObject(SubEquipmentCacheObject originalObject, SubEquipment configObject) {
    SubEquipmentCacheObject result = originalObject.clone();
    setCacheSubEquipmentCacheObjectFields(result, configObject);

    return result;
  }

  private SubEquipmentCacheObject setCacheSubEquipmentCacheObjectFields(SubEquipmentCacheObject cacheObject, SubEquipment configObject) {

    if (configObject.getName() != null) {
      cacheObject.setName(configObject.getName());
    }
    if (configObject.getAliveInterval() != null) {
      cacheObject.setAliveInterval(configObject.getAliveInterval());
    }
    if (configObject.getAliveTag() != null) {
      cacheObject.setAliveTagId(configObject.getAliveTag().getId());
    }
    if (configObject.getStatusTag() != null) {
      cacheObject.setStateTagId(configObject.getStatusTag().getId());
    }
    if (configObject.getCommFaultTag() != null) {
      cacheObject.setCommFaultTagId(configObject.getCommFaultTag().getId());
    }
    if (configObject.getEquipmentId() != null) {
      cacheObject.setParentId(configObject.getEquipmentId());
    }
    if (configObject.getDescription() != null) {
      cacheObject.setDescription(configObject.getDescription());
    }
    return cacheObject;
  }

  public DataTagCacheObject buildDataTagUpdateCacheObject(DataTagCacheObject originalObject, DataTag configObject) {
    DataTagCacheObject result = null;
    try {
      result = originalObject.clone();
      setCacheDataTagCacheObjectFields(result, configObject);

    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    return result;
  }

  public DataTagCacheObject buildDataTagCacheObject(Long id, DataTag configEquipment) {
    DataTagCacheObject expectedObject = new DataTagCacheObject(id);
    expectedObject = setCacheDataTagCacheObjectFields(expectedObject, configEquipment);
    expectedObject.setProcessId(5L);
    expectedObject.setDataTagQuality(new DataTagQualityImpl());

    return expectedObject;
  }

  private DataTagCacheObject setCacheDataTagCacheObjectFields(DataTagCacheObject cacheObject, DataTag configObject) {

    if (configObject.getName() != null) {
      cacheObject.setName(configObject.getName());
    }
    if (configObject.getAddress() != null) {
      cacheObject.setAddress(configObject.getAddress());
    }
    if (configObject.getDataType() != null) {
      cacheObject.setDataType(configObject.getDataType());
    }
    if (configObject.getEquipmentId() != null) {
      cacheObject.setEquipmentId(configObject.getEquipmentId());
    }
    if (configObject.getSubEquipmentId() != null) {
      cacheObject.setSubEquipmentId(configObject.getSubEquipmentId());
    }
    if (configObject.getDipAddress() != null) {
      cacheObject.setDipAddress(configObject.getDipAddress());
    }
    if (configObject.getJapcAddress() != null) {
      cacheObject.setJapcAddress(configObject.getJapcAddress());
    }
    if (configObject.getUnit() != null) {
      cacheObject.setUnit(configObject.getUnit());
    }
    if (configObject.getMetadata() != null) {
      Metadata metadata = new Metadata();
      metadata.setMetadata(configObject.getMetadata().getMetadata());
      cacheObject.setMetadata(metadata);
    }
    if (configObject.getMode() != null) {
      cacheObject.setMode((short) configObject.getMode().ordinal());
    }
    if (configObject.getIsLogged() != null) {
      cacheObject.setLogged(configObject.getIsLogged());
    }
    if (configObject.getMaxValue() != null) {
      cacheObject.setMaxValue((Comparable) configObject.getMaxValue());
    }
    if (configObject.getMinValue() != null) {
      cacheObject.setMinValue((Comparable) configObject.getMinValue());
    }
    if (configObject.getDescription() != null) {
      cacheObject.setDescription(configObject.getDescription());
    }
    return cacheObject;
  }

  public RuleTagCacheObject buildRuleTagUpdateCacheObject(RuleTagCacheObject originalObject, RuleTag configObject) {
    RuleTagCacheObject result = null;
    try {
      result = originalObject.clone();
      setCacheRuleTagCacheObjectFields(result, configObject);

    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    return result;
  }

  public RuleTagCacheObject buildRuleTagCacheObject(Long id, RuleTag configEquipment) {
    RuleTagCacheObject expectedObject = new RuleTagCacheObject(id);
    expectedObject = setCacheRuleTagCacheObjectFields(expectedObject, configEquipment);

    Set<Long> eqIds = new HashSet<>();
    eqIds.add(15L);
    expectedObject.setEquipmentIds(eqIds);
    Set<Long> procIds = new HashSet<>();
    procIds.add(5L);
    expectedObject.setProcessIds(procIds);
    expectedObject.setDataTagQuality(new DataTagQualityImpl());

    return expectedObject;
  }

  private RuleTagCacheObject setCacheRuleTagCacheObjectFields(RuleTagCacheObject cacheObject, RuleTag configObject) {

    if (configObject.getName() != null) {
      cacheObject.setName(configObject.getName());
    }
    if (configObject.getMode() != null) {
      cacheObject.setMode((short) configObject.getMode().ordinal());
    }
    if (configObject.getDataType() != null) {
      cacheObject.setDataType(configObject.getDataType());
    }
    if (configObject.getDescription() != null) {
      cacheObject.setDescription(configObject.getDescription());
    }
    if (configObject.getUnit() != null) {
      cacheObject.setUnit(configObject.getUnit());
    }
    if (configObject.getIsLogged() != null) {
      cacheObject.setLogged(configObject.getIsLogged());
    }
    if (configObject.getMetadata() != null) {
      Metadata metadata = new Metadata();
      metadata.setMetadata(configObject.getMetadata().getMetadata());
      cacheObject.setMetadata(metadata);
    }
    if (configObject.getRuleText() != null) {
      cacheObject.setRuleText(configObject.getRuleText());
    }
    return cacheObject;
  }

  public AlarmCacheObject buildAlarmUpdateCacheObject(AlarmCacheObject originalObject, Alarm configObject) {
    AlarmCacheObject result = null;
    try {
      result = (AlarmCacheObject) originalObject.clone();
      setCacheAlarmCacheObjectFields(result, configObject);

    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    return result;
  }

  public AlarmCacheObject buildAlarmCacheObject(Long id, Alarm configObject) {
    AlarmCacheObject expectedObject = new AlarmCacheObject(id);
    expectedObject = setCacheAlarmCacheObjectFields(expectedObject, configObject);

    return expectedObject;
  }

  private AlarmCacheObject setCacheAlarmCacheObjectFields(AlarmCacheObject cacheObject, Alarm configObject) {

    if (configObject.getDataTagId() != null) {
      cacheObject.setTagId(configObject.getDataTagId());
    }
    if (configObject.getFaultFamily() != null) {
      cacheObject.setFaultFamily(configObject.getFaultFamily());
    }
    if (configObject.getFaultMember() != null) {
      cacheObject.setFaultMember(configObject.getFaultMember());
    }
    if (configObject.getFaultCode() != null) {
      cacheObject.setFaultCode(configObject.getFaultCode());
    }
    if (configObject.getAlarmCondition() != null) {
      cacheObject.setCondition(AlarmCondition.fromConfigXML(configObject.getAlarmCondition().getXMLCondition()));
    }
    if (configObject.getMetadata() != null) {
      Metadata metadata = new Metadata();
      metadata.setMetadata(configObject.getMetadata().getMetadata());
      cacheObject.setMetadata(metadata);
    }
    return cacheObject;
  }

  public CommandTagCacheObject buildCommandTagUpdateCacheObject(CommandTagCacheObject originalObject, CommandTag configObject) {
    CommandTagCacheObject result = null;
    try {
      result = originalObject.clone();
      setCacheCommandTagCacheObjectFields(result, configObject);

    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    return result;
  }

  public CommandTagCacheObject buildCommandTagCacheObject(Long id, CommandTag configEquipment) {
    CommandTagCacheObject expectedObject = new CommandTagCacheObject(id);
    expectedObject = setCacheCommandTagCacheObjectFields(expectedObject, configEquipment);
    expectedObject.setProcessId(5L);
    expectedObject.setEquipmentId(15L);

    return expectedObject;
  }

  private CommandTagCacheObject setCacheCommandTagCacheObjectFields(CommandTagCacheObject cacheObject, CommandTag configObject) {

    if (configObject.getName() != null) {
      cacheObject.setName(configObject.getName());
    }
    if (configObject.getDataType() != null) {
      cacheObject.setDataType(configObject.getDataType());
    }
    if (configObject.getEquipmentId() != null) {
      cacheObject.setEquipmentId(configObject.getEquipmentId());
    }
    if (configObject.getClientTimeout() != null) {
      cacheObject.setClientTimeout(configObject.getClientTimeout());
    }
    if (configObject.getExecTimeout() != null) {
      cacheObject.setExecTimeout(configObject.getExecTimeout());
    }
    if (configObject.getSourceTimeout() != null) {
      cacheObject.setSourceTimeout(configObject.getSourceTimeout());
    }
    if (configObject.getSourceRetries() != null) {
      cacheObject.setSourceRetries(configObject.getSourceRetries());
    }
    if (configObject.getMetadata() != null) {
      Metadata metadata = new Metadata();
      metadata.setMetadata(configObject.getMetadata().getMetadata());
      cacheObject.setMetadata(metadata);
    }
    if (configObject.getMode() != null) {
      cacheObject.setMode((short) configObject.getMode().ordinal());
    }
    if (configObject.getHardwareAddress() != null) {
      cacheObject.setHardwareAddress(configObject.getHardwareAddress());
    }
    if (configObject.getMaxValue() != null) {
      cacheObject.setMaximum((Comparable) configObject.getMaxValue());
    }
    if (configObject.getMinValue() != null) {
      cacheObject.setMaximum((Comparable) configObject.getMinValue());
    }
    if (configObject.getDescription() != null) {
      cacheObject.setDescription(configObject.getDescription());
    }

    RbacAuthorizationDetails details = new RbacAuthorizationDetails();
    if (configObject.getRbacClass() != null) {
      details.setRbacClass(configObject.getRbacClass());
    } else {
      details.setRbacClass(cacheObject.getAuthorizationDetails().getRbacClass());
    }
    if (configObject.getRbacDevice() != null) {
      details.setRbacDevice(configObject.getRbacDevice());
    } else {
      details.setRbacDevice(cacheObject.getAuthorizationDetails().getRbacDevice());
    }
    if (configObject.getRbacProperty() != null) {
      details.setRbacProperty(configObject.getRbacProperty());
    } else {
      details.setRbacProperty(cacheObject.getAuthorizationDetails().getRbacProperty());
    }
    cacheObject.setAuthorizationDetails(details);

    return cacheObject;
  }
}
