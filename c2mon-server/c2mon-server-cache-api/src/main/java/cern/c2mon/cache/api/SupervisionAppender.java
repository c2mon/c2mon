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
package cern.c2mon.cache.api;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.supervision.SupervisionEvent;

/**
 * Helper bean for adding the current supervision status 
 * of Processes and Equipments to Tags.
 * 
 * @author Mark Brightwell
 *
 */
public interface SupervisionAppender {

  /**
   * Adds the current Process and Equipment
   * Supervision status to the quality of this tag.
   * 
   * <p>Notice NO timestamps of the Tag are modified by this method
   * and will be identical to the previous value.
   * 
   * @param tag for which to add the Supervision info
   * @param <T> the type of the Tag
   */
  <T extends Tag>void addSupervisionQuality(T tagCopy, SupervisionEvent event);
}
