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
package cern.c2mon.client.core.device.property;

import cern.c2mon.client.common.tag.Tag;

/**
 * Implementation of the {@link Field} interface.
 *
 * @author Justin Lewis Salmon
 */
public class FieldImpl extends BasePropertyImpl implements Field {

  /**
   * Constructor for a field whose internal {@link Tag} will be
   * lazily loaded in the future.
   *
   * @param name the name of the field
   * @param category the field category
   * @param tagId the id of the data tag corresponding to this field
   */
  public FieldImpl(String name, Category category, Long tagId) {
    super(name, category, tagId);
  }

  /**
   * Constructor for a field whose internal {@link Tag} does not
   * need to be lazily loaded.
   *
   * @param name the name of the field
   * @param category the field category
   * @param clientDataTag the internal data tag of the field
   */
  public FieldImpl(String name, Category category, Tag clientDataTag) {
    super(name, category, clientDataTag);
  }
}
