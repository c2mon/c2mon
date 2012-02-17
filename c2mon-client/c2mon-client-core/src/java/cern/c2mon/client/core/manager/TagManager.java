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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.cache.CacheSynchronizationException;
import cern.c2mon.client.core.cache.ClientDataTagCache;
import cern.c2mon.client.core.listener.TagSubscriptionListener;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.tim.shared.client.configuration.ConfigurationReport;
import cern.tim.shared.rule.RuleFormatException;

/**
 * The tag manager implements the <code>C2monTagManager</code> interface. It's
 * main job is to delegate cache requests and to update the cache with new
 * registered tags. Therefore it has to request an initial update from the C2MON
 * server.
 * <p>
 * Please note that the <code>TagManager</code> is not in charge of registering
 * the <code>ClientDataTags</code> to the <code>JmsProxy</code> nor to the
 * <code>SupervisionManager</code>. This is done directly by the cache.
 * 
 * @author Matthias Braeger
 */
@Service
public class TagManager implements CoreTagManager {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(TagManager.class);

  /**
   * The cache instance which is managing all <code>ClientDataTag</code> objects
   */
  private ClientDataTagCache cache;

  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;

  /** Reference to the <code>JmsProxy</code> singleton instance */
  private final JmsProxy jmsProxy;

  /** Lock for accessing the <code>listeners</code> variable */
  private ReentrantReadWriteLock alarmListenersLock = new ReentrantReadWriteLock();

  /** Lock for accessing the <code>listeners</code> variable */
  private ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();

  /** List of subscribed alarm listeners */
  private final Set<AlarmListener> alarmListeners = new HashSet<AlarmListener>();

  /** List of subscribed listeners */
  private final Set<TagSubscriptionListener> tagSubscriptionListeners = new HashSet<TagSubscriptionListener>();

  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   * 
   * @param pCache
   *          The cache instance which is managing all
   *          <code>ClientDataTag</code> objects
   * @param pRequestHandler
   *          Provides methods for requesting tag information from the C2MON
   *          server
   */
  @Autowired
  protected TagManager(final JmsProxy pJmsProxy, final ClientDataTagCache pCache,
      final RequestHandler pRequestHandler) {

    this.jmsProxy = pJmsProxy;
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
      } catch (CloneNotSupportedException e) {
        LOG.error("Unable to clone ClientDataTag with id " + cdt.getId(), e);
        throw new UnsupportedOperationException("Unable to clone ClientDataTag with id " + cdt.getId(), e);
      }
    }

    return clonedDataTags;
  }

  @Override
  public Set<Long> getAllSubscribedDataTagIds(final DataTagUpdateListener listener) {
    return cache.getAllTagIdsForListener(listener);
  }

  @Override
  public void refreshDataTags() {
    cache.refresh();
  }

  @Override
  public void refreshDataTags(final Collection<Long> tagIds) {
    cache.refresh(new HashSet<Long>(tagIds));
  }

  /**
   * Determine from its request all new tag id's which are not yet available in
   * the cache. Those ones are then created with a request to the C2MON server
   * and afterwards registered for the listener.
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

    try {
      // add listener to tags and subscribe them to the live topics
      Set<Long> newTags = cache.addDataTagUpdateListener(tagIds, listener);
      // Inform listeners (e.g. HistoryManager) about new subscriptions
      fireOnNewTagSubscriptionsEvent(newTags);
    }
    catch (CacheSynchronizationException cse) {
      // Rollback the subscription
      LOG.error("subscribeDataTags() : Cache error occured while subscribing to data tags ==> Rolling back subscription.");
      cache.unsubscribeDataTags(tagIds, listener);
      throw cse;
    }

    return true;
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
  public void addAlarmListener(final AlarmListener listener) throws JMSException {

    alarmListenersLock.writeLock().lock();

    if (alarmListeners.size() == 0) {
      jmsProxy.registerAlarmListener(this);
    }
    
    try {
      LOG.debug(new StringBuffer("addAlarmListener() : adding alarm listener " + listener.getClass()));
      alarmListeners.add(listener);
    } finally {
      alarmListenersLock.writeLock().unlock();
    }
  }

  @Override
  public void removeAlarmListener(final AlarmListener listener) throws JMSException {
    alarmListenersLock.writeLock().lock();
    try {
      LOG.debug(new StringBuffer("removeAlarmListener() removing alarm listener"));

      if (alarmListeners.size() == 1) {
        jmsProxy.unregisterAlarmListener(this);
      }

      alarmListeners.remove(listener);
    } finally {
      alarmListenersLock.writeLock().unlock();
    }
  }

  @Override
  public void addTagSubscriptionListener(final TagSubscriptionListener listener) {
    listenersLock.writeLock().lock();
    try {
      tagSubscriptionListeners.add(listener);
    } finally {
      listenersLock.writeLock().unlock();
    }
  }

  @Override
  public void removeTagSubscriptionListener(final TagSubscriptionListener listener) {
    listenersLock.writeLock().lock();
    try {
      tagSubscriptionListeners.remove(listener);
    } finally {
      listenersLock.writeLock().unlock();
    }
  }

  /**
   * Fires an <code>onNewTagSubscriptions()</code> event to all registered
   * <code>TagSubscriptionListener</code> listeners.
   * 
   * @param tagIds
   *          list of new subscribed tags
   */
  private void fireOnNewTagSubscriptionsEvent(final Set<Long> tagIds) {
    if (!tagIds.isEmpty()) {
      listenersLock.readLock().lock();
      try {
        Set<Long> copyList = new HashSet<Long>(tagIds);
        for (TagSubscriptionListener listener : tagSubscriptionListeners) {
          listener.onNewTagSubscriptions(copyList);
        }
      } finally {
        listenersLock.readLock().unlock();
      }
    }
  }

  /**
   * Fires an <code>onUnsubscribe()</code> event to all registered
   * <code>TagSubscriptionListener</code> listeners.
   * 
   * @param tagIds
   *          list of tags that have been removed from the cache
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

  @Override
  public Collection<ClientDataTagValue> getDataTags(final Collection<Long> tagIds) {
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
      LOG.error("getDataTags() - Unable to clone ClientDataTag! Please check the code.", e);
      throw new UnsupportedOperationException("Unable to clone ClientDataTag! Please check the code.", e);
    }

    // If there are missing values fetch them from the server
    if (!missingTags.isEmpty()) {
      try {
        Collection<TagUpdate> tagUpdates = clientRequestHandler.requestTags(missingTags);
        for (TagUpdate tagUpdate : tagUpdates) {
          try {
            ClientDataTag cdt = new ClientDataTagImpl(tagUpdate.getId());
            cdt.update(tagUpdate);
            resultList.add(cdt); // No need to clone in this case
          } catch (RuleFormatException e) {
            LOG.error("getDataTags() - Received an incorrect rule tag from the server. Please check tag with id " + tagUpdate.getId(), e);
            throw new RuntimeException("Received an incorrect rule tag from the server for tag id " + tagUpdate.getId());
          }

        }
      } catch (JMSException e) {
        LOG.error("getDataTags() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
      }
    }

    return resultList;
  }

  @Override
  public Collection<TagConfig> getTagConfigurations(final Collection<Long> tagIds) {

    try {
      // no cache for Tag Configurations => fetch them from the server
      return clientRequestHandler.requestTagConfigurations(tagIds);
    } catch (JMSException e) {
      LOG.error("getTagConfigurations() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return new ArrayList<TagConfig>();
  }

  @Override
  public String getProcessXml(final String processName) {

    try {
      return clientRequestHandler.getProcessXml(processName);
    } catch (JMSException e) {
      LOG.error("getProcessXml() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return null;
  }

  @Override
  public Collection<AlarmValue> getAllActiveAlarms() {

    try {
      return clientRequestHandler.requestAllActiveAlarms();
    } catch (JMSException e) {
      LOG.error("getAllActiveAlarms() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return new ArrayList<AlarmValue>();
  }

  @Override
  public Collection<AlarmValue> getAlarms(final Collection<Long> alarmIds) {

    try {
      return clientRequestHandler.requestAlarms(alarmIds);
    } catch (JMSException e) {
      LOG.error("getAlarms() - JMS connection lost -> Could not retrieve missing tags from the C2MON server.", e);
    }
    return new ArrayList<AlarmValue>();
  }

  @Override
  public ConfigurationReport applyConfiguration(final Long configurationId) {

    return clientRequestHandler.applyConfiguration(configurationId);
  }

  @Override
  public Collection<ProcessNameResponse> getProcessNames() {

    try {
      return clientRequestHandler.getProcessNames();
    } catch (JMSException e) {
      LOG.error("getProcessNames() - JMS connection lost -> Could not retrieve process names from the C2MON server.", e);
    }
    return  new ArrayList<ProcessNameResponse>();
  }

  @Override
  public int getCacheSize() {

    return cache.getCacheSize();
  }

  /**
   * Private method, notifies all listeners for an alarmUpdate.
   * @param alarm the updated Alarm
   */
  private void notifyAlarmListeners(final AlarmValue alarm) {

    LOG.debug("onAlarmUpdate() -  there is:" + alarmListeners.size() 
        + " listeners waiting to be notified!");  

    for (AlarmListener listener : alarmListeners) {

      listener.onAlarmUpdate(alarm);
    }
  }

  @Override
  public void onAlarmUpdate(final AlarmValue alarm) {

    alarmListenersLock.readLock().lock();

    LOG.debug("onAlarmUpdate() -  received alarm update for alarmId:" + alarm.getId());  
    try {
      notifyAlarmListeners(alarm);
    }
    finally {
      alarmListenersLock.readLock().unlock();
    }
  }
}
