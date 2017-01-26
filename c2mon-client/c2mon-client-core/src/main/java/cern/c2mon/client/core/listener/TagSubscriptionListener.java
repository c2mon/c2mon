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
package cern.c2mon.client.core.listener;

import java.util.Set;
import cern.c2mon.client.common.tag.Tag;

/**
 * Is used by {@link TagServiceImpl} to notify listeners about new tag subscriptions
 * or unsubscriptions.
 * 
 * @author Matthias Braeger
 * 
 */
public interface TagSubscriptionListener {

  /**
   * Is invoked whenever a new {@link Tag} is registered in the cache,
   * or when a listener is added to a {@link Tag} were no listener
   * has already been registered
   * @param tagIds
   *          A collection of {@link Tag} that now have listeners
   */
  void onNewTagSubscriptions(final Set<Long> tagIds);

  /**
   * Is invoked to inform listeners about tags in the cache which have no 
   * subscribed <code>DataTagUpdateListeners</code>.
   * 
   * @param tagIds
   *          A list of tag id's whose references in the cache have no update
   *          listeners subscribed.
   */
  void onUnsubscribe(final Set<Long> tagIds);

}
