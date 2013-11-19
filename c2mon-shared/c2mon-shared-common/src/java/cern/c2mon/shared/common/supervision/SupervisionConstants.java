/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
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
   * @author Mark Brightwell
   *
   */
 public enum SupervisionStatus { RUNNING, DOWN, STARTUP, STOPPED, UNCERTAIN };
  
 /**
  * Entities that are supervised.
  * 
  * @author Mark Brightwell
  *
  */
 public enum SupervisionEntity { PROCESS, EQUIPMENT, SUBEQUIPMENT };
  
}
