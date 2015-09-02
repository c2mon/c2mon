/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.DataTagListener;
import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.cache.CacheSynchronizationException;
import cern.c2mon.client.core.cache.ClientDataTagCache;
import cern.c2mon.client.core.listener.TagSubscriptionListener;
import cern.c2mon.client.core.manager.CoreSupervisionManager;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.rule.RuleFormatException;
import lombok.extern.slf4j.Slf4j;

/**
 * The tag manager implements the <code>C2monTagManager</code> interface. It's main job is to delegate cache requests
 * and to update the cache with new registered tags. Therefore it has to request an initial update from the C2MON
 * server.
 * <p>
 * Please note that the <code>TagServiceImpl</code> is not in charge of registering the <code>ClientDataTags</code> to the
 * <code>JmsProxy</code> nor to the <code>SupervisionManager</code>. This is done directly by the cache.
 *
 * @author Matthias Braeger
 */
@Service("tagService") @Slf4j
public class TagServiceImpl implements AdvancedTagService {

  /**
   * The cache instance which is managing all <code>ClientDataTag</code> objects
   */
  private ClientDataTagCache cache;

  /** Reference to the supervision manager singleton */
  private final CoreSupervisionManager supervisionManager;

  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;

  /** List of subscribed data tag update listeners */
  private final Set<DataTagUpdateListener> tagUpdateListeners = new HashSet<>();

  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   *
   * @param supervisionManager Needed to compute the tag quality before passing the tag value to the caller
   * @param cache The cache instance which is managing all <code>ClientDataTag</code> objects
   * @param requestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected TagServiceImpl(final CoreSupervisionManager supervisionManager, 
                           final ClientDataTagCache cache,
                           final RequestHandler requestHandler) {

    this.supervisionManager = supervisionManager;
    this.cache = cache;
    this.clientRequestHandler = requestHandler;
  }

  @Override
  public Collection<ClientDataTagValue> getAllSubscribedTags(final DataTagUpdateListener listener) {
    Collection<ClientDataTag> cacheTagList = cache.getAllTagsForListener(listener);
    Collection<ClientDataTagValue> clonedDataTags = new ArrayList<ClientDataTagValue>(cacheTagList.size());

    for (ClientDataTag cdt : cacheTagList) {
      try {
        clonedDataTags.add(cdt.clone());
      } catch (CloneNotSupportedException e) {
        log.error("Unable to clone ClientDataTag with id " + cdt.getId(), e);
        throw new UnsupportedOperationException("Unable to clone ClientDataTag with id " + cdt.getId(), e);
      }
    }

    return clonedDataTags;
  }

  @Override
  public Set<Long> getAllSubscribedTagIds(final DataTagUpdateListener listener) {
    return cache.getAllTagIdsForListener(listener);
  }

  @Override
  public void refreshTags() {
    cache.refresh();
  }

  @Override
  public void refreshTags(final Collection<Long> tagIds) {
    cache.refresh(new HashSet<Long>(tagIds));
  }

  @Override
  public void subscribeTag(final Long dataTagId, final DataTagUpdateListener listener) throws CacheSynchronizationException {
    if (dataTagId == null) {
      String error = "Called with null parameter (id collection).";
      log.warn("subscribeDataTag() : " + error);
      throw new IllegalArgumentException(error);
    }

    Set<Long> id = new HashSet<>(1);
    id.add(dataTagId);
    subscribeTags(id, listener);
  }

  @Override
  public void subscribeTags(final Set<Long> tagIds, final DataTagUpdateListener listener) throws CacheSynchronizationException {
    doTagSubscription(tagIds, listener);
  }

  @Override
  public void subscribeTag(final Long dataTagId, final DataTagListener listener) throws CacheSynchronizationException {
    if (dataTagId == null) {
      String error = "Called with null parameter (id collection).";
      log.warn("subscribeDataTagUpdate() : " + error);
      throw new IllegalArgumentException(error);
    }

    Set<Long> id = new HashSet<>(1);
    id.add(dataTagId);
    subscribeTags(id, listener);
  }

  /**
   * Determine from its request all new tag id's which are not yet available in the cache. Those ones are then created
   * with a request to the C2MON server and afterwards registered for the listener.
   */
  @Override
  public void subscribeTags(final Set<Long> tagIds, final DataTagListener listener) {
    doTagSubscription(tagIds, listener);
  }

  /**
   * Inner method that handles the tag subscription.
   * @param tagIds List of tag ids
   * @param listener The listener to be added to the <code>ClientDataTag</code> references
   * @param sendInitialValuesToListener if set to <code>true</code>, the listener will receive the
   *                                    current value of the tag.
   * @return The initial values of the subscribed tags.
   */
  private synchronized <T extends DataTagUpdateListener> void doTagSubscription(final Set<Long> tagIds, final T listener) {
    if (tagIds == null) {
      String error = "Called with null parameter (id collection). Ignoring request.";
      log.warn("doTagSubscription() : " + error);
      throw new IllegalArgumentException(error);
    }

    if (listener == null) {
      String error = "Called with null parameter (DataTagUpdateListener). Ignoring request.";
      log.warn("doTagSubscription() : " + error);
      throw new IllegalArgumentException(error);
    }

    if (tagIds.isEmpty()) {
      String info = "Called with empty tag id list. Ignoring request.";
      log.info("doTagSubscription() : " + info);
      return;
    }

    if (log.isDebugEnabled()) {
      log.debug(String.format("doTagSubscription() : called for %d tags.", tagIds.size()));
    }

    try {
      // add listener to tags and subscribe them to the live topics
      cache.subscribe(tagIds, listener);
      // Add listener to set
      tagUpdateListeners.add(listener);
    }
    catch (CacheSynchronizationException cse) {
      // Rollback the subscription
      log.error("doTagSubscription() : Cache error occured while subscribing to data tags ==> Rolling back subscription.", cse);
      cache.unsubscribeDataTags(tagIds, listener);
      throw cse;
    }
  }
  
  /**
   * Inner method that handles the tag subscription.
   * @param regexList List of tag ids
   * @param listener The listener to be added to the <code>ClientDataTag</code> references
   * @param sendInitialValuesToListener if set to <code>true</code>, the listener will receive the
   *                                    current value of the tag.
   * @return The initial values of the subscribed tags.
   */
  private synchronized <T extends DataTagUpdateListener> void doTagSubscriptionByName(final Set<String> regexList, final T listener) {
    if (regexList == null) {
      String error = "Called with null parameter (regex list). Ignoring request.";
      log.warn("doTagSubscription() : " + error);
      throw new IllegalArgumentException(error);
    }

    if (listener == null) {
      String error = "Called with null parameter (DataTagUpdateListener). Ignoring request.";
      log.warn("doTagSubscription() : " + error);
      throw new IllegalArgumentException(error);
    }

    if (regexList.isEmpty()) {
      String info = "Called with empty regex list. Ignoring request.";
      log.info("doTagSubscription() : " + info);
      return;
    }

    if (log.isDebugEnabled()) {
      log.debug(String.format("doTagSubscription() : called for %d tags.", regexList.size()));
    }

    try {
      // add listener to tags and subscribe them to the live topics
      cache.subscribeByRegex(regexList, listener);
      // Add listener to set
      tagUpdateListeners.add(listener);
    }
    catch (CacheSynchronizationException cse) {
      // Rollback the subscription
      log.error("doTagSubscription() : Cache error occured while subscribing to data tags.", cse);
      throw cse;
    }
  }

  @Override
  public void unsubscribeTag(Long dataTagId, DataTagUpdateListener listener) {
    if (dataTagId == null) {
      String error = "Called with null parameter (id collection).";
      log.warn("unsubscribeDataTag() : " + error);
      throw new IllegalArgumentException(error);
    }

    Set<Long> id = new HashSet<>(1);
    id.add(dataTagId);
    unsubscribeTags(id, listener);
  }

  @Override
  public void unsubscribeAllTags(final DataTagUpdateListener listener) {
    cache.unsubscribeAllDataTags(listener);
    tagUpdateListeners.remove(listener);
  }

  @Override
  public void unsubscribeTags(final Set<Long> dataTagIds, final DataTagUpdateListener listener) {
    cache.unsubscribeDataTags(dataTagIds, listener);
    tagUpdateListeners.remove(listener);
  }

  

  @Override
  public void addTagSubscriptionListener(final TagSubscriptionListener listener) {
    cache.addTagSubscriptionListener(listener);
  }

  @Override
  public void removeTagSubscriptionListener(final TagSubscriptionListener listener) {
    cache.removeTagSubscriptionListener(listener);
  }



  @Override
  public Collection<ClientDataTagValue> getTags(final Collection<Long> tagIds) {
    Collection<ClientDataTagValue> resultList = new ArrayList<ClientDataTagValue>();
    Collection<Long> missingTags = new ArrayList<Long>();
    Map<Long, ClientDataTag> cachedValues = cache.get(new HashSet<Long>(tagIds));

    try {
      for (Entry<Long, ClientDataTag> cacheEntry : cachedValues.entrySet()) {
        if (cacheEntry.getValue() != null) {
          resultList.add(cacheEntry.getValue().clone());
        } else {
          missingTags.add(cacheEntry.getKey());
        }
      }
    } catch (CloneNotSupportedException e) {
      log.error("getDataTags() - Unable to clone ClientDataTag! Please check the code.", e);
      throw new UnsupportedOperationException("Unable to clone ClientDataTag! Please check the code.", e);
    }

    // If there are missing values fetch them from the server
    if (!missingTags.isEmpty()) {
      try {
        Collection<TagUpdate> tagUpdates = clientRequestHandler.requestTags(missingTags);
        for (TagUpdate tagUpdate : tagUpdates) {
          try {
            ClientDataTagImpl cdt = new ClientDataTagImpl(tagUpdate.getId());
            cdt.update(tagUpdate);
            
            // In case of a CommFault- or Status control tag, we don't register to supervision invalidations
            if (!tagUpdate.isControlTag() || tagUpdate.isAliveTag()) {
              supervisionManager.addSupervisionListener(cdt, cdt.getProcessIds(), cdt.getEquipmentIds(), cdt.getSubEquipmentIds());
            }
            
            resultList.add(cdt.clone());

            if (!tagUpdate.isControlTag() || tagUpdate.isAliveTag()) {
              supervisionManager.removeSupervisionListener(cdt);
            }
          } catch (RuleFormatException e) {
            log.error("getDataTags() - Received an incorrect rule tag from the server. Please check tag with id " + tagUpdate.getId(), e);
            throw new RuntimeException("Received an incorrect rule tag from the server for tag id " + tagUpdate.getId());
          }

        }
      } catch (JMSException e) {
        log.error("getDataTags() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
      }
    }

    return resultList;
  }

  @Override
  public ClientDataTagValue getTag(final Long tagId) {
    if (tagId == null) {
      throw new NullPointerException("The tagId parameter cannot be null");
    }

    Collection<Long> coll = new ArrayList<Long>(1);
    coll.add(tagId);
    Collection<ClientDataTagValue> resultColl = getTags(coll);
    for(ClientDataTagValue cdt : resultColl) {
      return cdt;
    }

    return new ClientDataTagImpl(tagId);
  }

  @Override
  public int getCacheSize() {

    return cache.getCacheSize();
  }

  @Override
  public boolean isSubscribed(DataTagUpdateListener listener) {
    return tagUpdateListeners.contains(listener);
  }

  /**
   * TODO: Call could be optimized by filtering out all strings without
   */
  @Override
  public void subscribeTagsByName(String regex, DataTagUpdateListener listener) throws CacheSynchronizationException {
    subscribeTagsByName(new HashSet<>(Arrays.asList(new String[]{regex})), listener);
  }

  @Override
  public void subscribeTagsByName(String regex, DataTagListener listener) throws CacheSynchronizationException {
    subscribeTagsByName(new HashSet<>(Arrays.asList(new String[]{regex})), listener);
  }

  @Override
  public void subscribeTagsByName(Set<String> regexList, DataTagUpdateListener listener) throws CacheSynchronizationException {
    doTagSubscriptionByName(regexList, listener);
  }

  @Override
  public void subscribeTagsByName(Set<String> regexList, DataTagListener listener) throws CacheSynchronizationException {
    doTagSubscriptionByName(regexList, listener);
  }

  private Collection<ClientDataTagValue> getDataTagsByName(final Collection<String> tagNames) {
    
    Collection<ClientDataTagValue> resultList = new ArrayList<>();
    Set<String> missingTags = new HashSet<>();
    Map<String, ClientDataTag> cachedValues = cache.getByNames(new HashSet<>(tagNames));

    try {
      for (Entry<String, ClientDataTag> cacheEntry : cachedValues.entrySet()) {
        if (cacheEntry.getValue() != null) {
          resultList.add(cacheEntry.getValue().clone());
        } else {
          missingTags.add(cacheEntry.getKey());
        }
      }
    } catch (CloneNotSupportedException e) {
      log.error("getDataTags() - Unable to clone ClientDataTag! Please check the code.", e);
      throw new UnsupportedOperationException("Unable to clone ClientDataTag! Please check the code.", e);
    }
    
    if (!missingTags.isEmpty()) {
      resultList.addAll(findTagsByName(missingTags));
    }
    
    return resultList;
  }

  @Override
  public Collection<ClientDataTagValue> findTagsByName(String regex) {
    if (hasWildcard(regex)) {
      Set<String> regexList = new HashSet<>();
      regexList.add(regex);
      return findTagsByName(regexList);
    }
    else {
      return getDataTagsByName(Arrays.asList(new String[]{regex}));
    }
  }

  @Override
  public Collection<ClientDataTagValue> findTagsByName(Set<String> regexList) {
    Collection<ClientDataTagValue> resultList = new ArrayList<ClientDataTagValue>();
    
    try {
      Collection<TagUpdate> tagUpdates = clientRequestHandler.requestTagsByRegex(regexList);
      for (TagUpdate tagUpdate : tagUpdates) {
        try {
          ClientDataTagImpl cdt = new ClientDataTagImpl(tagUpdate.getId());
          cdt.update(tagUpdate);
          
          // In case of a CommFault- or Status control tag, we don't register to supervision invalidations
          if (!tagUpdate.isControlTag() || tagUpdate.isAliveTag()) {
            supervisionManager.addSupervisionListener(cdt, cdt.getProcessIds(), cdt.getEquipmentIds(), cdt.getSubEquipmentIds());
          }
          
          resultList.add(cdt.clone());

          if (!tagUpdate.isControlTag() || tagUpdate.isAliveTag()) {
            supervisionManager.removeSupervisionListener(cdt);
          }
        } catch (RuleFormatException e) {
          log.error("getDataTags() - Received an incorrect rule tag from the server. Please check tag with id " + tagUpdate.getId(), e);
          throw new RuntimeException("Received an incorrect rule tag from the server for tag id " + tagUpdate.getId());
        }

      }
    } catch (JMSException e) {
      log.error("getDataTags() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }

    return resultList;
  }
  
  /**
   * Checks whether the string contains un-escaped * or ? characters
   * @param s string to scan
   * @return <code>true</code>, if the string has wildcards
   */
  private static final boolean hasWildcard(String s) {
    String test = s.replace("\\*", "").replace("\\?", "");
    return (test.contains("*") || test.contains("?"));
  }
}
