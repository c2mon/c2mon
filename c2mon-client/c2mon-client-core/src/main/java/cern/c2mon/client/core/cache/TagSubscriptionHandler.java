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
package cern.c2mon.client.core.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.BaseListener;
import cern.c2mon.client.common.listener.DataTagListener;
import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.listener.TagSubscriptionListener;
import cern.c2mon.client.core.tag.TagController;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for {@link ClientDataTagCacheImpl} to handle all tag (un-)subscription requests.
 * In case a tag is not yet in the client cache, this class will fetch it from the server
 * and all the topic subscription will handled.
 *
 * @author Matthias Braeger
 */
@Service
@Slf4j
class TagSubscriptionHandler {

  /** The cache controller manages the cache references */
  private final CacheController controller;

  /** The cache Synchronizer */
  private final CacheSynchronizer cacheSynchronizer;

  /** Lock for accessing the <code>listeners</code> variable */
  private final static ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();

  /** List of subscribed listeners */
  private final Set<TagSubscriptionListener> tagSubscriptionListeners = new HashSet<TagSubscriptionListener>();


  @Autowired
  TagSubscriptionHandler(final CacheController cacheController,
                         final CacheSynchronizer cacheSynchronizer) {
    this.controller = cacheController;
    this.cacheSynchronizer = cacheSynchronizer;
  }

  /**
   * Subscribes the given listener to the list of tags. In case a tag is not yet in the
   * client cache, it is going to be fetched from the server and all the topic subscription will handled.
   * @param tagIds list of tags ids to which the listener shall be subscribed
   * @param listener the listener to subscribe.
   * @param sendInitialUpdateSeperately {@code true}, if the {@link BaseListener} is in fact a
   *        {@link DataTagListener} which allows sending the initial updates on a separate method.
   * @throws CacheSynchronizationException In case of errors during the subscription.
   */
  void subscribe(final Set<Long> tagIds, final BaseListener listener, final boolean sendInitialUpdateSeperately) throws CacheSynchronizationException {
    // Creates the uninitialised tags
    Set<Long> newTagIds = cacheSynchronizer.initTags(tagIds);

    handleTagSubscription(tagIds, newTagIds, listener, sendInitialUpdateSeperately);
  }

  /**
   * Subscribes the given listener to the list of tags matching at least one of the regular expressions.
   * In case a tag is not yet in the client cache, it is fetched from the server. Also all the topic
   * subscription will handled.
   * @param regexList list of regular expressions
   * @param listener the listener to subscribe.
   * @param sendInitialUpdateSeperately {@code true}, if the {@link BaseListener} is in fact a
   *        {@link DataTagListener} which allows sending the initial updates on a separate method.
   * @throws CacheSynchronizationException In case of errors during the subscription.
   */
  void subscribeByRegex(final Set<String> regexList, final BaseListener listener, final boolean sendInitialUpdateSeperately) throws CacheSynchronizationException {
    // list of all matching tags, filled during createMissingTags
    final Set<Long> allMatchingTags = new HashSet<Long>();

    // Create the uninitialized tags
    Set<Long> newTagIds = cacheSynchronizer.initTags(regexList, allMatchingTags);

    handleTagSubscription(allMatchingTags, newTagIds, listener, sendInitialUpdateSeperately);
  }

  /**
   * Handles the listener subscription to the tags. Furthermore it triggers the topic subscription for new tag points
   * @param subscriptionList list of tag ids to which the listner shall be subscribed to
   * @param newTagIds Newly created tags during the subscription process
   * @param listener The tag listener to subscribe
   * @param sendInitialUpdateSeperately {@code true}, if the {@link BaseListener} is in fact a
   *        {@link DataTagListener} which allows sending the initial updates on a separate method.
   */
  private void handleTagSubscription(Set<Long> subscriptionList, Set<Long> newTagIds, final BaseListener listener, boolean sendInitialUpdateSeperately) {
    // Needed if, the initial values shall be sent on the separate #onInitialUpdate() method
    final Map<Long, Tag> initialUpdates = new HashMap<>(subscriptionList.size());

    TagController cdt = null;
    for (Long tagId : subscriptionList) {
      cdt = controller.getActiveCache().get(tagId);
      if (sendInitialUpdateSeperately) {
        initialUpdates.put(tagId, cdt.getTagImpl().clone());
      }
    } // end for


    // Before subscribing to the update topics we send the initial values,
    // if the listener is of type DataTagListener
    if (sendInitialUpdateSeperately && listener instanceof DataTagListener) {
      if (log.isDebugEnabled()) {
        log.debug("handleTagSubscription() - Sending initial values to DataTagListener");
      }

      Collection<Tag> oldFormat = new ArrayList<>(initialUpdates.size());
      oldFormat.addAll(initialUpdates.values());
      ((DataTagListener) listener).onInitialUpdate(oldFormat);
    }
    else if (sendInitialUpdateSeperately && listener instanceof TagListener) {
      if (log.isDebugEnabled()) {
        log.debug("handleTagSubscription() - Sending initial values to DataTagListener");
      }

      Collection<Tag> values = initialUpdates.values();
      ((TagListener) listener).onInitialUpdate(values);
    }

    // Add the listener to all tags
    for (Long tagId : subscriptionList) {
      cdt = controller.getActiveCache().get(tagId);
      cdt.addUpdateListener(listener, initialUpdates.get(tagId));
    }

    if (!newTagIds.isEmpty()) {
      // Asynchronously subscribe to the topics and get the latest values again
      cacheSynchronizer.subscribeTags(newTagIds);

      // Inform listeners (e.g. HistoryManager) about new subscriptions
      fireOnNewTagSubscriptionsEvent(newTagIds);
    }

  }

  void unsubscribeAllTags(final BaseListener listener) {
    Set<Long> tagsToRemove = new HashSet<Long>();
    controller.getWriteLock().lock();
    try {
      for (TagController cdt : controller.getActiveCache().values()) {
        if (cdt.isUpdateListenerRegistered(listener)) {
          cdt.removeUpdateListener(listener);
          if (!cdt.hasUpdateListeners()) {
            tagsToRemove.add(cdt.getTagImpl().getId());
          }
        }
      }

      // Remove from cache
      cacheSynchronizer.removeTags(tagsToRemove);
    } finally {
      controller.getWriteLock().unlock();
    }

    fireOnUnsubscribeEvent(tagsToRemove);
  }

  void unsubscribeTags(final Set<Long> dataTagIds, final BaseListener listener) {
    Set<Long> tagsToRemove = new HashSet<>();
    controller.getWriteLock().lock();
    try {
      TagController cdt = null;
      for (Long tagId : dataTagIds) {
        cdt = controller.getActiveCache().get(tagId);
        if (cdt != null) {
          cdt.removeUpdateListener(listener);
          if (!cdt.hasUpdateListeners()) {
            tagsToRemove.add(tagId);
          }
        }
      }

      // Remove from cache
      cacheSynchronizer.removeTags(tagsToRemove);
    } finally {
      controller.getWriteLock().unlock();
    }

    fireOnUnsubscribeEvent(tagsToRemove);
  }

  /**
   * Fires an <code>onNewTagSubscriptions()</code> event to all registered <code>TagSubscriptionListener</code>
   * listeners.
   *
   * @param tagIds list of new subscribed tags
   */
  private void fireOnNewTagSubscriptionsEvent(final Set<Long> tagIds) {
    if (!tagIds.isEmpty()) {
      listenersLock.readLock().lock();
      try {
        Set<Long> copyList = new HashSet<>(tagIds);
        for (TagSubscriptionListener listener : tagSubscriptionListeners) {
          listener.onNewTagSubscriptions(copyList);
        }
      } finally {
        listenersLock.readLock().unlock();
      }
    }
  }

  void addSubscriptionListener(final TagSubscriptionListener listener) {
    listenersLock.writeLock().lock();
    try {
      tagSubscriptionListeners.add(listener);
    } finally {
      listenersLock.writeLock().unlock();
    }
  }

  void removeSubscriptionListener(final TagSubscriptionListener listener) {
    listenersLock.writeLock().lock();
    try {
      tagSubscriptionListeners.remove(listener);
    } finally {
      listenersLock.writeLock().unlock();
    }
  }

  /**
   * Fires an <code>onUnsubscribe()</code> event to all registered <code>TagSubscriptionListener</code> listeners.
   *
   * @param tagIds list of tags that have been removed from the cache
   */
  private void fireOnUnsubscribeEvent(final Set<Long> tagIds) {
    listenersLock.readLock().lock();
    try {
      Set<Long> copyList = new HashSet<Long>(tagIds);
      for (TagSubscriptionListener listener : tagSubscriptionListeners) {
        listener.onUnsubscribe(copyList);
      }
    } finally {
      listenersLock.readLock().unlock();
    }
  }
}
