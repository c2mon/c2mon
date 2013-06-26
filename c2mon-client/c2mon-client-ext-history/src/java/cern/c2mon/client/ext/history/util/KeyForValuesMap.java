/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
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
 *****************************************************************************/
package cern.c2mon.client.ext.history.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cern.c2mon.client.ext.history.playback.publish.SupervisionListenersManager;
import cern.c2mon.client.ext.history.playback.publish.TagListenersManager;

/**
 * This class make it possible to map keys to a set of values, without needing
 * to think about if a key have any values already or not. When removing a value
 * it returns the keys which have become empty because of it. When adding a
 * value to a key, it returns <code>true</code> if the key were used for the
 * first time or not.
 * 
 * @see TagListenersManager
 * @see SupervisionListenersManager
 * 
 * @author vdeila
 * 
 * @param <K>
 *          The type of key which all the values will be referenced by
 * @param <V>
 *          The type of value
 */
public class KeyForValuesMap<K extends Number, V> {

  /** The keys with the values */
  private final Map<K, Set<V>> keysWithValues;

  /**
   * Constructor
   */
  public KeyForValuesMap() {
    this.keysWithValues = new HashMap<K, Set<V>>();
  }

  /**
   * Removes all keys and values
   */
  public void clear() {
    this.keysWithValues.clear();
  }

  /**
   *
   * @param key
   *          The key to add the value to
   * @param value
   *          The value to add
   * @return <code>true</code> if this is the first value registered on the key
   */
  public synchronized boolean add(final K key, final V value) {
    final Set<V> list = getList(key, true);
    boolean result = list.isEmpty();
    list.add(value);
    return result;
  }

  /**
   * 
   * @param value
   *          The value to remove
   * @return a list of keys which doesn't have any values after the removal of
   *         this key
   */
  public synchronized Collection<K> remove(final V value) {
    // Goes through all the lists and removes the key, if it exists.
    final Set<K> removedKeys = new HashSet<K>();
    final Set<K> keys = this.keysWithValues.keySet();
    for (K key : keys) {
      final Set<V> list = this.keysWithValues.get(key);
      if (list.remove(value) && list.isEmpty()) {
        // Removes the list if it is empty
        removedKeys.add(key);
      }
    }
    for (K removedKey : removedKeys) {
      this.keysWithValues.remove(removedKey);
    }
    return removedKeys;
  }
  
  /**
   * All values with the given key will be removed.
   * 
   * @param key the key to remove. 
   */
  public synchronized void remove(final K key) {
    this.keysWithValues.remove(key);
  }

  /**
   * 
   * @param key
   *          the key to check
   * @return <code>true</code> if the key have any values
   */
  public synchronized boolean haveKey(final K key) {
    final Set<V> list = getList(key, false);
    return list != null && !list.isEmpty();
  }

  /**
   * 
   * @param key
   *          the key to get the values for
   * @return the values registered on the key, or an empty list of there is no values.
   */
  public synchronized Collection<V> getValues(final K key) {
    Set<V> result = getList(key, false);
    if (result == null) {
      return new HashSet<V>();
    }
    else {
      return new HashSet<V>(result);
    }
  }

  /**
   * 
   * @param key
   *          The key to get the values of
   * @param createIfNeeded
   *          if <code>true</code> it creates a list if it doesn't exist already
   * @return The list, or <code>null</code> if it doesn't exist and
   *         <code>createIfNeeded</code> is <code>false</code>
   */
  private Set<V> getList(final K key, final boolean createIfNeeded) {
    Set<V> result = keysWithValues.get(key);
    if (result == null && createIfNeeded) {
      result = new HashSet<V>();
      keysWithValues.put(key, result);
    }
    return result;
  }
  
}
