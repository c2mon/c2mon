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
package cern.c2mon.web.restapi.cache;

import cern.c2mon.client.common.tag.ClientDataTagValue;

/**
 * The {@link ClientDataTagCache} is used to store the latest values of data
 * tags in order for them to be pulled RESTfully.
 *
 * @author Justin Lewis Salmon
 */
public interface ClientDataTagCache {

  /**
   * Add a tag to the cache.
   *
   * @param tag the tag to add
   */
  public void add(ClientDataTagValue tag);

  /**
   * Retrieve a tag from the cache.
   *
   * @param id the ID of the tag to retrieve
   * @return the requested tag, or null if it wasn't in the cache
   */
  public ClientDataTagValue get(Long id);

  /**
   * Remove a tag from the cache.
   *
   * @param id the ID of the tag to be removed.
   */
  public void remove(Long id);

  /**
   * Check if the cache contains a tag.
   *
   * @param id the ID of the tag to be checked
   * @return true if the cache contains the tag, false otherwise
   */
  public boolean contains(Long id);
}
