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
package cern.c2mon.daq.common.impl;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;

public class EquipmentTimeDeadbandTester extends EquipmentTimeDeadband {

  public EquipmentTimeDeadbandTester(final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer, 
                                     final IProcessMessageSender processMessageSender,
                                     final EquipmentSenderFilterModule equipmentSenderFilterModule, 
                                     final EquipmentLoggerFactory equipmentLoggerFactory) {
    super(dynamicTimeDeadbandFilterer, processMessageSender, equipmentSenderFilterModule, equipmentLoggerFactory);
    // TODO Auto-generated constructor stub
  }

  @Override
  /**
   * 
   * @param tagScheduler The scheduler to start
   */
   protected void startSDTtimeDeadbandScheduler(final SDTTimeDeadbandScheduler tagScheduler) {
     // Don't do anything
   }

  
}
