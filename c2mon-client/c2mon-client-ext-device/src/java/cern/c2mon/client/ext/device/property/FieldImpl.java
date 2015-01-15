/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
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
package cern.c2mon.client.ext.device.property;

import cern.c2mon.client.common.tag.ClientDataTagValue;

/**
 * Implementation of the {@link Field} interface.
 *
 * @author Justin Lewis Salmon
 */
public class FieldImpl extends BasePropertyImpl implements Field {

  /**
   * Constructor for a field whose internal {@link ClientDataTagValue} will be
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
   * Constructor for a field whose internal {@link ClientDataTagValue} does not
   * need to be lazily loaded.
   *
   * @param name the name of the field
   * @param category the field category
   * @param clientDataTag the internal data tag of the field
   */
  public FieldImpl(String name, Category category, ClientDataTagValue clientDataTag) {
    super(name, category, clientDataTag);
  }
}
