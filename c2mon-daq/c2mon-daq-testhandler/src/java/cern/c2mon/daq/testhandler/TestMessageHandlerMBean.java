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
package cern.c2mon.daq.testhandler;

/**
 * This class defines the methods on the TestMessageHandler that will be exposed
 * via JMX.
 *
 * @author Justin Lewis Salmon
 */
public interface TestMessageHandlerMBean {

  /**
   * Causes the simulated Process to go down by preventing the sending of its
   * alive tag.
   */
  public void suppressProcessAliveTag();

  /**
   * Re-enables the sending of the alive tag for the Process.
   */
  public void activateProcessAliveTag();

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
