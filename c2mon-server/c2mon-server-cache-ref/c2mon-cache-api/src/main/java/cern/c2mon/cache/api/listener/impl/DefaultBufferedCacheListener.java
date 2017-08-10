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
package cern.c2mon.cache.api.listener.impl;

import cern.c2mon.cache.api.listener.C2monBufferedCacheListener;
import cern.c2mon.shared.common.Cacheable;

/**
 * Cache listener passing collections of cache objects to listener modules
 * instead of single objects.
 *
 * @param <T> the type of cache object
 *
 * @author Mark Brightwell
 */
public class DefaultBufferedCacheListener<T extends Cacheable> extends AbstractBufferedCacheListener<T, T> {

  /**
   * Constructor.
   *
   * @param bufferedCacheListener listener expecting collections of cache objects
   * @param frequency             the frequency (in seconds) at which the buffer should be emptied
   */
  public DefaultBufferedCacheListener(final C2monBufferedCacheListener<T> bufferedCacheListener, int frequency) {
    super(bufferedCacheListener, frequency);
  }

  /**
   * Returns the cache object itself.
   */
  @Override
  T getDerivedObject(final T cacheable) {
    return cacheable;
  }

}
