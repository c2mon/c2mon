/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.daq.common.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.SubEquipmentConfiguration;

/**
 * Helper class of {@link EquipmentMessageSender}.
 * <p/>
 * Takes care of sending the (Sub-)Equipemt state to the server. Note that it
 * will filter redundant state information as this should not be used as
 * heartbeat mechanism.
 * 
 * @author Matthias Braeger
 */
@AllArgsConstructor
final class EquipmentStateSender {
  
  /**
   * The equipment configuration of this sender.
   */
  private EquipmentConfiguration equipmentConfiguration;
  
  /**
   * The process message sender takes the messages actually send to the server.
   */
  private IProcessMessageSender processMessageSender;
  
  /**
   * Keeps track of the last COMM_FAULT state sent to the server 
   */
  private final Map<Long, CommFaultState> lastSentStates = new ConcurrentHashMap<>();
  
  
  /**
   * Sends a note to the business layer, to confirm that the equipment is not
   * properly configured, or connected to its data source
   */
  void confirmEquipmentStateIncorrect() {
    confirmEquipmentStateIncorrect(null);
  }

  /**
   * Sends a note to the business layer, to confirm that the equipment is not
   * properly configured, or connected to its data source
   *
   * @param description additional description
   */
  void confirmEquipmentStateIncorrect(final String description) {
    sendCommfaultTag(this.equipmentConfiguration.getCommFaultTagId(), equipmentConfiguration.getName(), this.equipmentConfiguration.getCommFaultTagValue(), description);

    // Send the commFaultTag for the equipment's subequipments too
    Map<Long, SubEquipmentConfiguration> subEquipmentConfigurations = equipmentConfiguration.getSubEquipmentConfigurations();

    for (SubEquipmentConfiguration subEquipmentConfiguration : subEquipmentConfigurations.values()) {
      sendCommfaultTag(subEquipmentConfiguration.getCommFaultTagId(), subEquipmentConfiguration.getName(), subEquipmentConfiguration.getCommFaultTagValue(), description);
    }
  }

  /**
   * Sends a note to the business layer, to confirm that the equipment is
   * properly configured, connected to its source and running
   */
  void confirmEquipmentStateOK() {
    confirmEquipmentStateOK(null);
  }

  /**
   * Sends a note to the business layer, to confirm that the equipment is
   * properly configured, connected to its source and running
   *
   * @param description additional description
   */
  void confirmEquipmentStateOK(final String description) {
    sendCommfaultTag(this.equipmentConfiguration.getCommFaultTagId(), equipmentConfiguration.getName(), !this.equipmentConfiguration.getCommFaultTagValue(), description);

    // Send the commFaultTag for the equipment's subequipments too
    Map<Long, SubEquipmentConfiguration> subEquipmentConfigurations = equipmentConfiguration.getSubEquipmentConfigurations();

    for (SubEquipmentConfiguration subEquipmentConfiguration : subEquipmentConfigurations.values()) {
      sendCommfaultTag(subEquipmentConfiguration.getCommFaultTagId(), subEquipmentConfiguration.getName(), !subEquipmentConfiguration.getCommFaultTagValue(), description);
    }
  }
  
  /**
   * Sends the CommfaultTag message.
   *
   * @param tagID       The CommfaultTag id.
   * @param state       The CommFaultTag value to send.
   * @param description The description of the CommfaultTag
   */
  private void sendCommfaultTag(final long tagID, final String equipmentName, final boolean state, final String description) {
    CommFaultState newState = new CommFaultState(tagID, equipmentName, state, description);
    sendCommfaultTag(newState);
  }
  
  private void sendCommfaultTag(CommFaultState newState) {
    if (isNewState(newState)) {
      String name = newState.equipmentName + ":COMM_FAULT";
      this.processMessageSender.sendCommfaultTag(newState.tagID, name, newState.state, newState.description);
      lastSentStates.put(newState.tagID, newState);
    }
  }
  
  /**
   * Compares to the previous state to determine, if the message is redundant or not
   * @param newState New commmFault state 
   * @return true, if it is a new state information that shall be send to the server
   */
  private boolean isNewState(CommFaultState newState) {
    if (lastSentStates.containsKey(newState.tagID)) {
      return !lastSentStates.get(newState.tagID).equals(newState);
    }
    return true;
  }
  
  /**
   * POJO to keep track of the last state sent to the server
   * 
   * @author Matthias Braeger
   */
  @AllArgsConstructor
  @EqualsAndHashCode
  private class CommFaultState {
    private long tagID;
    private String equipmentName;
    private boolean state;
    private String description;
  }
}
