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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.client.core.manager.CoreSupervisionManager;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.jms.ConnectionListener;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.TagQualityStatus;
import cern.tim.shared.rule.RuleFormatException;


/**
 * This class implements the <code>CacheSynchronizer</code> interface
 * and handles the cache synchonization between the <code>CacheController</code>
 * and the C2MON server after a JMS connection or heartbeat loss.
 *
 * @author Matthias Braeger
 */
@Service
public class CacheSynchronizerImpl implements CacheSynchronizer, HeartbeatListener, ConnectionListener {
  
  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(CacheSynchronizerImpl.class);

  /** 
   * Synchronization lock object to avoid several thread refreshing at the
   * same time the live cache. 
   */
  private final Object refreshLiveCacheSyncLock = new Object();
  
  /** Default message for a JMS connection lost exception */
  private static final String JMS_CONNECTION_LOST_MSG = "JMS connection lost.";
  
  /** 
   * Is set to <code>true</code>, if the live cache has been invalidated because of
   * a JMS exception.
   */
  private boolean jmsConnectionDown = true;
  
  /** 
   * Is set to <code>true</code>, if the live cache has been invalidated because of
   * a heartbeat expiration.
   */
  private boolean heartbeatExpired = true;

  /** The cache controller manages the cache references */
  private final CacheController controller;
  
  /** Reference to the jmsProxy singleton */
  private final JmsProxy jmsProxy;
  
  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;
  
  /** Reference to the supervision manager singleton */
  private final CoreSupervisionManager supervisionManager;
  
  /**
   * When the cache is in history mode, the <code>TagManager</code> still needs to initialize
   * new tags with the static information which it requests from the C2MON server. Those
   * information are also used for the tags in the history cache. To remember the tags which
   * still need to be created in the history cache once the liveCache is correctly setup we
   * put the tag id into this Set. The set is cleared once the history cache is up to date.
   */
  private final Set<Long> historyCacheUpdateList = new HashSet<Long>();
  
  /** 
   * <code>Map</code> reference containing all subscribed data tags which
   * are updated via the <code>JmsProxy</code>
   */
  private Map<Long, ClientDataTagImpl> liveCache = null;
  
  /** 
   * <code>Map</code> reference containing all subscribed data tags
   * which are updated via the <code>HistoryManager</code>
   */
  private Map<Long, ClientDataTagImpl> historyCache =  null;
  
  /** Reference to the cache read lock */
  private ReadLock cacheReadLock = null;
  
  /**
   * Default Constructor used by Spring to wire in the references to the other services.
   * @param pJmsProxy Reference to the {@link JmsProxy} singleton
   * @param pRequestHandler Provides methods for requesting tag information from the C2MON server
   * @param pSupervisionManager Needed to register new tags as supervision event listener
   * @param pCacheController Provides acces to the different cache instances and to the thread locks.
   */
  @Autowired
  public CacheSynchronizerImpl(final JmsProxy pJmsProxy, 
                               final RequestHandler pRequestHandler,
                               final CoreSupervisionManager pSupervisionManager,
                               final CacheController pCacheController) {
    this.jmsProxy = pJmsProxy;
    this.clientRequestHandler = pRequestHandler;
    this.supervisionManager = pSupervisionManager;
    this.controller = pCacheController;
  }
  
  @PostConstruct
  protected void init() {
    supervisionManager.addConnectionListener(this);
    supervisionManager.addHeartbeatListener(this);
    
    cacheReadLock = controller.getReadLock();
    historyCache = controller.getHistoryCache();
    liveCache = controller.getLiveCache();
  }
 

  @Override
  public void refresh(final Set<Long> tagIds) {
    synchronized (refreshLiveCacheSyncLock) {
      synchronizeCache(tagIds);
    } // end synchronized block
  }
  

  @Override
  public void createTags(final Set<Long> tagIds) {
    if (tagIds.size() > 0) {
      ClientDataTagImpl cdt = null;
      for (Long tagId : tagIds) {
        cdt = new ClientDataTagImpl(tagId);
        liveCache.put(cdt.getId(), cdt);
        if (controller.isHistoryModeEnabled()) {
          historyCacheUpdateList.add(tagId);
        }
      }
      synchronizeCache(tagIds);
      finalizeTagIntitialization();
    }
  }
  
  /** 
   * Inner method to which removes all <code>ClientDataTag</code> references
   * with the given id from the cache. At the same time it unsubscribes the
   * live tags from the <code>JmsProxy</code> where they were formerly
   * registered as <code>ServerUpdateListener</code> by the <code>TagManager</code>.
   * 
   * @param tagIds list of <code>ClientDataTag</code> id's
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
        ClientDataTagImpl liveTag = liveCache.remove(tagId);
        if (liveTag.getDataTagQuality().isExistingTag()) {
          try {
            jmsProxy.unregisterUpdateListener(liveTag);
          }
          catch (Exception e) {
            LOG.warn("removeTags() - Could not unregister tag " + tagId + " from JmsProxy. Reason: " + e.getMessage());
          }
        }
        supervisionManager.removeSupervisionListener(liveTag);
      }
    }
  }
  
  /**
   * Inner method for invalidating all tags from the live cache with a given
   * <code>TagQualityStatus</code> flag.
   * @param status The invalidation status to add
   * @param invalidationMessage The invalidation message.
   */
  private void invalidateLiveCache(final TagQualityStatus status, final String invalidationMessage) {  
    LOG.debug("invalidateLiveCache() - Invalidating " + liveCache.size() + " tag entries with " + status + ".");
    for (ClientDataTagImpl cdt : liveCache.values()) {
      cdt.invalidate(status, invalidationMessage);
    }
  }
  
  /**
   * Inner method for removing an invalid status from all tags from the live cache
   * @param statusToRemove The invalidation status to remove
   */
  private void removeLiveCacheInvalidation(final TagQualityStatus statusToRemove) {  
    LOG.debug("removeLiveCacheInvalidation() - removing " + statusToRemove + " from " + liveCache.size() + " tag entries.");
    for (ClientDataTagImpl cdt : liveCache.values()) {
      cdt.validate(statusToRemove);
    }
  }
  
  /**
   * This inner method gets called whenever a live tag is added to the cache
   * or when a refresh is triggered. it handles the the live tag registration
   * to the <code>JmsProxy</code> and the <code>SupervisionManager</code>. 
   * @param liveTag The live tag
   * @return <code>true</code>, if the tag had to be registered to the jms proxy.
   *         In case he was already registered the method will return <code>false</code>.
   * @throws JMSException In case of problems while registering the the tag for live updates.
   */
  private boolean handleLiveTagRegistration(final ClientDataTagImpl liveTag) throws JMSException {
    final DataTagQuality tagQuality = liveTag.getDataTagQuality();
    
    if (tagQuality.isInitialised() && tagQuality.isExistingTag()) {
      supervisionManager.addSupervisionListener(liveTag, liveTag.getProcessIds(), liveTag.getEquipmentIds());
      if (!jmsProxy.isRegisteredListener(liveTag)) {
        jmsProxy.registerUpdateListener(liveTag, liveTag);
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Inner method which synchronizes the live cache with the C2MON server
   * @param pTagIds Set of tag id's that shall be refreshed. If the parameter
   *        is <code>null</code>, the entire cache is updated.
   */
  private void synchronizeCache(final Set<Long> pTagIds) {
    boolean jmsConnectionLost = false;

    try {
      if (!liveCache.isEmpty()) {
        final Set<Long> unsynchronizedTagIds;
        if (pTagIds == null || jmsConnectionDown || heartbeatExpired) {
          // Refresh entire cache
          unsynchronizedTagIds = new HashSet<Long>(liveCache.keySet());
        }
        else {
          unsynchronizedTagIds = new HashSet<Long>(pTagIds);
        }
        LOG.info("synchronizeCache() - Synchronizing " + unsynchronizedTagIds.size() + " live cache entries with the server.");
        
        
        // Map that keeps a reference to all newly registered tags which values have
        // to be synchronized with a second server call.
        Map<Long, ClientDataTag> newKnownTags = new Hashtable<Long, ClientDataTag>();
        Collection<TagUpdate> tagUpdates = clientRequestHandler.requestTags(unsynchronizedTagIds);
        for (TagUpdate tagUpdate : tagUpdates) {
          try {
            ClientDataTagImpl liveTag = liveCache.get(tagUpdate.getId()); 
            liveTag.update(tagUpdate);
            if (handleLiveTagRegistration(liveTag)) {
              newKnownTags.put(liveTag.getId(), liveTag);
            }
            // remove it from the list 
            unsynchronizedTagIds.remove(tagUpdate.getId());
          }
          catch (RuleFormatException e) {
            LOG.error("synchronizeCache() - Received an incorrect rule tag from the server. Please check tag with id " + tagUpdate.getId(), e);
            throw new RuntimeException("Received an incorrect rule tag from the server for tag id " + tagUpdate.getId());
          }
        }

        // Set all tags to unknown which were not returned by the C2MON server
        // Please note that we do not touch at this point the history cache.
        for (Long tagId : unsynchronizedTagIds) {
          final ClientDataTagImpl liveTag = liveCache.get(tagId);
          if (liveTag.getDataTagQuality().isExistingTag()) {
            try {
              jmsProxy.unregisterUpdateListener(liveTag);
            }
            catch (Exception e) {
              LOG.warn("removeTags() - Could not unregister tag " + tagId + " from JmsProxy. Reason: " + e.getMessage());
            }
            supervisionManager.removeSupervisionListener(liveTag);
            final ClientDataTagImpl unkownTag = new ClientDataTagImpl(tagId);
            unkownTag.getDataTagQuality().setInvalidStatus(TagQualityStatus.UNDEFINED_TAG, "Tag is not known by the system");
            unkownTag.addUpdateListeners(liveTag.getUpdateListeners());
            liveCache.put(tagId, unkownTag);
          }
        }
        
        synchronizeCacheValues(newKnownTags);
      }
    }
    catch (Exception e) {
      LOG.error("synchronizeCache() - Could not refresh tags in the live cache.", e);
      jmsConnectionLost = true;
    }
  
    if (jmsConnectionLost) {
      invalidateLiveCache(TagQualityStatus.JMS_CONNECTION_DOWN, JMS_CONNECTION_LOST_MSG);
    }
    else {
      // Checks, if there are tags which needs to be synchronized with the history cache.
      finalizeTagIntitialization();
    }
    
    this.jmsConnectionDown = jmsConnectionLost;
    this.heartbeatExpired = jmsConnectionLost;
  }
  
  /**
   * Inner method that updates a second time in case an update was send before 
   * the ClientDataTag was subscribed to the topic.
   * @param newTags Map of uninitialized tags from the cache
   * @throws JMSException In case of a JMS problem
   */
  private void synchronizeCacheValues(final Map<Long, ClientDataTag> newTags) throws JMSException {
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
            LOG.fatal("synchronizeCacheValues() - Received an incorrect rule tag from the server. Please check tag with id " + tagValueUpdate.getId(), e);
            throw new RuntimeException(e);
          }
        }
      } // end for loop
    }
  }
  
  /**
   * This method is called by the <code>TagManager</code> after new tags were created with the
   * {@link ClientDataTagCache#create(Long)} method and initialized with the static information
   * from the C2MON server. This call is then updating the history cache with the cloned tags
   * from the live cache, but only if the cache is currently set to history mode. 
   */
  private void finalizeTagIntitialization() {
    if (controller.isHistoryModeEnabled()) {
      try {
        ClientDataTagImpl cdt = null;
        ClientDataTagImpl historyTag = null;
        for (Long tagId : historyCacheUpdateList) {
          cdt = liveCache.get(tagId);
          historyTag = cdt.clone();
          // Adds the clone (without listeners) to the history cache.
          historyCache.put(tagId, historyTag);
        }
      }
      catch (CloneNotSupportedException e) {
        LOG.error("finishedTagIntitialization() - ClientDataTag is not clonable. Please check the code!", e);
        throw new RuntimeException(e);
      }
      historyCacheUpdateList.clear();
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
        }
        finally { cacheReadLock.unlock(); }
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
      }
      finally { cacheReadLock.unlock(); }
      
      if (heartbeatExpired || jmsConnectionDown) {
        LOG.info("onHeartbeatResumed() - Server heartbeat is resumed -> refreshing the live cache.");
        synchronizeCache(null);
      }
    }
  }

  @Override
  public void onConnection() {
    synchronized (refreshLiveCacheSyncLock) {
      cacheReadLock.lock();
      try {
        removeLiveCacheInvalidation(TagQualityStatus.JMS_CONNECTION_DOWN);
      }
      finally { cacheReadLock.unlock(); }
      
      if (jmsConnectionDown || heartbeatExpired) {
        LOG.info("onConnection() - JMS connection is now up -> refreshing the live cache.");
        synchronizeCache(null);
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
        }
        finally { cacheReadLock.unlock(); }
      }
    }
  }
}
