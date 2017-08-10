/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.cache.api.listener.impl;

import cern.c2mon.cache.api.listener.C2monBufferedCacheListener;
import cern.c2mon.cache.api.listener.impl.AbstractBufferedCacheListener;
import cern.c2mon.shared.common.Cacheable;

/**
 * Cache listener passing collections of keys to listener modules
 * instead of the objects themselves.
 *
 * @param <T> the type of cache object
 *
 * @author Mark Brightwell
 */
public class BufferedKeyCacheListener<T extends Cacheable> extends AbstractBufferedCacheListener<T, Long> {

  /**
   * Constructor
   *
   * @param bufferedKeyTimCacheListener the listener to register.
   * @param frequency                   the frequency (in ms) at which the buffer should be emptied
   */
  public BufferedKeyCacheListener(final C2monBufferedCacheListener<Long> bufferedKeyTimCacheListener, int frequency) {
    super(bufferedKeyTimCacheListener, frequency);
  }

  /**
   * Returns the key of the cache object.
   */
  @Override
  Long getDerivedObject(final T cacheable) {
    return cacheable.getId();
  }

}
