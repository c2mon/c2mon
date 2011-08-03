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
package cern.c2mon.client.core;

import java.util.Collection;
import java.util.Set;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;

/**
 * This interface describes the methods which are provided by
 * the C2MON tag manager singleton. The tag manager allows
 * subscribing listeners to the <code>ClientDataTag</code>'s 
 * and to get informed when a new update has been sent.
 *
 * @author Matthias Braeger
 */
public interface C2monTagManager {

  /**
   * Use this method for registering a listener and to receive updates for specific data tags.
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before.
   * You will be informed about new updates via the <code>onUpdate(ClientDataTagValue)</code>
   * method.
   *  
   * @param dataTagIds A collection of data tag IDs
   * @param listener the listener which shall be registered
   * @return <code>true</code>, if the registration was successfull, otherwhise <code>false</code>
   */
  boolean subscribeDataTags(final Set<Long> dataTagIds, final DataTagUpdateListener listener);
  
  /**
   * Use this method for unregistering a listener from receiving updates for specific data tags.
   *  
   * @param dataTagIds A collection of data tag id's
   * @param listener the listener which shall be registered
   */
  void unsubscribeDataTags(final Set<Long> dataTagIds, final DataTagUpdateListener listener);
  
  
  /**
   * Use this method to unsubscribe from all previously registered data tags.
   * @param listener the listener which shall be registered
   */
  void unsubscribeAllDataTags(final DataTagUpdateListener listener);
  
  
  /**
   * Returns for a given listener a copy of all subscribed data tags with
   * their current state as <code>ClientDataTagValue</code> instances.
   * 
   * @param listener The listener for which we want to get the data tags
   *        subscriptions
   * @return A collection of <code>ClientDataTag</code> objects
   */
  Collection<ClientDataTagValue> getAllSubscribedDataTags(final DataTagUpdateListener listener);
  
  /**
   * Returns for a given listener a list of all subscribed data tags ids.
   * 
   * @param listener The listener for which we want to get the data tags
   *        subscriptions
   * @return A collection of tag ids
   */
  Set<Long> getAllSubscribedDataTagIds(final DataTagUpdateListener listener);
  
  /**
   * Returns for every valid id of the list a copy of the cached data tag.
   * If the value is not in the cache it will try to fetch it from the server.
   * However, in case of a connection error or an unknown tag id the corresponding
   * tag might be missing.
   * 
   * @param tagIds A collection of data tag id's
   * @return A collection of all <code>ClientDataTag</code> objects
   */
  Collection<ClientDataTagValue> getDataTags(Collection<Long> tagIds);
  
  /**
   * This method is used to synchronize subscribed data tags with the
   * server. It will ask the server to send the actual tag information for
   * all subscribed data tags. The C2MON client API will then send an update
   * to all subscribed listeners.
   */
  void refreshDataTags();
  
  /**
   * This method is used to synchronize a list subscribed data tags with the
   * server. It will ask the server to send the actual tag information for
   * all subscribed tags of the given list. The C2MON client API will then send
   * an update to all subscribed listeners.
   * 
   * @param tagIds A collection of data tag id's
   * @throws NullPointerException if the Collection is <code>null</code>.
   */
  void refreshDataTags(Collection<Long> tagIds);
}
