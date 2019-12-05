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
package cern.c2mon.server.configuration.handler;

import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.configuration.handler.impl.TagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;

/**
 * Bean managing configuration updates to C2MON Control tags.
 *
 * @author Mark Brightwell
 */
public interface ControlTagConfigHandler extends TagConfigHandler<ControlTag>, BaseConfigHandler<ProcessChange> {

  /**
   * Given a ControlTag id, returns a create event for sending
   * to the DAQ layer if necessary. Returns null if no event needs
   * sending to the DAQ layer for this particular ControlTag.
   *
   * @param configId     the id of the configuration
   * @param controlTagId the id of the ControlTag that needs creating on the DAQ layer
   * @param equipmentId  the id of the Equipment this control tag is attached to (compulsory)
   * @param processId    the id of the Process to reconfigure
   * @return the change event including the process id
   */
  ProcessChange getCreateEvent(Long configId, Long controlTagId, Long equipmentId, Long processId);

}
