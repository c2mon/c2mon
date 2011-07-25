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

import java.util.Collection;
import java.util.Set;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.manager.TagManager;

/**
 * This interface describes the methods which are provided manipulate
 * the references in the client data tag cache.
 * <p>
 * <b>Please note</b>, that only the {@link TagManager} should use this
 * interface face! If your class needs absolutely to access the cache
 * it should use the {@link BasicCacheHandler} instead. 
 *
 * @author Matthias Braeger
 * @see BasicCacheHandler
 */
public interface ClientDataTagCache extends BasicCacheHandler {
  /**
   * Creates a <code>ClientDataTag</code> object and adds it to the cache. If the cache
   * has already an entry for that tag it does not create a new one but returns
   * the existing <b>live tag</b> reference.
   * <p>
   * This method is used by the
   * {@link C2monTagManager#subscribeDataTags(Set, DataTagUpdateListener)}
   * method to create new tags in the cache.
   * This method is called before adding the {@link DataTagUpdateListener} references
   * to the <code>ClientDataTag</code>. 
   * <p>
   * Please note that the cache does not handle the subscription of the
   * <code>ClientDataTag</code> to the <code>JmsProxy</code> or <code>SupervisionManager</code>.
   * All this is done by the <code>C2monTagManager</code>. For this it is using the
   * <b>live tag</b> reference which is returned by this method call.
   * 
   * @param tagId The id of the <code>ClientDataTag</code> that shall be
   *                      added to the cache.
   * @return The reference to the live tag. Please not that if the cache is in history mode and you
   *         want to get that reference of the tag you need to call {@link #get(Long)}.
   * @see cern.c2mon.client.core.C2monTagManager#subscribeDataTags(Collection, DataTagUpdateListener)
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  ClientDataTag create(Long tagId);
  
  /**
   * Adds the given listener to the tags in the cache.
   * @param tagIds List of tag ids
   * @param listener The listener to be added to the <code>ClientDataTag</code> references
   * @return List containing the tag id's of all <code>ClientDataTag</code> objects in the
   *         cache which did not have already at least one listener registered.
   * @throws NullPointerException If one of the parameter is <code>null</code> or if one of 
   *                              the tags is not present in the cache 
   */
  Set<Long> addDataTagUpdateListener(Set<Long> tagIds, DataTagUpdateListener listener);
  
  /**
   * This method synchronizes subscribed data tags with the server.
   * It will ask the server to send the actual tag information for all subscribed data tags.
   * Once the cache is synchronized, all subscribed <code>DataTagUpdateListener</code> will
   * be notified.
   */
  void refresh();
  
  /**
   * This method synchronizes subscribed data tags with the server.
   * It will ask the server to send the actual tag information for all subscribed data tags.
   * Once the cache is synchronized, all subscribed <code>DataTagUpdateListener</code> will
   * be notified.
   * 
   * @param tagIds A set of data tag id's
   */
  void refresh(Set<Long> tagIds);
  
  /**
   * Unsubscribes the given listener from all cache objects. 
   * @param listener The listener which shall be unsubscribed.
   * @return List of id's from those tags which have no <code>DataTagUpdateListener</code>
   *         anymore subscribed.
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  Set<Long> unsubscribeAllDataTags(DataTagUpdateListener listener);
  
  /**
   * Unsubscribes the given listener from all tags specified by the
   * list of tag ids.
   * @param dataTagIds list of tag ids 
   * @param listener The listener which shall be unsubscribed.
   * @return List of id's from those tags which have no <code>DataTagUpdateListener</code>
   *         anymore subscribed.
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  Set<Long> unsubscribeDataTags(Set<Long> dataTagIds, DataTagUpdateListener listener);
}
