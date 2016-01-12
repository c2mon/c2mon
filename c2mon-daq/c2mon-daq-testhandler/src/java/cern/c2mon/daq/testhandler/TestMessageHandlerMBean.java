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
package cern.c2mon.daq.testhandler;

/**
 * This class defines the methods on the TestMessageHandler that will be exposed
 * via JMX.
 *
 * @author Justin Lewis Salmon
 */
public interface TestMessageHandlerMBean {

  /**
   * Causes the simulated Equipment to go down by preventing the sending of its
   * alive tag.
   */
  public void suppressEquipmentAliveTag();

  /**
   * Re-enables the sending of the alive tag for the Equipment.
   */
  public void activateEquipmentAliveTag();

  /**
   * Causes a simulated SubEquipment to go down by preventing the sending of its
   * alive tag.
   *
   * @param aliveTagId the ID of the SubEquipment alive tag
   */
  public void suppressSubEquipmentAliveTag(int aliveTagId);

  /**
   * Re-enables the sending of the alive tag for a SubEquipment.
   *
   * @param aliveTagId the ID of the SubEquipment alive tag
   */
  public void activateSubEquipmentAliveTag(int aliveTagId);

  /**
   * Sends the commfault tag for the equipment with the given value.
   *
   * @param value the commfault tag value (true = no problem, false = problem)
   */
  public void sendEquipmentCommFaultTag(boolean value);

  /**
   * Sends the commfault tag for a subequipment with the given value.
   *
   * @param commFaultTagId the ID of the SubEquipment commfault tag
   * @param value the commfault tag value (true = no problem, false = problem)
   */
  public void sendSubEquipmentCommFaultTag(int commFaultTagId, boolean value);
}
