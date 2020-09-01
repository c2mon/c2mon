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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.jms.ConnectionListener;
import cern.c2mon.client.core.jms.JmsProxy;
import cern.c2mon.client.core.jms.RequestHandler;
import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.client.core.service.CoreSupervisionService;
import cern.c2mon.client.core.service.impl.SupervisionServiceImpl;
import cern.c2mon.client.core.tag.TagController;
import cern.c2mon.client.core.tag.TagImpl;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;
import cern.c2mon.shared.rule.RuleFormatException;

/**
 * This class implements the <code>CacheSynchronizer</code> interface and
 * handles the cache synchonization between the <code>CacheController</code> and
 * the C2MON server after a JMS connection or heartbeat loss.
 *
 * @author Matthias Braeger
 */
@Service
public class CacheSynchronizerImpl implements CacheSynchronizer, HeartbeatListener, ConnectionListener {

  /** Log4j Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(CacheSynchronizerImpl.class);

  /**
   * Synchronization lock object to avoid several thread refreshing at the same
   * time the live cache.
   */
  private final Object refreshLiveCacheSyncLock = new Object();

  /** Default message for a JMS connection lost exception */
  private static final String JMS_CONNECTION_LOST_MSG = "JMS connection lost.";

  /**
   * Is set to <code>true</code>, if the live cache has been invalidated because
   * of a JMS exception.
   */
  private boolean jmsConnectionDown = true;

  /**
   * Is set to <code>true</code>, if the live cache has been invalidated because
   * of a heartbeat expiration.
   */
  private boolean heartbeatExpired = true;

  /** The cache controller manages the cache references */
  private final CacheController controller;

  /** Reference to the jmsProxy singleton */
  private final JmsProxy jmsProxy;

  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler tagRequestHandler;

  /** Reference to the supervision manager singleton */
  private final CoreSupervisionService supervisionManager;

  /**
   * <code>Map</code> reference containing all subscribed data tags which are
   * updated via the <code>JmsProxy</code>
   */
  private Map<Long, TagController> liveCache = null;

  /**
   * <code>Map</code> reference containing all subscribed data tags which are
   * updated via the <code>HistoryManager</code>
   */
  private Map<Long, TagController> historyCache = null;

  /** Reference to the cache read lock */
  private ReadLock cacheReadLock = null;

  /**
   * Default Constructor used by Spring to wire in the references to the other
   * services.
   *
   * @param pJmsProxy Reference to the {@link JmsProxy} singleton
   * @param pRequestHandler Provides methods for requesting tag information from
   *          the C2MON server
   * @param pSupervisionManager Needed to register new tags as supervision event
   *          listener
   * @param pCacheController Provides acces to the different cache instances and
   *          to the thread locks.
   */
  @Autowired
  public CacheSynchronizerImpl(JmsProxy pJmsProxy,
                               @Qualifier("coreRequestHandler") RequestHandler pRequestHandler,
                               CoreSupervisionService pSupervisionManager,
                               CacheController pCacheController) {
    this.jmsProxy = pJmsProxy;
    this.tagRequestHandler = pRequestHandler;
    this.supervisionManager = pSupervisionManager;
    this.controller = pCacheController;
  }

  @PostConstruct
  protected void init() {
    cacheReadLock = controller.getReadLock();
    historyCache = controller.getHistoryCache();
    liveCache = controller.getLiveCache();

    supervisionManager.addConnectionListener(this);
    supervisionManager.addHeartbeatListener(this);
  }

  @Override
  public void refresh(final Set<Long> tagIds) throws CacheSynchronizationException {
    supervisionManager.refreshSupervisionStatus();
    synchronized (refreshLiveCacheSyncLock) {
      synchronizeCache(tagIds);
    }
  }

  @Override
  public Set<Long> initTags(final Set<Long> tagIds) throws CacheSynchronizationException {
    Set<Long> newTags = new HashSet<>();

    if (tagIds.size() > 0) {
      TagController cdt = null;
      for (Long tagId : tagIds) {
        if (!liveCache.containsKey(tagId)) {
          cdt = new TagController(tagId, true);
          liveCache.put(cdt.getTagImpl().getId(), cdt);
          newTags.add(tagId);
        }
      }
    }

    if (!newTags.isEmpty()) {
      // will fetch the initial tag information from the server
      synchronized (refreshLiveCacheSyncLock) {
        try {
          synchronizeTags(newTags);
        }
        catch (JMSException e) {
          throw new CacheSynchronizationException(e);
        }
      }

      // Checks, if there are tags which needs to be synchronized with
      // the history cache.
      if (controller.isHistoryModeEnabled()) {
        synchronizeHistoryCache(newTags);
      }
    }

    return newTags;
  }

  @Override
  public Set<Long> initTags(final Set<String> regexList, final Set<Long> allMatchingTags) throws CacheSynchronizationException {
    final Set<Long> newTags = new HashSet<>();

    try {
      Collection<TagUpdate> tagUpdates = tagRequestHandler.requestTagsByRegex(regexList);

      for (TagUpdate tagUpdate : tagUpdates) {

        try {

          controller.getWriteLock().lock();
          try {
            if (!liveCache.containsKey(tagUpdate.getId())) {
              TagController cdt = new TagController(tagUpdate.getId());

              cdt.update(tagUpdate);
              subscribeToSupervisionManager(cdt);
              liveCache.put(cdt.getTagImpl().getId(), cdt);

              newTags.add(cdt.getTagImpl().getId());
            }
          } finally {
            controller.getWriteLock().unlock();
          }

          allMatchingTags.add(tagUpdate.getId());

        } catch (RuleFormatException e) {
          LOG.error("Received an incorrect rule tag from the server. Please check tag with id " + tagUpdate.getId(), e);
          throw new RuntimeException("Received an incorrect rule tag from the server for tag id " + tagUpdate.getId());
        }

      }
    } catch (JMSException e) {
      LOG.error("JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }

    if (!newTags.isEmpty()) {
      // Checks, if we need to synchonize the
      if (controller.isHistoryModeEnabled()) {
        synchronizeHistoryCache(newTags);
      }
    }

    return newTags;
  }

  /**
   * Inner method to which removes all <code>Tag</code> references
   * with the given id from the cache. At the same time it unsubscribes the live
   * tags from the <code>JmsProxy</code> where they were formerly registered as
   * <code>ServerUpdateListener</code> by the <code>TagServiceImpl</code>.
   *
   * @param tagIds list of <code>Tag</code> id's
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  @Override
  public void removeTags(final Set<Long> tagIds) {
    if (tagIds.size() > 0) {
      LOG.info("removeTags() - Removing " + tagIds.size() + " tags from the cache.");
      for (Long tagId : tagIds) {
        if (controller.isHistoryModeEnabled()) {
          historyCache.remove(tagId);
        }
        TagController liveTag = liveCache.remove(tagId);
        if (liveTag.getTagImpl().getDataTagQuality().isExistingTag()) {
          try {
            jmsProxy.unregisterUpdateListener(liveTag);
          } catch (Exception e) {
            LOG.warn("removeTags() - Could not unregister tag " + tagId + " from JmsProxy. Reason: " + e.getMessage());
          }
        }
        supervisionManager.removeSupervisionListener(liveTag);
      }

      if (liveCache.isEmpty()) {
        LOG.info("removeTags() - Cache is now empty.");
      }
      else {
        LOG.info(String.format("removeTags() - Cache contains still %d tags", liveCache.size()));
      }
    }
  }

  /**
   * Inner method for invalidating all tags from the live cache with a given
   * <code>TagQualityStatus</code> flag.
   *
   * @param status The invalidation status to add
   * @param invalidationMessage The invalidation message.
   */
  private void invalidateLiveCache(final TagQualityStatus status, final String invalidationMessage) {
    LOG.debug("invalidateLiveCache() - Invalidating " + liveCache.size() + " tag entries with " + status + ".");
    for (TagController cdt : liveCache.values()) {
      cdt.invalidate(status, invalidationMessage);
    }
  }

  /**
   * Inner method for removing an invalid status from all tags from the live
   * cache
   *
   * @param statusToRemove The invalidation status to remove
   */
  private void removeLiveCacheInvalidation(final TagQualityStatus statusToRemove) {
    LOG.debug("removeLiveCacheInvalidation() - removing " + statusToRemove + " from " + liveCache.size() + " tag entries.");
    for (TagController cdt : liveCache.values()) {
      cdt.validate(statusToRemove);
    }
  }

  /**
   * Inner method which synchronizes the live cache with the C2MON server
   *
   * @param pTagIds Set of tag id's that shall be refreshed. If the parameter is
   *          <code>null</code>, the entire cache is updated.
   * @throws CacheSynchronizationException In case a problem during the cache
   *           synchronization with the C2MON server.
   */
  private void synchronizeCache(final Set<Long> pTagIds) throws CacheSynchronizationException {
    try {
      if (!liveCache.isEmpty()) {
        final Set<Long> unsynchronizedTagIds;

        if (pTagIds == null || jmsConnectionDown || heartbeatExpired) {
          // Refresh entire cache
          unsynchronizedTagIds = new HashSet<>(liveCache.keySet());
        } else {
          unsynchronizedTagIds = new HashSet<>(pTagIds);
        }

        unsynchronizedTagIds.removeAll(synchronizeTags(unsynchronizedTagIds));

        // Set all tags to unknown which were not returned by the C2MON server
        // Please note that we do not touch at this point the history cache.
        for (Long tagId : unsynchronizedTagIds) {
          final TagController liveTag = liveCache.get(tagId);
          if (liveTag.getTagImpl().getDataTagQuality().isExistingTag()) {
            if (jmsProxy.isRegisteredListener(liveTag)) {
              try {
                jmsProxy.unregisterUpdateListener(liveTag);
              } catch (Exception e) {
                LOG.warn("synchronizeCache() - Could not unregister tag " + tagId + " from JmsProxy. Reason: " + e.getMessage());
              }
            }
            supervisionManager.removeSupervisionListener(liveTag);
            final TagController unkownTag = new TagController(tagId, true);
            unkownTag.addUpdateListeners(liveTag.getUpdateListeners());
            liveCache.put(tagId, unkownTag);
          }
        }
      }

      // Reset JMS and Heartbeat problem flags
      jmsConnectionDown = false;
      heartbeatExpired = false;

    } catch (Exception e) {
      throw new CacheSynchronizationException("Could not refresh tags in the live cache.", e);
    }
  }

  /**
   * Gets the list of tags out of the live cache and synchronizes them again with
   * the server.
   * @param tagId the list of tags to be synchronized
   * @return the tags that could actually be synchronized.
   */
  private Set<Long> synchronizeTags(Set<Long> tagIds) throws JMSException {
    Set<Long> tagsKnownByServer = new HashSet<>();

    LOG.info("synchronizeTags() - Synchronizing " + tagIds.size() + " live cache entries with the server.");

    // Get and update the initial tags
    final Collection<TagUpdate> tagUpdates = tagRequestHandler.requestTags(tagIds);
    for (TagUpdate tagUpdate : tagUpdates) {
      try {
        TagController liveTag = liveCache.get(tagUpdate.getId());
        boolean wasUnknown = !liveTag.getTagImpl().getDataTagQuality().isExistingTag();

        liveTag.update(tagUpdate);

        if (wasUnknown) {
          subscribeToSupervisionManager(liveTag);
        }

        tagsKnownByServer.add(tagUpdate.getId());
      } catch (RuleFormatException e) {
        LOG.error("synchronizeCache() - Received an incorrect rule tag from the server. Please check tag with id " + tagUpdate.getId(), e);
        throw new RuntimeException("Received an incorrect rule tag from the server for tag id " + tagUpdate.getId());
      }
    }

    return tagsKnownByServer;
  }

  /**
   * Subscribes to the tag value update topic and requests the values once
   * again, in a separate thread.
   *
   * @param tagIds the set of tag IDs to be subscribed to
   */
  @Override
  public void subscribeTags(Set<Long> tagIds) {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.execute(new AsyncTagSubscriptionTask(tagIds));
  }

  /**
   * This inner class is responsible for asynchronously subscribing to the JMS
   * proxy and synchronising the values for a set of tags.
   *
   * @author Justin Lewis Salmon
   */
  class AsyncTagSubscriptionTask implements Runnable {

    /**
     * The set of tag IDs to be subscribed to.
     */
    final Set<Long> tagIds;

    /**
     * Constructor.
     *
     * @param tagIds set of tag IDs to be subscribed to.
     */
    public AsyncTagSubscriptionTask(Set<Long> tagIds) {
      this.tagIds = tagIds;
    }

    @Override
    public void run() {
      LOG.info("Subscribing to and synchronizing values for " + tagIds.size() + " tags");

      // Keep a reference to all newly registered tags whose values have to be
      // synchronised with a second server call.
      Set<Long> newKnownTags = new HashSet<>();
      try {
        LOG.info("Subscribing to tag value update topic");

        for (Long tagId : tagIds) {
          TagController liveTag = liveCache.get(tagId);

          // It's possible that really fast clients can unsubscribe from a tag
          // before getting to this point (see TIMS-979). In this case, the tag
          // will have been removed from the cache and hence be null. So we
          // simply don't do anything with it.
          if (liveTag == null) {
            continue;
          }

          // Subscribe to the value update topic of the tag
          if (handleLiveTagRegistration(liveTag)) {
            newKnownTags.add(tagId);
          }
        }

        // Perform once again a tag request in order to assure that no
        // update has been missed whilst subscribing to the topic
        LOG.info("Synchronizing cache values after update topic registration");
        synchronizeTagValues(newKnownTags);

      } catch (JMSException e) {
        throw new CacheSynchronizationException("Could not refresh tags in the live cache.", e);
      }
    }

    /**
     * Inner method that updates a second time in case an update was sent before
     * the Tag was subscribed to the topic.
     *
     * @param newTags List of new registered tags
     * @throws JMSException In case of a JMS problem
     */
    private void synchronizeTagValues(final Set<Long> newTags) throws JMSException {
      if (!newTags.isEmpty()) {
        TagController newTag = null;
        Collection<TagValueUpdate> requestedTagValues = tagRequestHandler.requestTagValues(newTags);
        for (TagValueUpdate tagValueUpdate : requestedTagValues) {
          newTag = liveCache.get(tagValueUpdate.getId());
          if (newTag != null) {
            newTag.update(tagValueUpdate);
          }
        }
      }
    }

    /**
     * This inner method gets called whenever a live tag is added to the cache
     * or when a refresh is triggered. it handles the the live tag registration
     * to the <code>JmsProxy</code> and the <code>SupervisionManager</code>.
     *
     * @param liveTag The live tag
     * @return <code>true</code>, if the tag had to be registered to the jms
     *         proxy. In case he was already registered the method will return
     *         <code>false</code>.
     * @throws JMSException in case of problems while registering the the tag
     *           for live updates.
     */
    private boolean handleLiveTagRegistration(final TagController liveTag) throws JMSException {
      final DataTagQuality tagQuality = liveTag.getTagImpl().getDataTagQuality();

      if (tagQuality.isExistingTag()) {
        if (!jmsProxy.isRegisteredListener(liveTag)) {
          jmsProxy.registerUpdateListener(liveTag, liveTag.getTagImpl());
          return true;
        }
      } else {
        supervisionManager.removeSupervisionListener(liveTag);
        if (jmsProxy.isRegisteredListener(liveTag)) {
          jmsProxy.unregisterUpdateListener(liveTag);
        }
      }

      return false;
    }
  }

  /**
   * This inner method is called after new tags were created with the
   * {@link #synchronizeCache(Set)} method and initialized with the static
   * information from the C2MON server. This call is then updating the history
   * cache with the cloned tags from the live cache, but only if the cache is
   * currently set to history mode.
   *
   * @param historyCacheUpdateList When the cache is in history mode, the <code>TagServiceImpl</code> still needs
   *        to initialize new tags with the static information which it requests from
   *        the C2MON server. Those information are also used for the tags in the
   *        history cache. To remember the tags which still need to be created in the
   *        history cache once the liveCache is correctly setup we put the tag id into
   *        this Set. The set is cleared once the history cache is up to date.
   */
  private void synchronizeHistoryCache(Set<Long> historyCacheUpdateList) {
    if (controller.isHistoryModeEnabled()) {
      TagController cdt = null;
      TagController historyTag = null;
      for (Long tagId : historyCacheUpdateList) {
        cdt = liveCache.get(tagId);
        historyTag = new TagController(cdt.getTagImpl().clone());
        // Adds the clone (without listeners) to the history cache.
        historyCache.put(tagId, historyTag);
      }
    }
  }

  @Override
  public void onHeartbeatExpired(final Heartbeat pHeartbeat) {
    synchronized (refreshLiveCacheSyncLock) {
      if (!heartbeatExpired) {
        cacheReadLock.lock();
        try {
          LOG.info("onHeartbeatExpired() - Server heartbeat has expired -> invalidating the live cache, if not yet done.");
          String errMsg = "Server heartbeat has expired.";
          invalidateLiveCache(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED, errMsg);
          heartbeatExpired = true;
        } finally {
          cacheReadLock.unlock();
        }
      }
    }
  }

  @Override
  public void onHeartbeatReceived(Heartbeat pHeartbeat) {
    // Do nothing
  }

  @Override
  public void onHeartbeatResumed(Heartbeat pHeartbeat) {
    synchronized (refreshLiveCacheSyncLock) {
      cacheReadLock.lock();
      try {
        removeLiveCacheInvalidation(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED);
      } finally {
        cacheReadLock.unlock();
      }

      if (heartbeatExpired || jmsConnectionDown) {
        LOG.info("onHeartbeatResumed() - Server heartbeat is resumed -> refreshing the live cache.");
        try {
          synchronizeCache(null);
        } catch (CacheSynchronizationException e) {
          LOG.error("onHeartbeatResumed() - Error occurred while trying to refresh the live cache.", e);
        }
      }
    }
  }

  @Override
  public void onConnection() {
    synchronized (refreshLiveCacheSyncLock) {
      cacheReadLock.lock();
      try {
        removeLiveCacheInvalidation(TagQualityStatus.JMS_CONNECTION_DOWN);
      } finally {
        cacheReadLock.unlock();
      }

      if (jmsConnectionDown || heartbeatExpired) {
        LOG.info("onConnection() - JMS connection is now up -> refreshing the live cache.");

        try {
          synchronizeCache(null);
        } catch (CacheSynchronizationException e) {
          LOG.error("onConnection() - Error occurred while trying to refresh the live cache.", e);
        }

      }
    }
  }

  @Override
  public void onDisconnection() {
    synchronized (refreshLiveCacheSyncLock) {
      if (!jmsConnectionDown) {
        cacheReadLock.lock();
        try {
          LOG.info("onDisconnection() - JMS connection is down -> invalidating the live cache, if not yet done.");
          invalidateLiveCache(TagQualityStatus.JMS_CONNECTION_DOWN, JMS_CONNECTION_LOST_MSG);
          jmsConnectionDown = true;
        } finally {
          cacheReadLock.unlock();
        }
      }
    }
  }

  /**
   * Subscribes the given tag to the {@link SupervisionServiceImpl}
   * @param cdt a newly created tag
   */
  private void subscribeToSupervisionManager(final TagController cdt) {
    // In case of a CommFault- or Status control tag, we don't register to supervision invalidations
    TagImpl tagImpl = cdt.getTagImpl();
    if (!tagImpl.isControlTag() || tagImpl.isAliveTag()) {
      supervisionManager.addSupervisionListener(cdt, tagImpl.getProcessIds(), tagImpl.getEquipmentIds(), tagImpl.getSubEquipmentIds());
    }
  }
}
