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
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.listener.DataTagUpdateListener;
import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.client.core.manager.CoreSupervisionManager;
import cern.c2mon.client.core.tag.ClientDataTag;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.jms.ConnectionListener;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.c2mon.shared.client.tag.TagUpdate;
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
   * Pointer to the actual used cache instance (live or history)
   */
  private Map<Long, ClientDataTagImpl> activeCache = null;
  
  /** 
   * <code>Map</code> containing all subscribed data tags which are updated via the
   * <code>HistoryManager</code>
   */
  private final Map<Long, ClientDataTagImpl> historyCache =  new Hashtable<Long, ClientDataTagImpl>(1500);
  
  /** 
   * <code>Map</code> containing all subscribed data tags which are updated via the
   * <code>JmsProxy</code>
   */
  private final Map<Long, ClientDataTagImpl> liveCache = new Hashtable<Long, ClientDataTagImpl>(1500);
  
  /** Thread lock for access to the <code>dataTags</code> Map */
  private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
  
  /** Reference to the jmsProxy singleton */
  private final JmsProxy jmsProxy;
  
  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;
  
  /** Reference to the supervision manager singleton */
  private final CoreSupervisionManager supervisionManager;
  
  /**
   * Flag to remember whether the cache is in history mode or not
   */
  private boolean historyMode = false;
  
  /** Thread synchronization lock for avoiding a cache mode switch */ 
  private final Object historyModeLock = new Object();
  
  
  /**
   * Default Constructor used by Spring to wire in the reference to the <code>JmsProxy</code>
   * and <code>RequestHandler</code>.
   * @param pJmsProxy Reference to the {@link JmsProxy} singleton
   * @param pRequestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected ClientDataTagCacheImpl(final JmsProxy pJmsProxy, final RequestHandler pRequestHandler, final CoreSupervisionManager pSupervisionManager) {
    this.jmsProxy = pJmsProxy;
    this.clientRequestHandler = pRequestHandler;
    this.supervisionManager = pSupervisionManager;
    
    this.activeCache = this.liveCache;
  }
  
  /**
   * This method is called by Spring after having created this service.
   */
  @PostConstruct
  private void init() {
    supervisionManager.addConnectionListener(this);
    supervisionManager.addHeartbeatListener(this);
  }
  
  @Override
  public ClientDataTag get(final Long tagId) {
    ClientDataTag cdt = null;
     
    cacheLock.readLock().lock();
    try {
      cdt = activeCache.get(tagId);
    }
    finally {
      cacheLock.readLock().unlock();
    }
  
    return cdt;
  }
  
  @Override
  public Collection<ClientDataTag> getAllSubscribedDataTags() {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>(activeCache.size());
    
    cacheLock.readLock().lock();
    try {
      for (ClientDataTagImpl cdt : activeCache.values()) {
        if (cdt.hasUpdateListeners()) {
          list.add(cdt);
        }
      }
    }
    finally {
      cacheLock.readLock().unlock();
    }
    
    return list;
  }

  @Override
  public Collection<ClientDataTag> getAllTagsForEquipment(final Long equipmentId) {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>(activeCache.size());
  
    cacheLock.readLock().lock();
    try {
      for (ClientDataTag cdt : activeCache.values()) {
        if (cdt.getEquipmentIds().contains(equipmentId)) {
          list.add(cdt);
        }
      }
    }
    finally {
      cacheLock.readLock().unlock();
    }
    
    return list;
  }

  @Override
  public Collection<ClientDataTag> getAllTagsForListener(final DataTagUpdateListener listener) {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>(activeCache.size());

    cacheLock.readLock().lock();
    try {
      for (ClientDataTagImpl cdt : activeCache.values()) {
        if (cdt.isUpdateListenerRegistered(listener)) {
          list.add(cdt);
        }
      }
    }
    finally {
      cacheLock.readLock().unlock();
    }
    
    return list;
  }

  @Override
  public Collection<ClientDataTag> getAllTagsForProcess(final Long processId) {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>(activeCache.size());

    cacheLock.readLock().lock();
    try {
      for (ClientDataTag cdt : activeCache.values()) {
        if (cdt.getProcessIds().contains(processId)) {
          list.add(cdt);
        }
      }
    }
    finally {
      cacheLock.readLock().unlock();
    }
    
    return list;
  }

  @Override
  public ClientDataTag create(final Long tagId) {
    if (tagId == null) {
      throw new NullPointerException("This method does not allow the tag id being null.");
    }
    ClientDataTagImpl cdt = new ClientDataTagImpl(tagId);
    cacheLock.writeLock().lock();
    try {
      if (!containsTag(tagId)) {
        liveCache.put(cdt.getId(), cdt);
        
        if (historyMode) {
          try {
            ClientDataTagImpl historyTag = cdt.clone();
            historyTag.clean();
            // Adds the cleaned clone (without listeners) to the history cache.
            historyCache.put(cdt.getId(), historyTag);
            
          }
          catch (CloneNotSupportedException e) {
            LOG.error("put() - ClientDataTag is not clonable. Please check the code!", e);
            throw new RuntimeException(e);
          }
        }
      }
      else {
        cdt = liveCache.get(tagId);
      }
      
    }
    finally {
      cacheLock.writeLock().unlock();
    }
    
    return cdt;
  }
  
  /**
   * Inner method which synchronizes the live cache with the C2MON server
   */
  private void refreshLiveCache() {
    boolean jmsConnectionLost = false;
    cacheLock.readLock().lock();
    try {
      if (!liveCache.isEmpty()) {
        LOG.info("refreshLiveCache() - Synchronizing " + liveCache.size() + " live cache entries with the server.");
        
        Collection<Long> tagIds = liveCache.keySet();
        Collection<TagUpdate> tagUpdates = clientRequestHandler.requestTags(tagIds);
        for (TagUpdate tagUpdate : tagUpdates) {
          try {
            ClientDataTagImpl liveTag = liveCache.get(tagUpdate.getId()); 
            liveTag.update(tagUpdate);
            handleLiveTagRegistration(liveTag);
          }
          catch (RuleFormatException e) {
            LOG.error("refreshLiveCache() - Could not update tag with id " + tagUpdate.getId(), e);
          }
        }
      }
    }
    catch (JMSException e) {
      LOG.error("refreshLiveCache() - Could not refresh tags in the cache. " + JMS_CONNECTION_LOST_MSG);
      jmsConnectionLost = true;
    }
    finally {
      cacheLock.readLock().unlock();
    }
  
    if (jmsConnectionLost) {
      cacheLock.readLock().lock();
      try {
        invalidateLiveCache(TagQualityStatus.JMS_CONNECTION_DOWN, JMS_CONNECTION_LOST_MSG);
      }
      finally {
        cacheLock.readLock().unlock();
      }
    }
    
    this.jmsConnectionDown = jmsConnectionLost;
    this.heartbeatExpired = jmsConnectionLost;
  }

  @Override
  public void refresh() {
    synchronized (refreshLiveCacheSyncLock) {
      refreshLiveCache();
    } // end synchronized block
  }
  
  /**
   * This inner method gets called whenever a live tag is added to the cache
   * or when a refresh is triggered. it handles the the live tag registration
   * to the <code>JmsProxy</code> and the <code>SupervisionManager</code>. 
   * @param liveTag The live tag
   */
  private void handleLiveTagRegistration(final ClientDataTagImpl liveTag) {
    final DataTagQuality tagQuality = liveTag.getDataTagQuality();
    try {
      if (tagQuality.isInitialised() && tagQuality.isExistingTag()) {
        supervisionManager.addSupervisionListener(liveTag, liveTag.getProcessIds(), liveTag.getEquipmentIds());
        if (!jmsProxy.isRegisteredListener(liveTag)) {
          jmsProxy.registerUpdateListener(liveTag, liveTag);
        }
      }
    }
    catch (JMSException e) {
      LOG.warn("initializeNewTags() - invalidate tag " + liveTag.getId() + ". Reason: " + JMS_CONNECTION_LOST_MSG);
      liveTag.invalidate(TagQualityStatus.JMS_CONNECTION_DOWN, JMS_CONNECTION_LOST_MSG);
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
    for (Long tagId : tagIds) {
      if (historyMode) {
        historyCache.remove(tagId);
      }
      ClientDataTag liveTag = liveCache.remove(tagId);
      jmsProxy.unregisterUpdateListener(liveTag);
      supervisionManager.removeSupervisionListener(liveTag);
    } 
  }

  @Override
  public Set<Long> unsubscribeAllDataTags(final DataTagUpdateListener listener) {
    Set<Long> tagsToRemove = new HashSet<Long>();
    cacheLock.writeLock().lock();
    try {
      for (ClientDataTagImpl cdt : activeCache.values()) {
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
      cacheLock.writeLock().unlock();
    }
    
    return tagsToRemove;
  }

  @Override
  public Set<Long> unsubscribeDataTags(final Set<Long> dataTagIds, final DataTagUpdateListener listener) {
    Set<Long> tagsToRemove = new HashSet<Long>();
    cacheLock.writeLock().lock();
    try {
      ClientDataTagImpl cdt = null;
      for (Long tagId : dataTagIds) {
        cdt = activeCache.get(tagId);
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
      cacheLock.writeLock().unlock();
    }
    
    return tagsToRemove;
  }

  @Override
  public Map<Long, ClientDataTag> get(final Set<Long> tagIds) {
    Map<Long, ClientDataTag> resultMap = new HashMap<Long, ClientDataTag>(tagIds.size());
    cacheLock.readLock().lock();
    try {
      for (Long tagId : tagIds) {
        resultMap.put(tagId, activeCache.get(tagId));
      }
    }
    finally {
      cacheLock.readLock().unlock();
    }
    
    return resultMap;
  }

  @Override
  public boolean isHistoryModeEnabled() {
    return historyMode;
  }

  @Override
  public void setHistoryMode(final boolean enable) {
    synchronized (historyModeLock) {
      if (historyMode == enable) {
        LOG.info("setHistoryMode() - The cache is already in history mode.");
        return;
      }
      
      cacheLock.writeLock().lock();
      try {
        if (enable) {
          enableHistoryMode();
        }
        else {
          disableHistoryMode();
        }
      }
      finally {
        cacheLock.writeLock().unlock();
      }
      
      historyMode = enable;
    }
  }
  
  @Override
  public Object getHistoryModeSyncLock() {
    return historyModeLock;
  }
  
  /**
   * Inner method which clones the entire live cache and moves all registered
   * <code>DataTagUpdateListener</code> listeners to the history cache instance.
   */
  private void enableHistoryMode() {
    historyCache.clear();
    ClientDataTagImpl liveTag = null;
    ClientDataTagImpl historyTag = null;
    Collection<DataTagUpdateListener> listeners = null;
    
    try {
      for (Entry<Long, ClientDataTagImpl> entry : liveCache.entrySet()) {
        liveTag = entry.getValue();
        
        historyTag = liveTag.clone();
        historyTag.clean();
        
        listeners = liveTag.getUpdateListeners();
        liveTag.removeAllUpdateListeners();
        historyTag.addUpdateListeners(listeners);
        historyCache.put(entry.getKey(), historyTag);
      }
    }
    catch (CloneNotSupportedException e) {
      LOG.error("put() - ClientDataTag is not clonable. Please check the code!", e);
      throw new RuntimeException(e);
    }
    activeCache = historyCache;
  }
  
  /**
   * Inner method which moves all registered <code>DataTagUpdateListener</code>
   * listeners back to the live cache instance.
   */
  private void disableHistoryMode() {
    ClientDataTagImpl historyTag = null;
    Collection<DataTagUpdateListener> listeners = null;
    
    for (Entry<Long, ClientDataTagImpl> entry : historyCache.entrySet()) {
      historyTag = entry.getValue();
      listeners = historyTag.getUpdateListeners();
      
      historyTag.removeAllUpdateListeners();
      liveCache.get(entry.getKey()).addUpdateListeners(listeners);
    }
    
    activeCache = liveCache;
  }

  @Override
  public boolean containsTag(final Long tagId) {
    return activeCache.containsKey(tagId);
  }

  @Override
  public Set<Long> addDataTagUpdateListener(final Set<Long> tagIds, final DataTagUpdateListener listener) {
    Set<Long> newSubscriptions = new HashSet<Long>();
    cacheLock.readLock().lock();
    try {
      ClientDataTagImpl cdt = null;
      for (Long tagId : tagIds) {
        cdt = activeCache.get(tagId);
        if (!cdt.hasUpdateListeners()) {
          newSubscriptions.add(tagId);
          handleLiveTagRegistration(liveCache.get(tagId));
        }
        cdt.addUpdateListener(listener);
      }
    }
    finally {
      cacheLock.readLock().unlock();
    }
    
    return newSubscriptions;
  }

  @Override
  public void onHeartbeatExpired(final Heartbeat pHeartbeat) {
    synchronized (refreshLiveCacheSyncLock) {
      if (!heartbeatExpired) {
        cacheLock.readLock().lock();
        try {
          LOG.info("onHeartbeatExpired() - Server heartbeat has expired -> invalidating the live cache, if not yet done.");
          String errMsg = "Server heartbeat has expired.";
          invalidateLiveCache(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED, errMsg);
          heartbeatExpired = true;
        }
        finally {
          cacheLock.readLock().unlock();
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
      cacheLock.readLock().lock();
      try {
        removeLiveCacheInvalidation(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED);
      }
      finally {
        cacheLock.readLock().unlock();
      }
      if (heartbeatExpired || jmsConnectionDown) {
        LOG.info("onHeartbeatResumed() - Server heartbeat is resumed -> refreshing the live cache.");
        refreshLiveCache();
      }
    }
  }

  @Override
  public void onConnection() {
    synchronized (refreshLiveCacheSyncLock) {
      cacheLock.readLock().lock();
      try {
        removeLiveCacheInvalidation(TagQualityStatus.JMS_CONNECTION_DOWN);
      }
      finally {
        cacheLock.readLock().unlock();
      }
      if (jmsConnectionDown || heartbeatExpired) {
        LOG.info("onConnection() - JMS connection is now up -> refreshing the live cache.");
        refreshLiveCache();
      }
    }
  }

  @Override
  public void onDisconnection() {
    synchronized (refreshLiveCacheSyncLock) {
      if (!jmsConnectionDown) {
        cacheLock.readLock().lock();
        try {    
          LOG.info("onDisconnection() - JMS connection is down -> invalidating the live cache, if not yet done.");
          invalidateLiveCache(TagQualityStatus.JMS_CONNECTION_DOWN, JMS_CONNECTION_LOST_MSG);
          jmsConnectionDown = true;
        }
        finally {
          cacheLock.readLock().unlock();
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
