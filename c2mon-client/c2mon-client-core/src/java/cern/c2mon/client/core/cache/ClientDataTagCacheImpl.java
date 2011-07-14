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

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.listener.DataTagUpdateListener;
import cern.c2mon.client.core.tag.ClientDataTag;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.tim.shared.common.datatag.TagQualityStatus;
import cern.tim.shared.rule.RuleFormatException;

@Service
public class ClientDataTagCacheImpl implements ClientDataTagCache {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(ClientDataTagCacheImpl.class);
  
  /**
   * Pointer to the actual used cache instance (live or history)
   */
  private Map<Long, ClientDataTag> activeCache = null;
  
  /** 
   * <code>Map</code> containing all subscribed data tags which are updated via the
   * <code>HistoryManager</code>
   */
  private Map<Long, ClientDataTag> historyCache =  new Hashtable<Long, ClientDataTag>(1500);
  
  /** 
   * <code>Map</code> containing all subscribed data tags which are updated via the
   * <code>JmsProxy</code>
   */
  private final Map<Long, ClientDataTag> liveCache = new Hashtable<Long, ClientDataTag>(1500);
  
  /** Thread lock for access to the <code>dataTags</code> Map */
  private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
  
  /** Reference to the jmsProxy singleton */
  private final JmsProxy jmsProxy;
  
  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;
  
  /**
   * Flag to remember whether the cache is in history mode or not
   */
  private boolean historyMode = false;
  
  
  /**
   * Default Constructor used by Spring to wire in the reference to the <code>JmsProxy</code>
   * and <code>RequestHandler</code>.
   * @param pJmsProxy Reference to the {@link JmsProxy} singleton
   * @param pRequestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected ClientDataTagCacheImpl(final JmsProxy pJmsProxy, final RequestHandler pRequestHandler) {
    this.jmsProxy = pJmsProxy;
    this.clientRequestHandler = pRequestHandler;
    this.activeCache = this.liveCache;
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
      for (ClientDataTag cdt : activeCache.values()) {
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
  public Collection<ClientDataTag> getAllUnsubscribedDataTags() {
    Collection<ClientDataTag> list = new ArrayList<ClientDataTag>();

    cacheLock.readLock().lock();
    try {
      for (ClientDataTag cdt : activeCache.values()) {
        if (!cdt.hasUpdateListeners()) {
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
      for (ClientDataTag cdt : activeCache.values()) {
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
  public void put(final ClientDataTag clientDataTag) {
    if (clientDataTag != null) {
      cacheLock.writeLock().lock();
      try {
        liveCache.put(clientDataTag.getId(), clientDataTag);
        if (historyMode) {
          try {
            ClientDataTag historyTag = clientDataTag.clone();
            historyTag.clean();
            // Adds the cleaned clone (without listeners) to the history cache.
            historyCache.put(clientDataTag.getId(), historyTag);
            
          }
          catch (CloneNotSupportedException e) {
            LOG.error("put() - ClientDataTag is not clonable. Please check the code!", e);
            throw new RuntimeException(e);
          }
        }
      }
      finally {
        cacheLock.writeLock().unlock();
      }
    }
  }

  @Override
  public void refresh() {
    boolean jmsConnectionLost = false;
    cacheLock.readLock().lock();
    try {
      Collection<Long> tagIds = liveCache.keySet();
      Collection<TagUpdate> tagUpdates = clientRequestHandler.requestTags(tagIds);
      for (TagUpdate tagUpdate : tagUpdates) {
        try {
          liveCache.get(tagUpdate.getId()).update(tagUpdate);
        }
        catch (RuleFormatException e) {
          LOG.error("refresh() - Could not update tag with id " + tagUpdate.getId(), e);
        }
      }
    }
    catch (JMSException e) {
      LOG.error("refresh() - Could not refresh tags in the cache. JMS connection lost.");
      jmsConnectionLost = true;
    }
    finally {
      cacheLock.readLock().unlock();
    }
    
    if (jmsConnectionLost) {
      cacheLock.readLock().lock();
      try {
        for (ClientDataTag cdt : liveCache.values()) {
          cdt.getDataTagQuality().addInvalidStatus(TagQualityStatus.INACCESSIBLE, "JMS connection lost.");
        }
      }
      finally {
        cacheLock.readLock().unlock();
      }
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
      
    } 
  }

  @Override
  public Set<Long> unsubscribeAllDataTags(final DataTagUpdateListener listener) {
    Set<Long> tagsToRemove = new HashSet<Long>();
    cacheLock.writeLock().lock();
    try {
      for (ClientDataTag cdt : activeCache.values()) {
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
      ClientDataTag cdt = null;
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
  public synchronized void setHistoryMode(final boolean enable) {
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
  
  /**
   * Inner method which clones the entire live cache and moves all registered
   * <code>DataTagUpdateListener</code> listeners to the history cache instance.
   */
  private void enableHistoryMode() {
    historyCache.clear();
    ClientDataTag liveTag = null;
    ClientDataTag historyTag = null;
    Collection<DataTagUpdateListener> listeners = null;
    
    try {
      for (Entry<Long, ClientDataTag> entry : liveCache.entrySet()) {
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
    ClientDataTag historyTag = null;
    Collection<DataTagUpdateListener> listeners = null;
    
    for (Entry<Long, ClientDataTag> entry : historyCache.entrySet()) {
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
    Set<Long> retVals = new HashSet<Long>();
    cacheLock.readLock().lock();
    try {
      ClientDataTag cdt = null;
      for (Long tagId : tagIds) {
        cdt = activeCache.get(tagId);
        if (!cdt.hasUpdateListeners()) {
          retVals.add(tagId);
        }
        cdt.addUpdateListener(listener);
      }
    }
    finally {
      cacheLock.readLock().unlock();
    }
    
    return retVals;
  }
}
