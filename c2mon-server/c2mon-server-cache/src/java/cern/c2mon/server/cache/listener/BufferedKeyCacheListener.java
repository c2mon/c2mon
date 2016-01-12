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
package cern.c2mon.server.cache.listener;

import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.shared.common.Cacheable;

/**
 * Cache listener passing collections of keys to listener modules
 * instead of the objects themselves.
 * 
 * @author Mark Brightwell
 * @param <T> the type of cache object
 *
 */
public class BufferedKeyCacheListener<T extends Cacheable> extends AbstractBufferedCacheListener<T, Long> {

  /**
   * Constructor
   * @param bufferedKeyTimCacheListener the listener to register.
   */
  public BufferedKeyCacheListener(final BufferedTimCacheListener<Long> bufferedKeyTimCacheListener) {
    super(bufferedKeyTimCacheListener);   
  }

  /**
   * Returns the key of the cache object.
   */
  @Override
  Long getDerivedObject(final T cacheable) {
    return cacheable.getId();
  }

}
