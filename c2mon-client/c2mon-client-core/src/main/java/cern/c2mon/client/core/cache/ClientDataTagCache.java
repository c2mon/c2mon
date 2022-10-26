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

import java.util.Set;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.core.listener.TagSubscriptionListener;
import cern.c2mon.client.core.service.impl.TagServiceImpl;

/**
 * This interface describes the methods which are provided manipulate
 * the references in the client data tag cache.
 * <p>
 * <b>Please note</b>, that only the {@link TagServiceImpl} should use this
 * interface face! If your class needs absolutely to access the cache
 * it should use the {@link BasicCacheHandler} instead. 
 *
 * @author Matthias Braeger
 * @see BasicCacheHandler
 */
public interface ClientDataTagCache extends BasicCacheHandler {
  
  /**
   * Adds the given listener to the tags in the cache. If the tag is not yet known to the
   * client API it will fetch it from the server.
   * <p>
   * <b>Please note, that this method is synchronizing on the history lock.</b>
   * @param tagIds List of tag ids
   * @param listener The listener to be added to the <code>ClientDataTag</code> references
   * @throws NullPointerException If one of the parameter is <code>null</code> or if one of 
   *                              the tags is not present in the cache
   * @throws CacheSynchronizationException In case of communication problems with the C2MON
   *         server during the refresh process.
   * @see #getHistoryModeSyncLock();
   */
  <T extends BaseTagListener> void subscribe(Set<Long> tagIds, T listener) throws CacheSynchronizationException;
  
  /**
   * Adds the given listener to the tags matching the regular expression. 
   * If the tag is not yet known to the client API it will fetch it from the server.
   * <p>
   * <b>Please note, that this method is synchronizing on the history lock.</b>
   * @param regexList List of regular expressions
   * @param listener The listener to be added to the <code>ClientDataTag</code> references
   * @throws NullPointerException If one of the parameter is <code>null</code> or if one of 
   *                              the tags is not present in the cache
   * @throws CacheSynchronizationException In case of communication problems with the C2MON
   *         server during the refresh process.
   * @see #getHistoryModeSyncLock();
   */
  <T extends BaseTagListener> void subscribeByRegex(Set<String> regexList, T listener) throws CacheSynchronizationException;
  
  /**
   * Adds the given listener to the tags with the respective process ids
   * If the tag is not yet known to the client API it will fetch it from the server.
   * <p>
   * <b>Please note, that this method is synchronizing on the history lock.</b>
   * @param processIds List of process ids
   * @param listener The listener to be added to the <code>ClientDataTag</code> references
   * @throws NullPointerException If one of the parameter is <code>null</code> or if one of
   *                              the tags is not present in the cache
   * @throws CacheSynchronizationException In case of communication problems with the C2MON
   *         server during the refresh process.
   * @see #getHistoryModeSyncLock();
   */
  <T extends BaseTagListener> void subscribeByProcessIds(Set<Long> processIds, T listener) throws CacheSynchronizationException;

  /**
   * Adds the given listener to the tags with the respective equipment ids
   * If the tag is not yet known to the client API it will fetch it from the server.
   * <p>
   * <b>Please note, that this method is synchronizing on the history lock.</b>
   * @param equipmentIds List of process ids
   * @param listener The listener to be added to the <code>ClientDataTag</code> references
   * @throws NullPointerException If one of the parameter is <code>null</code> or if one of
   *                              the tags is not present in the cache
   * @throws CacheSynchronizationException In case of communication problems with the C2MON
   *         server during the refresh process.
   * @see #getHistoryModeSyncLock();
   */
  <T extends BaseTagListener> void subscribeByEquipmentIds(Set<Long> equipmentIds, T listener) throws CacheSynchronizationException;

  /**
   * Adds the given listener to the tags with the respective sub equipment ids
   * If the tag is not yet known to the client API it will fetch it from the server.
   * <p>
   * <b>Please note, that this method is synchronizing on the history lock.</b>
   * @param subEquipmentIds List of process ids
   * @param listener The listener to be added to the <code>ClientDataTag</code> references
   * @throws NullPointerException If one of the parameter is <code>null</code> or if one of
   *                              the tags is not present in the cache
   * @throws CacheSynchronizationException In case of communication problems with the C2MON
   *         server during the refresh process.
   * @see #getHistoryModeSyncLock();
   */
  <T extends BaseTagListener> void subscribeBySubEquipmentIds(Set<Long> subEquipmentIds, T listener) throws CacheSynchronizationException;


  
  /**
   * This method synchronizes subscribed data tags with the server.
   * It will ask the server to send the actual tag information for all subscribed data tags.
   * Once the cache is synchronized, all subscribed <code>DataTagUpdateListener</code> will
   * be notified.
   * @throws CacheSynchronizationException In case of communication problems with the C2MON
   *         server during the refresh process.
   */
  void refresh() throws CacheSynchronizationException;
  
  /**
   * This method synchronizes subscribed data tags with the server.
   * It will ask the server to send the actual tag information for all subscribed data tags.
   * Once the cache is synchronized, all subscribed <code>DataTagUpdateListener</code> will
   * be notified.
   * 
   * @param tagIds A set of data tag id's
   * @throws CacheSynchronizationException In case of communication problems with the C2MON
   *         server during the refresh process.
   */
  void refresh(Set<Long> tagIds) throws CacheSynchronizationException;
  
  /**
   * Unsubscribes the given listener from all cache objects. 
   * @param listener The listener which shall be unsubscribed.
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  void unsubscribeAllDataTags(BaseTagListener listener);
  
  /**
   * Unsubscribes the given listener from all tags specified by the
   * list of tag ids.
   * @param dataTagIds list of tag ids 
   * @param listener The listener which shall be unsubscribed.
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  void unsubscribeDataTags(Set<Long> dataTagIds, final BaseTagListener listener);
  
  /**
   * Returns the cache size.
   * @return the cache size (this is the number of subscribed data 
   * tags contained in the cache).
   */
  int getCacheSize();
  
  /**
   * Registers a <code>TagSubscriptionListener</code>. 
   * @param listener The listener to be registered
   * @throws NullPointerException In case that the parameter is <code>null</code>.
   */
  void addTagSubscriptionListener(TagSubscriptionListener listener);
  
  /**
   * Unregisters a <code>TagSubscriptionListener</code>. 
   * @param listener The listener to be unregistered
   * @throws NullPointerException In case that the parameter is <code>null</code>.
   */
  void removeTagSubscriptionListener(TagSubscriptionListener listener);
}
