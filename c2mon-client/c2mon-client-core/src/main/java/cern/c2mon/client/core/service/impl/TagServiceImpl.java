/*******************************************************************************
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
 ******************************************************************************/
package cern.c2mon.client.core.service.impl;

import java.util.*;
import java.util.Map.Entry;

import javax.jms.JMSException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.cache.CacheSynchronizationException;
import cern.c2mon.client.core.cache.ClientDataTagCache;
import cern.c2mon.client.core.elasticsearch.ElasticsearchService;
import cern.c2mon.client.core.jms.RequestHandler;
import cern.c2mon.client.core.listener.TagSubscriptionListener;
import cern.c2mon.client.core.service.CoreSupervisionService;
import cern.c2mon.client.core.tag.TagController;
import cern.c2mon.client.core.tag.TagImpl;
import cern.c2mon.client.core.service.AdvancedTagService;
import cern.c2mon.client.core.jms.RequestHandler;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.rule.RuleFormatException;

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
@Service
@Slf4j
public class TagServiceImpl implements AdvancedTagService {

  /**
   * The cache instance which is managing all <code>Tag</code> objects
   */
  private ClientDataTagCache cache;

  /** Reference to the supervision manager singleton */
  private final CoreSupervisionService supervisionService;

  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;

  /** List of subscribed data tag update listeners */
  private final Set<BaseTagListener> tagUpdateListeners = new HashSet<>();

  private final ElasticsearchService elasticsearchService;

  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   *
   * @param supervisionService Needed to compute the tag quality before passing the tag value to the caller
   * @param cache The cache instance which is managing all <code>Tag</code> objects
   * @param requestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected TagServiceImpl(final CoreSupervisionService supervisionService,
                           final ClientDataTagCache cache,
                           final @Qualifier("coreRequestHandler") RequestHandler requestHandler,
                           final ElasticsearchService elasticsearchService) {

    this.supervisionService = supervisionService;
    this.cache = cache;
    this.clientRequestHandler = requestHandler;
    this.elasticsearchService = elasticsearchService;
  }

  @Override
  public Collection<Tag> getSubscriptions(final BaseTagListener listener) {
    Collection<Tag> cacheTagList = cache.getAllTagsForListener(listener);
    Collection<Tag> clonedDataTags = new ArrayList<>(cacheTagList.size());

    for (Tag cdt : cacheTagList) {
      clonedDataTags.add(((TagImpl) cdt).clone());
    }

    return clonedDataTags;
  }

  @Override
  public Set<Long> getSubscriptionIds(final BaseTagListener listener) {
    return cache.getAllTagIdsForListener(listener);
  }

  @Override
  public void refresh() {
    cache.refresh();
  }

  @Override
  public void refresh(final Collection<Long> tagIds) {
    cache.refresh(new HashSet<Long>(tagIds));
  }

  @Override
  public void subscribe(final Long dataTagId, final BaseTagListener listener) throws CacheSynchronizationException {
    if (dataTagId == null) {
      String error = "Called with null parameter (id collection).";
      log.warn("subscribe() : " + error);
      throw new IllegalArgumentException(error);
    }

    Set<Long> id = new HashSet<>(1);
    id.add(dataTagId);
    subscribe(id, listener);
  }

  @Override
  public void subscribe(final Set<Long> tagIds, final BaseTagListener listener) throws CacheSynchronizationException {
    doSubscription(tagIds, listener);
  }

  @Override
  public void subscribe(final Long dataTagId, final TagListener listener) throws CacheSynchronizationException {
    if (dataTagId == null) {
      String error = "Called with null parameter (id collection).";
      log.warn("subscribe() : " + error);
      throw new IllegalArgumentException(error);
    }

    Set<Long> id = new HashSet<>(1);
    id.add(dataTagId);
    subscribe(id, listener);
  }

  /**
   * Determine from its request all new tag id's which are not yet available in the cache. Those ones are then created
   * with a request to the C2MON server and afterwards registered for the listener.
   */
  @Override
  public void subscribe(final Set<Long> tagIds, final TagListener listener) {
    doSubscription(tagIds, listener);
  }

  /**
   * Inner method that handles the tag subscription.
   * @param tagIds List of tag ids
   * @param listener The listener to be added to the <code>Tag</code> references
   */
  public synchronized <T extends BaseTagListener> void doSubscription(final Set<Long> tagIds, final T listener) {
    if (tagIds == null) {
      String error = "Called with null parameter (id collection). Ignoring request.";
      log.warn("doSubscription() : " + error);
      throw new IllegalArgumentException(error);
    }

    if (listener == null) {
      String error = "Called with null parameter (BaseTagListener). Ignoring request.";
      log.warn("doSubscription() : " + error);
      throw new IllegalArgumentException(error);
    }

    if (tagIds.isEmpty()) {
      String info = "Called with empty tag id list. Ignoring request.";
      log.info("doSubscription() : " + info);
      return;
    }

    if (log.isDebugEnabled()) {
      log.debug(String.format("doSubscription() : called for %d tags.", tagIds.size()));
    }

    try {
      // add listener to tags and subscribe them to the live topics
      cache.subscribe(tagIds, listener);
      // Add listener to set
      tagUpdateListeners.add(listener);
    }
    catch (CacheSynchronizationException cse) {
      // Rollback the subscription
      log.error("doSubscription() : Cache error occured while subscribing to data tags ==> Rolling back subscription.", cse);
      cache.unsubscribeDataTags(tagIds, listener);
      throw cse;
    }
  }

  /**
   * Inner method that handles the tag subscription.
   * @param regexList List of tag ids
   * @param listener The listener to be added to the <code>Tag</code> references
   */
  private synchronized <T extends BaseTagListener> void doSubscriptionByName(final Set<String> regexList, final T listener) {
    if (regexList == null) {
      String error = "Called with null parameter (regex list). Ignoring request.";
      log.warn("doSubscriptionByName() : " + error);
      throw new IllegalArgumentException(error);
    }

    if (listener == null) {
      String error = "Called with null parameter (BaseTagListener). Ignoring request.";
      log.warn("doSubscriptionByName() : " + error);
      throw new IllegalArgumentException(error);
    }

    if (regexList.isEmpty()) {
      String info = "Called with empty regex list. Ignoring request.";
      log.info("doSubscriptionByName() : " + info);
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
      log.error("doSubscriptionByName() : Cache error occured while subscribing to data tags.", cse);
      throw cse;
    }
  }

  @Override
  public void unsubscribe(Long dataTagId, BaseTagListener listener) {
    if (dataTagId == null) {
      String error = "Called with null parameter (id collection).";
      log.warn("unsubscribe() : " + error);
      throw new IllegalArgumentException(error);
    }

    Set<Long> id = new HashSet<>(1);
    id.add(dataTagId);
    unsubscribe(id, listener);
  }

  @Deprecated
  public void unsubscribeAllDataTags(final BaseTagListener listener) {
    cache.unsubscribeAllDataTags(listener);
    tagUpdateListeners.remove(listener);
  }

  @Override
  public void unsubscribe(final BaseTagListener listener) {
    cache.unsubscribeAllDataTags(listener);
    tagUpdateListeners.remove(listener);
  }

  @Deprecated
  public void unsubscribeDataTags(final Set<Long> dataTagIds, final BaseTagListener listener) {
    cache.unsubscribeDataTags(dataTagIds, listener);
    tagUpdateListeners.remove(listener);
  }

  @Override
  public void unsubscribe(final Set<Long> dataTagIds, final BaseTagListener listener) {
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
  public Collection<Tag> get(final Collection<Long> tagIds) {
    Collection<Tag> resultList = new ArrayList<>();
    Collection<Long> missingTags = new ArrayList<>();
    Map<Long, Tag> cachedValues = cache.get(new HashSet<>(tagIds));

    for (Entry<Long, Tag> cacheEntry : cachedValues.entrySet()) {
      if (cacheEntry.getValue() != null) {
        resultList.add(((TagImpl) cacheEntry.getValue()).clone());
      } else {
        missingTags.add(cacheEntry.getKey());
      }
    }

    // If there are missing values fetch them from the server
    if (!missingTags.isEmpty()) {
      try {
        Collection<TagUpdate> tagUpdates = clientRequestHandler.requestTags(missingTags);
        for (TagUpdate tagUpdate : tagUpdates) {
          try {
            TagController tagController = new TagController(tagUpdate.getId());
            TagImpl tagImpl = tagController.getTagImpl();

            tagController.update(tagUpdate);

            // In case of a CommFault- or Status control tag, we don't register to supervision invalidations
            if (!tagUpdate.isControlTag() || tagUpdate.isAliveTag()) {
              supervisionService.addSupervisionListener(tagController, tagImpl.getProcessIds(), tagImpl.getEquipmentIds(), tagImpl.getSubEquipmentIds());
            }

            missingTags.remove(tagImpl.getId());
            resultList.add(tagImpl.clone());

            if (!tagUpdate.isControlTag() || tagUpdate.isAliveTag()) {
              supervisionService.removeSupervisionListener(tagController);
            }
          } catch (RuleFormatException e) {
            log.error("get() - Received an incorrect rule tag from the server. Please check tag with id " + tagUpdate.getId(), e);
            throw new RuntimeException("Received an incorrect rule tag from the server for tag id " + tagUpdate.getId());
          }

        }
      } catch (JMSException e) {
        log.error("get() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
      }

      for (Long tagId : missingTags) {
        resultList.add(new TagImpl(tagId, true));
      }
    }

    return resultList;
  }

  @Override
  public Tag get(final Long tagId) {
    if (tagId == null) {
      throw new NullPointerException("The tagId parameter cannot be null");
    }

    Collection<Long> coll = new ArrayList<Long>(1);
    coll.add(tagId);
    Collection<Tag> resultColl = get(coll);
    for(Tag cdt : resultColl) {
      return cdt;
    }

    return new TagImpl(tagId, true);
  }

  @Override
  public int getCacheSize() {

    return cache.getCacheSize();
  }

  @Override
  public boolean isSubscribed(BaseTagListener listener) {
    return tagUpdateListeners.contains(listener);
  }

  /**
   * TODO: Call could be optimized by filtering out all strings without
   */
  @Override
  public void subscribeByName(String regex, BaseTagListener listener) throws CacheSynchronizationException {
    subscribeByName(new HashSet<>(Arrays.asList(new String[]{regex})), listener);
  }

  @Override
  public void subscribeByName(String regex, TagListener listener) throws CacheSynchronizationException {
    subscribeByName(new HashSet<>(Arrays.asList(new String[]{regex})), listener);
  }

  @Override
  public void subscribeByName(Set<String> regexList, BaseTagListener listener) throws CacheSynchronizationException {
    doSubscriptionByName(regexList, listener);
  }

  @Override
  public void subscribeByName(Set<String> regexList, TagListener listener) throws CacheSynchronizationException {
    doSubscriptionByName(regexList, listener);
  }

  private Collection<Tag> getByName(final Collection<String> tagNames) {

    Collection<Tag> resultList = new ArrayList<>();
    Set<String> missingTags = new HashSet<>();
    Map<String, Tag> cachedValues = cache.getByNames(new HashSet<>(tagNames));


    for (Entry<String, Tag> cacheEntry : cachedValues.entrySet()) {
      if (cacheEntry.getValue() != null) {
        resultList.add(((TagImpl) cacheEntry.getValue()).clone());
      } else {
        missingTags.add(cacheEntry.getKey());
      }
    }

    if (!missingTags.isEmpty()) {
      resultList.addAll(findByName(missingTags));
    }

    return resultList;
  }

  @Override
  public Collection<Tag> findByName(String regex) {
    if (hasWildcard(regex)) {
      Set<String> regexList = new HashSet<>();
      regexList.add(regex);
      return findByName(regexList);
    } else {
      return getByName(Arrays.asList(new String[]{regex}));
    }
  }

  @Override
  public Collection<Tag> findByName(Set<String> regexList) {
    Collection<Tag> resultList = new ArrayList<Tag>();

    try {
      Collection<TagUpdate> tagUpdates = clientRequestHandler.requestTagsByRegex(regexList);
      for (TagUpdate tagUpdate : tagUpdates) {
        try {
          TagController tagController = new TagController(tagUpdate.getId());
          TagImpl tagImpl = tagController.getTagImpl();

          tagController.update(tagUpdate);

          // In case of a CommFault- or Status control tag, we don't register to supervision invalidations
          if (!tagUpdate.isControlTag() || tagUpdate.isAliveTag()) {
            supervisionService.addSupervisionListener(tagController, tagImpl.getProcessIds(), tagImpl.getEquipmentIds(), tagImpl.getSubEquipmentIds());
          }

          resultList.add(tagImpl.clone());

          if (!tagUpdate.isControlTag() || tagUpdate.isAliveTag()) {
            supervisionService.removeSupervisionListener(tagController);
          }
        } catch (RuleFormatException e) {
          log.error("findByName() - Received an incorrect rule tag from the server. Please check tag with id " + tagUpdate.getId(), e);
          throw new RuntimeException("Received an incorrect rule tag from the server for tag id " + tagUpdate.getId());
        }

      }
    } catch (JMSException e) {
      log.error("findByName() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }

    return resultList;
  }

  @Override
  public Collection<Tag> findByMetadata(String key, String value) {
    return get(elasticsearchService.findTagsByMetadata(key, value));
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
