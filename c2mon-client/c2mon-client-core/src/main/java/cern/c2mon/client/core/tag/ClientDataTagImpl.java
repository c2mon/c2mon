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
package cern.c2mon.client.core.tag;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import cern.c2mon.client.common.listener.BaseListener;
import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * A client representation of the DataTag object.
 * <code>Tag</code> objects are created by the
 * <code>TagFactory</code>. The object connects to its update topic
 * and receives tag updates from the TIM server.
 * When the Tag value or quality changes it notifies its registered
 * <code>DataTagUpdateListeners</code>
 *
 * @author Matthias Braeger
 * @see TagFactory
 * @see DataTagUpdateListener
 */
@Root(name = "Tag")
@Slf4j
public class ClientDataTagImpl {

  @Getter
  private CloneableTagBean cloneableTagBean;
  /**
   * Default description when the object is not yet initialized
   */
  private static final String DEFAULT_DESCRIPTION = "Tag not initialised.";


  @SuppressWarnings("unchecked")
  @Override
  public TagBean clone() {
//    getCloneableTagBean().getUpdateTagLock().readLock().lock();
//    try {
//      ClientDataTagImpl clone = (ClientDataTagImpl) super.clone();
//
//      clone.updateTagLock = new ReentrantReadWriteLock();
//
//      // clone the process id map
//      clone.cloneableTagBean.setProcessSupervisionStatus(new HashMap<>(cloneableTagBean.getProcessSupervisionStatus().size()));
//      for (Entry<Long, SupervisionEvent> entry : cloneableTagBean.getProcessSupervisionStatus().entrySet()) {
//        if (entry.getValue() != null) {
//          clone.cloneableTagBean.getProcessSupervisionStatus().put(entry.getKey(), entry.getValue().clone());
//        }
//        else {
//          clone.cloneableTagBean.getProcessSupervisionStatus().put(entry.getKey(), null);
//        }
//      }
//
//      // clone the equipment id map
//      clone.cloneableTagBean.setEquipmentSupervisionStatus(new HashMap<>(cloneableTagBean.getEquipmentSupervisionStatus().size()));
//      for (Entry<Long, SupervisionEvent> entry : cloneableTagBean.getEquipmentSupervisionStatus().entrySet()) {
//        if (entry.getValue() != null) {
//          clone.cloneableTagBean.getEquipmentSupervisionStatus().put(entry.getKey(), entry.getValue().clone());
//        }
//        else {
//          clone.cloneableTagBean.getEquipmentSupervisionStatus().put(entry.getKey(), null);
//        }
//      }
//
//      // clone the sub equipment id map
//      clone.cloneableTagBean.setSubEquipmentSupervisionStatus(new HashMap<>(cloneableTagBean.getSubEquipmentSupervisionStatus().size()));
//      for (Entry<Long, SupervisionEvent> entry : cloneableTagBean.getSubEquipmentSupervisionStatus().entrySet()) {
//        if (entry.getValue() != null) {
//          clone.cloneableTagBean.getSubEquipmentSupervisionStatus().put(entry.getKey(), entry.getValue().clone());
//        }
//        else {
//          clone.cloneableTagBean.getSubEquipmentSupervisionStatus().put(entry.getKey(), null);
//        }
//      }
//
//      // clone the metadata map - alternative:
//      //clone.metadata = (Map<String, Object>) ((HashMap)this.metadata).clone();
//      //clone.metadata.putAll(metadata);
//      clone.metadata = new HashMap<>();
//      for (Entry<String, Object> entry : metadata.entrySet()) {
//        clone.metadata.put(deepClone(entry.getKey()), deepClone(entry.getValue()));
//      }
//
//      // AlarmsValue objects are immutable
//      clone.cloneableTagBean.setAlarms(new ArrayList<>());
//      for (AlarmValue alarm : cloneableTagBean.getAlarms()) {
//        clone.cloneableTagBean.getAlarms().add(alarm.clone());
//      }
//
//      if (cloneableTagBean.getDataTagQuality() != null) {
//        clone.cloneableTagBean.setTagQuality(cloneableTagBean.getDataTagQuality().clone());
//      }
//      if (cloneableTagBean.getTimestamp() != null) {
//        clone.cloneableTagBean.setSourceTimestamp((Timestamp) cloneableTagBean.getTimestamp().clone());
//      }
//      if (cloneableTagBean.getDaqTimestamp() != null) {
//        clone.cloneableTagBean.setDaqTimestamp((Timestamp) cloneableTagBean.getDaqTimestamp().clone());
//      }
//      if (cloneableTagBean.getServerTimestamp() != null) {
//        clone.cloneableTagBean.setServerTimestamp((Timestamp) cloneableTagBean.getServerTimestamp().clone());
//      }
//      if (cloneableTagBean.getRuleExpression() != null) {
//        clone.cloneableTagBean.setRuleExpression((RuleExpression) cloneableTagBean.getRuleExpression().clone());
//      }
//      clone.listeners = new ConcurrentIdentitySet<>();
//
//      return clone;
//    }
//    catch (CloneNotSupportedException cloneException) {
//      log.error(
//              "clone() - Cloning the ClientDataTagImpl object failed! No update sent to the client.");
//      throw new RuntimeException(cloneException);
//    } finally {
//      updateTagLock.readLock().unlock();
//    }

    return cloneableTagBean.getTagBean().clone();
  }
}
