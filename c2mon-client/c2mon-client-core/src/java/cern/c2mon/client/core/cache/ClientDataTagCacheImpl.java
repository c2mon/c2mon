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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.core.C2monTagManager;
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
 * This class implements the cache of the C2MON client API. The public method
 * provided by this class are never accessed directly by the application layer.
 * Only the <code>TagManager</code> has a reference to the cache service and is
 * controlling the access to it.
 * <p>
 * The cache provides a <code>create()</code> method for creating a new
 * <code>ClientDataTag</code> cache entry. In the background it handles also the
 * subscription to the incoming live events. Only the initialization of the tag
 * is performed by the <code>TagManager</code>.
 * <p>
 * It is possible to switch the <code>ClientDataTagCache</code> from live mode
 * into history mode and back. Therefore this class manages internally two
 * <code>ClientDataTag</code> map instances, one for live tag updates and the
 * other for historical events. Depending on the cache mode the getter methods
 * return either references to the live tags or to the history tags. 
 *
 * @author Matthias Braeger
 */
@Service
public class ClientDataTagCacheImpl implements ClientDataTagCache, HeartbeatListener, ConnectionListener {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(ClientDataTagCacheImpl.class);
  
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
  
  /** 
   * Synchronization lock object to avoid several thread refreshing at the
   * same time the live cache. 
   */
  private final Object refreshLiveCacheSyncLock = new Object();
  
  /**
   * When the cache is in history mode, the <code>TagManager</code> still needs to initialize
   * new tags with the static information which it requests from the C2MON server. Those
   * information are also used for the tags in the history cache. To remember the tags which
   * still need to be created in the history cache once the liveCache is correctly setup we
   * put the tag id into this Set. The set is cleared once the history cache is up to date.
   */
  private final Set<Long> historyCacheUpdateList = new HashSet<Long>();
  
  /** Reference to the jmsProxy singleton */
  private final JmsProxy jmsProxy;
  
  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;
  
  /** Reference to the supervision manager singleton */
  private final CoreSupervisionManager supervisionManager;
  
  /** The cache controller manages the cache references */
  private final CacheController controller;
  
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
  
  /** Reference to the cache write lock */
  private WriteLock cacheWriteLock = null;
  
  /**
   * Default Constructor used by Spring to wire in the reference to the <code>JmsProxy</code>
   * and <code>RequestHandler</code>.
   * @param pJmsProxy Reference to the {@link JmsProxy} singleton
   * @param pRequestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected ClientDataTagCacheImpl(final JmsProxy pJmsProxy, 
                                   final RequestHandler pRequestHandler,
                                   final CoreSupervisionManager pSupervisionManager,
                                   final CacheController pCacheController) {
    this.jmsProxy = pJmsProxy;
    this.clientRequestHandler = pRequestHandler;
    this.supervisionManager = pSupervisionManager;
    this.controller = pCacheController;
  }
  
  /**
   * This method is called by Spring after having created this service.
   */
  @PostConstruct
  protected void init() {
    supervisionManager.addConnectionListener(this);
    supervisionManager.addHeartbeatListener(this);
    
    cacheReadLock = controller.getReadLock();
    cacheWriteLock = controller.getWriteLock();
    
    historyCache = controller.getHistoryCache();
    liveCache = controller.getLiveCache();
  }
  
  @Override
  public ClientDataTag get(final Long tagId) {
    ClientDataTag cdt = null;
     
    cacheReadLock.lock();
    try {
      cdt = controller.getActiveCache().get(tagId);
    }
    finally { cacheReadLock.unlock(); }
  
    return cdt;
  }
  
  @Override
  public Collection<ClientDataTag> getAllSubscribedDataTags() {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>(controller.getActiveCache().size());
    
    cacheReadLock.lock();
    try {
      for (ClientDataTagImpl cdt : controller.getActiveCache().values()) {
        if (cdt.hasUpdateListeners()) {
          list.add(cdt);
        }
      }
    }
    finally { cacheReadLock.unlock(); }
    
    return list;
  }

  @Override
  public Collection<ClientDataTag> getAllTagsForEquipment(final Long equipmentId) {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>();
  
    cacheReadLock.lock();
    try {
      for (ClientDataTag cdt : controller.getActiveCache().values()) {
        if (cdt.getEquipmentIds().contains(equipmentId)) {
          list.add(cdt);
        }
      }
    }
    finally { cacheReadLock.unlock(); }
    
    return list;
  }

  @Override
  public Collection<ClientDataTag> getAllTagsForListener(final DataTagUpdateListener listener) {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>();

    cacheReadLock.lock();
    try {
      for (ClientDataTagImpl cdt : controller.getActiveCache().values()) {
        if (cdt.isUpdateListenerRegistered(listener)) {
          list.add(cdt);
        }
      }
    }
    finally { cacheReadLock.unlock(); }
    
    return list;
  }
  
  @Override
  public Set<Long> getAllTagIdsForListener(final DataTagUpdateListener listener) {
    Set<Long> list = new HashSet<Long>();

    cacheReadLock.lock();
    try {
      for (ClientDataTagImpl cdt : controller.getActiveCache().values()) {
        if (cdt.isUpdateListenerRegistered(listener)) {
          list.add(cdt.getId());
        }
      }
    }
    finally {
      cacheReadLock.unlock();
    }
    
    return list;
  }

  @Override
  public Collection<ClientDataTag> getAllTagsForProcess(final Long processId) {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>();

    cacheReadLock.lock();
    try {
      for (ClientDataTag cdt : controller.getActiveCache().values()) {
        if (cdt.getProcessIds().contains(processId)) {
          list.add(cdt);
        }
      }
    }
    finally {
      cacheReadLock.unlock();
    }
    
    return list;
  }
  

  /**
   * Inner method which creates a <code>ClientDataTag</code> object and adds it to
   * the cache. If the cache has already an entry for that tag it does not create a
   * new one but returns the existing <b>live tag</b> reference.
   * <p>
   * This method is used by the
   * {@link C2monTagManager#subscribeDataTags(Set, DataTagUpdateListener)}
   * method to create new tags in the cache.
   * This method is called before adding the {@link DataTagUpdateListener} references
   * to the <code>ClientDataTag</code>. 
   * <p>
   * Please note that the cache does not handle the subscription of the
   * <code>ClientDataTag</code> to the <code>JmsProxy</code> or <code>SupervisionManager</code>.
   * All this is done by the <code>C2monTagManager</code>. For this it is using the
   * <b>live tag</b> reference which is returned by this method call.
   * 
   * @param tagIds The ids of the <code>ClientDataTag</code> objects that shall be
   *                      added to the cache.
   * @see cern.c2mon.client.core.C2monTagManager#subscribeDataTags(Collection, DataTagUpdateListener)
   */
  private void createTags(final Set<Long> tagIds) {
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
 
  
  /**
   * Inner method which synchronizes the live cache with the C2MON server
   * @param pTagIds Set of tag id's that shall be refreshed. If the parameter
   *        is <code>null</code>, the entire cache is updated.
   */
  private void synchronizeCache(final Set<Long> pTagIds) {
    boolean jmsConnectionLost = false;

    try {
      if (!liveCache.isEmpty()) {
        Set<Long> tagIds = pTagIds;
        if (tagIds == null) {
          // Refresh entire cache
          tagIds = liveCache.keySet();
        }
        LOG.info("synchronizeCache() - Synchronizing " + tagIds.size() + " live cache entries with the server.");
        
        
        // Map that keeps a reference to all newly registered tags which values have
        // to be synchronized with a second server call.
        Map<Long, ClientDataTag> newKnownTags = new Hashtable<Long, ClientDataTag>();
        Collection<TagUpdate> tagUpdates = clientRequestHandler.requestTags(tagIds);
        for (TagUpdate tagUpdate : tagUpdates) {
          try {
            ClientDataTagImpl liveTag = liveCache.get(tagUpdate.getId()); 
            liveTag.update(tagUpdate);
            if (handleLiveTagRegistration(liveTag)) {
              newKnownTags.put(liveTag.getId(), liveTag);
            }
          }
          catch (RuleFormatException e) {
            LOG.error("synchronizeCache() - Received an incorrect rule tag from the server. Please check tag with id " + tagUpdate.getId(), e);
            throw new RuntimeException("Received an incorrect rule tag from the server for tag id " + tagUpdate.getId());
          }
        }

        synchronizeCacheValues(newKnownTags);
      }
    }
    catch (JMSException e) {
      LOG.error("synchronizeCache() - Could not refresh tags in the live cache. " + JMS_CONNECTION_LOST_MSG);
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

  
  @Override
  public void refresh() {
    synchronized (refreshLiveCacheSyncLock) {
      synchronizeCache(null);
    } // end synchronized block
  }
  
  @Override
  public void refresh(final Set<Long> tagIds) {
    synchronized (refreshLiveCacheSyncLock) {
      synchronizeCache(tagIds);
    } // end synchronized block
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
   * Inner method to which removes all <code>ClientDataTag</code> references
   * with the given id from the cache. At the same time it unsubscribes the
   * live tags from the <code>JmsProxy</code> where they were formerly
   * registered as <code>ServerUpdateListener</code> by the <code>TagManager</code>.
   * 
   * @param tagIds list of <code>ClientDataTag</code> id's
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  private void remove(final Set<Long> tagIds) {
    if (tagIds.size() > 0) {
      LOG.info("remove() - Removing " + tagIds.size() + " tags from the cache.");
      for (Long tagId : tagIds) {
        if (controller.isHistoryModeEnabled()) {
          historyCache.remove(tagId);
        }
        ClientDataTagImpl liveTag = liveCache.remove(tagId);
        try {
          jmsProxy.unregisterUpdateListener(liveTag);
        }
        catch (Exception e) {
          LOG.warn("remove() - Could not unregister tag " + tagId + " from JmsProxy. Reason: " + e.getMessage());
        }
        supervisionManager.removeSupervisionListener(liveTag);
      }
    }
  }

  @Override
  public Set<Long> unsubscribeAllDataTags(final DataTagUpdateListener listener) {
    Set<Long> tagsToRemove = new HashSet<Long>();
    cacheWriteLock.lock();
    try {
      for (ClientDataTagImpl cdt : controller.getActiveCache().values()) {
        if (cdt.isUpdateListenerRegistered(listener)) {
          cdt.removeUpdateListener(listener);
          if (!cdt.hasUpdateListeners()) {
            tagsToRemove.add(cdt.getId());
          }
        }
      }
      
      // Remove from cache
      remove(tagsToRemove);
    }
    finally {
      cacheWriteLock.unlock();
    }
    
    return tagsToRemove;
  }

  @Override
  public Set<Long> unsubscribeDataTags(final Set<Long> dataTagIds, final DataTagUpdateListener listener) {
    Set<Long> tagsToRemove = new HashSet<Long>();
    cacheWriteLock.lock();
    try {
      ClientDataTagImpl cdt = null;
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
      remove(tagsToRemove);
    }
    finally {
      cacheWriteLock.unlock();
    }
    
    return tagsToRemove;
  }

  @Override
  public Map<Long, ClientDataTag> get(final Set<Long> tagIds) {
    Map<Long, ClientDataTag> resultMap = new HashMap<Long, ClientDataTag>(tagIds.size());
    cacheReadLock.lock();
    try {
      for (Long tagId : tagIds) {
        resultMap.put(tagId, controller.getActiveCache().get(tagId));
      }
    }
    finally {
      cacheReadLock.unlock();
    }
    
    return resultMap;
  }

  @Override
  public boolean isHistoryModeEnabled() {
    return controller.isHistoryModeEnabled();
  }

  @Override
  public void setHistoryMode(final boolean enable) {
    controller.setHistoryMode(enable);
  }
  
  @Override
  public Object getHistoryModeSyncLock() {
    return controller.getHistoryModeSyncLock();
  }

  @Override
  public boolean containsTag(final Long tagId) {
    return controller.getActiveCache().containsKey(tagId);
  }

  @Override
  public Set<Long> addDataTagUpdateListener(final Set<Long> tagIds, final DataTagUpdateListener listener) {
    Set<Long> newTagIds = new HashSet<Long>();
    synchronized (getHistoryModeSyncLock()) {
      cacheWriteLock.lock();
      try {
        ClientDataTagImpl cdt = null;
        for (Long tagId : tagIds) {
          if (liveCache.containsKey(tagId)) {
            cdt = controller.getActiveCache().get(tagId);
            cdt.addUpdateListener(listener);
          }
          else {
            newTagIds.add(tagId);
          }
        }
        
        createTags(newTagIds);
        for (Long tagId : newTagIds) {
          cdt = controller.getActiveCache().get(tagId);
          cdt.addUpdateListener(listener);
        }
      }
      finally {
        cacheWriteLock.unlock();
      }
    } // end of synchronization
    
    return newTagIds;
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
        finally {
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
      }
      finally {
        cacheReadLock.unlock();
      }
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
      finally {
        cacheReadLock.unlock();
      }
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
        finally {
          cacheReadLock.unlock();
        }
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
}
