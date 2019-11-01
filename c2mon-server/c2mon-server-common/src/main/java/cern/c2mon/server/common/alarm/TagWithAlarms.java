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
package cern.c2mon.server.common.alarm;

import cern.c2mon.server.common.tag.Tag;
import lombok.Data;

import java.util.Collection;

/**
 * Groups a Tag with the Alarms associated with it.
 * The Alarms should be evaluated for the set Tag
 * value.
 *
 * <p>Foreseen usage is for both the Tag and Alarms
 * to be clones of the cache objects, frozen with
 * the corresponding values (for sending to the clients
 * for example)
 *
 * @author Mark Brightwell, Alexander Papageorgiou
 */
@Data
public class TagWithAlarms<T extends Tag> {

  /**
   * Get the Tag.
   * @return the tag itself (usually not in the cache)
   */
  private final T tag;

  /**
   * Get the associated Alarms.
   * @return a collection of alarms evaluated for this Tag
   */
  private final Collection<Alarm> alarms;
}
