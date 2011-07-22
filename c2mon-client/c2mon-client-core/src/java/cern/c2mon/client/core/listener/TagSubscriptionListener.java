/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
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
 *****************************************************************************/
package cern.c2mon.client.core.listener;

import java.util.Set;

import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.core.manager.TagManager;

/**
 * Is used by {@link TagManager} to notify listeners about new tag subscriptions
 * or unsubscriptions.
 * 
 * @author Matthias Braeger
 * 
 */
public interface TagSubscriptionListener {

  /**
   * Is invoked whenever a new {@link ClientDataTag} is registered in the cache, 
   * or when a listener is added to a {@link ClientDataTag} were no listener
   * has already been registered
   * @param tagIds
   *          A collection of {@link ClientDataTag} that now have listeners
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
