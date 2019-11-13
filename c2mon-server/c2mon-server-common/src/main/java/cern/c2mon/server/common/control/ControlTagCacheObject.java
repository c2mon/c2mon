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
package cern.c2mon.server.common.control;

import cern.c2mon.server.common.tag.AbstractInfoTagCacheObject;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ControlTagCacheObject extends AbstractInfoTagCacheObject implements ControlTag {

  private static final long serialVersionUID = -410086626397719930L;

  /**
   * Used to construct a fake cache object, which is returned when a key cannot
   * be located in the cache.
   *
   * @param id
   * @param name
   * @param datatype
   * @param mode
   */
  public ControlTagCacheObject(Long id, String name, String datatype, short mode) {
    super(id, name, datatype, mode);
  }

  public ControlTagCacheObject(Long id) {
    super(id);
  }
}
