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
package cern.c2mon.client.core.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.cache.ClientDataTagCache;
import cern.c2mon.client.core.listener.DataTagUpdateListener;
import cern.c2mon.client.core.listener.TagSubscriptionListener;
import cern.c2mon.client.core.tag.ClientDataTag;
import cern.c2mon.client.core.tag.ClientDataTagValue;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.common.datatag.TagQualityStatus;
import cern.tim.shared.rule.RuleFormatException;

/**
 * The tag manager implements the <code>C2monTagManager</code> interface.
 * It's main job is to delegate cache requests and to update the cache
 * with new registered tags. Therefore it has to request an initial update
 * from the C2MON server.
 * <p>
 * Please note that the <code>TagManager</code> is not in charge of registering
 * the <code>ClientDataTags</code> to the <code>JmsProxy</code> nor to the
 * <code>SupervisionManager</code>. This is done directly by the cache. 
 * @author Matthias Braeger
 */
@Service
public class TagManager implements CoreTagManager {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(TagManager.class);
  
  /** The cache instance which is managing all <code>ClientDataTag</code> objects */
  private ClientDataTagCache cache;
  
  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;
  
  /** Lock for accessing the <code>listeners</code> variable */
  private ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();

  /** List of subscribed listeners */
  private final Set<TagSubscriptionListener> tagSubscriptionListeners = new HashSet<TagSubscriptionListener>();

  
  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   * @param pCache The cache instance which is managing all <code>ClientDataTag</code> objects
   * @param pRequestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected TagManager(final ClientDataTagCache pCache, 
                       final RequestHandler pRequestHandler) {
    this.cache = pCache;
    this.clientRequestHandler = pRequestHandler;
  }
  
  @Override
  public Collection<ClientDataTagValue> getAllSubscribedDataTags(final DataTagUpdateListener listener) {
    Collection<ClientDataTag> cacheTagList = cache.getAllTagsForListener(listener);
    Collection<ClientDataTagValue> clonedDataTags = new ArrayList<ClientDataTagValue>(cacheTagList.size());
    
    for (ClientDataTag cdt : cacheTagList) {
      try {
        clonedDataTags.add(cdt.clone());
      }
      catch (CloneNotSupportedException e) {
        throw new UnsupportedOperationException("Unable to clone ClientDataTag with id " + cdt.getId(), e);
      }
    }
    
    
    return clonedDataTags;
  }

  @Override
  public void refreshDataTags() {
    cache.refresh();
  }

  /**
   * Determine from its request all new tag id's which are not yet available in the cache.
   * Those ones are then created with a request to the C2MON server and afterwards registered
   * for the listener.  
   */
  @Override
  public synchronized boolean subscribeDataTags(final Set<Long> tagIds, final DataTagUpdateListener listener) {
    if (tagIds == null) {
      LOG.warn("subscribeDataTags() : called with null parameter (id collection). Ignoring request.");
      return false;
    }

    if (listener == null) {
      LOG.warn("subscribeDataTags() : called with null parameter (DataTagUpdateListener). Ignoring request.");
      return false;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(new StringBuffer("subscribeDataTags() : called for ").append(tagIds.size()).append(" tags."));
    }
      
    // Find tag id's which are not yet in the cache
    Map<Long, ClientDataTag> newTags = new HashMap<Long, ClientDataTag>();
    
    synchronized (cache.getHistoryModeSyncLock()) {
      // Find out which tags need to be initialized
      ClientDataTag newTag = null;
      for (Long tagId : tagIds) { 
        if (!cache.containsTag(tagId)) {
          newTag = cache.create(tagId);
          newTag.getDataTagQuality().setInvalidStatus(TagQualityStatus.UNDEFINED_TAG);
          newTags.put(tagId, newTag);
        }
      }
      
      try {
        initializeNewTags(newTags);
        // add listener to tags
        this.cache.addDataTagUpdateListener(tagIds, listener);
        // Inform listeners (e.g. HistoryManager) about new subscriptions
        fireOnNewTagSubscriptionsEvent(newTags.keySet());
        synchronizeNewTags(newTags);
      }
      catch (JMSException e) {
        LOG.warn("initializeNewTags() - JMS connection lost -> Invalidate all newly requested tags.");
        for (ClientDataTag cdt : newTags.values()) {
          cdt.getDataTagQuality().addInvalidStatus(TagQualityStatus.JMS_CONNECTION_DOWN, "JMS connection lost.");
        }
        return false;
      }
    }
    
    return true;
  }
  
  /**
   * Requests from the C2MON server the initial values for all tags of the passed map. 
   * @param newTags Map of uninitialized tags from the cache
   * @throws JMSException In case of a JMS problem
   */
  private void initializeNewTags(final Map<Long, ClientDataTag> newTags) throws JMSException {
    if (!newTags.isEmpty()) {
      ClientDataTag newTag = null;
      // Request initial tag information from C2MON server
      Collection<TagUpdate> requestedTags = clientRequestHandler.requestTags(newTags.keySet());
      // Update the new tags and put them into the cache
      for (TagUpdate tagUpdate : requestedTags) {
        try { 
          newTag = newTags.get(tagUpdate.getId());
          newTag.update(tagUpdate);
        }
        catch (RuleFormatException e) {
          LOG.fatal("Received an incorrect rule tag from the server. Please check tag with id " + tagUpdate.getId(), e);
          throw new RuntimeException(e);
        }
      }
    }
  }
  
  /**
   * Update a second time in case an update was send before the ClientDataTag
   * was subscribed to the topic.
   * @param newTags Map of uninitialized tags from the cache
   * @throws JMSException In case of a JMS problem
   */
  private void synchronizeNewTags(final Map<Long, ClientDataTag> newTags) throws JMSException {
    if (!newTags.isEmpty()) {
      ClientDataTag newTag = null;
      Collection<TagValueUpdate> requestedTagValues = clientRequestHandler.requestTagValues(newTags.keySet());
      for (TagValueUpdate tagValueUpdate : requestedTagValues) {
        newTag = newTags.get(tagValueUpdate.getId());
        if (newTag.getServerTimestamp() == null || newTag.getServerTimestamp().before(tagValueUpdate.getServerTimestamp())) {
          try {
            newTag.update(tagValueUpdate);
          }
          catch (RuleFormatException e) {
            LOG.fatal("Received an incorrect rule tag from the server. Please check tag with id " + tagValueUpdate.getId(), e);
            throw new RuntimeException(e);
          }
        }
      }
    }
  }

  @Override
  public void unsubscribeAllDataTags(final DataTagUpdateListener listener) {
    Set<Long> unsubscribedTagIds = cache.unsubscribeAllDataTags(listener);
    fireOnUnsubscribeEvent(unsubscribedTagIds);
  }

  @Override
  public void unsubscribeDataTags(final Set<Long> dataTagIds, final DataTagUpdateListener listener) {
    Set<Long> unsubscribedTagIds = cache.unsubscribeDataTags(dataTagIds, listener);
    fireOnUnsubscribeEvent(unsubscribedTagIds);
  }
  
  @Override
  public void addTagSubscriptionListener(final TagSubscriptionListener listener) {
    listenersLock.writeLock().lock();
    try {
      tagSubscriptionListeners.add(listener);
    }
    finally {
      listenersLock.writeLock().unlock();
    }
  }

  @Override
  public void removeTagSubscriptionListener(final TagSubscriptionListener listener) {
    listenersLock.writeLock().lock();
    try {
      tagSubscriptionListeners.remove(listener);
    }
    finally {
      listenersLock.writeLock().unlock();
    }
  }
  
  /**
   * Fires an <code>onNewTagSubscriptions()</code> event to all registered
   * <code>TagSubscriptionListener</code> listeners.
   * @param tagIds list of new subscribed tags
   */
  private void fireOnNewTagSubscriptionsEvent(final Set<Long> tagIds) {
    if (!tagIds.isEmpty()) {
      listenersLock.readLock().lock();
      try {
        Set<Long> copyList = new HashSet<Long>(tagIds);
        for (TagSubscriptionListener listener : tagSubscriptionListeners) {
          listener.onNewTagSubscriptions(copyList);
        }
      }
      finally {
        listenersLock.readLock().unlock();
      }
    }
  }
  
  /**
   * Fires an <code>onUnsubscribe()</code> event to all registered
   * <code>TagSubscriptionListener</code> listeners.
   * @param tagIds list of tags that have been removed from the cache
   */
  private void fireOnUnsubscribeEvent(final Set<Long> tagIds) {
    listenersLock.readLock().lock();
    try {
      Set<Long> copyList = new HashSet<Long>(tagIds);
      for (TagSubscriptionListener listener : tagSubscriptionListeners) {
        listener.onUnsubscribe(copyList);
      }
    }
    finally {
      listenersLock.readLock().unlock();
    }
  }
}
