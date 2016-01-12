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
package cern.c2mon.client.ext.history.playback;

import java.util.Collection;

import cern.c2mon.client.common.listener.TagUpdateListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.ext.history.common.HistoryPlayer;
import cern.c2mon.client.ext.history.common.HistoryPlayerEvents;
import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.Timespan;
import cern.c2mon.client.jms.SupervisionListener;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;

/**
 * This interface describes the methods to be used by the c2mon-core which are
 * provided by the history player.
 * 
 * @author vdeila
 * 
 */
public interface HistoryPlayerCoreAccess extends HistoryPlayer, HistoryPlayerEvents {

  /**
   * Configures the player with a data source and a time period
   * 
   * @param provider
   *          the provider to use to get history data
   * @param timespan
   *          the time frame to ask for
   */
  void configure(HistoryProvider provider, Timespan timespan);

  /**
   * Activates the history player
   */
  void activateHistoryPlayer();

  /**
   * Stops the history player, stops all buffering and clears all loaded data.
   */
  void deactivateHistoryPlayer();

  /**
   * Call this method to register update listener for a tag
   * 
   * @param tagUpdateListener
   *          The listener to add
   * @param tagId
   *          The tag id the listener will be registered on
   * @param currentRealtimeValue
   *          The current real time value of the tag, is used for filtering. Can
   *          be <code>null</code>, but is not recommended for performance
   * 
   * @see #beginLoading()
   */
  void registerTagUpdateListener(TagUpdateListener tagUpdateListener, Long tagId, Tag currentRealtimeValue);

  /**
   * Registers the listener to the given set of supervision events
   * 
   * @param entity
   *          the entity
   * @param supervisionListener
   *          the listener to register
   * @param ids
   *          the ids of the entity
   * 
   * @see #beginLoading()
   */
  void registerSupervisionListener(SupervisionEntity entity, SupervisionListener supervisionListener, Collection<Long> ids);

  /**
   * Unregisters the listener from all tags it is registered to.
   * 
   * @param tagUpdateListener
   *          The listener to unregister
   */
  void unregisterTagUpdateListener(TagUpdateListener tagUpdateListener);
  
  /**
   * Unregisters all listeners which listens on this tag.
   * 
   * @param tagIds
   *          the tag ids to unregister
   */
  void unregisterTags(Collection<Long> tagIds);

  /**
   * Unregisters the listener from all supervision events it is registered to.
   * 
   * @param entity
   *          The entity to remove the listener from
   * @param listener
   *          The listener to unregister
   */
  void unregisterSupervisionListener(SupervisionEntity entity, SupervisionListener listener);

  /**
   * Call this method after registration of any listeners. Preferably called
   * after registering a bunch of listeners, and not in a loop.
   * 
   * @see #registerTagUpdateListener(TagUpdateListener, Long, TagValueUpdate)
   * @see #registerSupervisionListener(SupervisionEntity, SupervisionListener,
   *      Collection)
   */
  void beginLoading();

  /**
   * 
   * @return <code>true</code> if history player is active, <code>false</code>
   *         otherwise
   */
  boolean isHistoryPlayerActive();

  /**
   * Stops all loading. This will put the history player in an undefined state.
   * The next call to the history player should be
   * {@link #deactivateHistoryPlayer()}. Then the history player is ready to use
   * again.
   */
  void stopLoading();
  
}
