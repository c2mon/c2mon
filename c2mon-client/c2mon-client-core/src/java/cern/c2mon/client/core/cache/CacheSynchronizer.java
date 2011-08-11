/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
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
package cern.c2mon.client.core.cache;

import java.util.Set;

import cern.c2mon.client.common.listener.DataTagUpdateListener;

/**
 * The <code>CacheSynchronizer</code> interface is used by the {@link ClientDataTagCache}
 * implementation for creating, removing or refreshing the <code>ClientDataTag</code>
 * references in the cache.
 *
 * @author Matthias Braeger
 */
interface CacheSynchronizer {

  /**
   * Creates a <code>ClientDataTag</code> object and adds it to the cache.
   * If the cache has already an entry for that tag it does not create a
   * new one but returns the existing <b>live tag</b> reference.
   * <p>
   * This method is used by the
   * {@link ClientDataTagCache#addDataTagUpdateListener(Set, DataTagUpdateListener)}
   * method to create new tags in the cache.
   * This method is called before adding the {@link DataTagUpdateListener} references
   * to the <code>ClientDataTag</code>. 
   * <p>
   * Please note that this method handles also the subscription of the
   * <code>ClientDataTag</code> to the <code>JmsProxy</code> and
   * <code>SupervisionManager</code>.
   * 
   * @param tagIds The ids of the <code>ClientDataTag</code> objects that shall be
   *                      added to the cache.
   * @see ClientDataTagCache#addDataTagUpdateListener(Set, DataTagUpdateListener)
   */
  void createTags(final Set<Long> tagIds);
  
  
  /** 
   * Removes all <code>ClientDataTag</code> references
   * with the given id from the cache. At the same time it unsubscribes the
   * live tags from the <code>JmsProxy</code> where they were formerly
   * registered as <code>ServerUpdateListener</code> by the <code>TagManager</code>.
   * 
   * @param tagIds list of <code>ClientDataTag</code> id's
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  void removeTags(Set<Long> tagIds);

  
  /**
   * Synchronizes the live cache with the C2MON server
   * @param tagIds Set of tag id's that shall be refreshed. If the parameter
   *        is <code>null</code>, the entire cache is updated.
   */
  void refresh(Set<Long> tagIds);
}
