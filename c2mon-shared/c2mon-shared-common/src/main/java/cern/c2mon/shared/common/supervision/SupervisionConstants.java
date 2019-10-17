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
package cern.c2mon.shared.common.supervision;

/**
 * Constants used by the DAQ/Equipment/SubEquipment
 * supervision mechanism.
 *
 * <p>Also used in the supervison DB log for
 * logging supervision changes.
 *
 * @author Mark Brightwell
 *
 */
public class SupervisionConstants {

  /**
   * The possible status changes of the supervised
   * entities. The status should only pertain to whether
   * the component is running correctly or not (not to
   * other status information such as reconfiguration
   * status).
   *
   * UNCERTAIN: indicates the server is not sure of the status of the DAQ/Equipment,
   *            for instance after a server downtime
   *
   * STOPPED: Deprecated in 2019/10/17 as there didn't seem to be any consumers
   *
   * @author Mark Brightwell
   *
   */
 public enum SupervisionStatus { RUNNING, DOWN, STARTUP, @Deprecated STOPPED, UNCERTAIN, RUNNING_LOCAL }

 /**
  * Entities that are supervised.
  *
  * @author Mark Brightwell
  *
  */
 public enum SupervisionEntity { PROCESS, EQUIPMENT, SUBEQUIPMENT };

}
