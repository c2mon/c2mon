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
package cern.c2mon.server.cachepersistence.listener;

import cern.c2mon.cache.api.listener.BufferedCacheListener;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A common implementation of the SynchroBufferListener
 * designed for the cache persistence mechanism (cache to
 * database).
 *
 * <p>One is instantiated for each cache for which updates
 * should be persisted (Tag caches, alarms).
 *
 * @author Mark Brightwell
 */
@Slf4j
public class PersistenceSynchroListener<CACHEABLE extends Cacheable> implements BufferedCacheListener<CACHEABLE> {

  /**
   * The DAQ used to persist the Collection to
   * the database.
   */
  private BatchPersistenceManager persistenceManager;

  /**
   * The constructor that should be used to instantiate a new
   * listener. This listener is then automatically registered
   * with the C2monCache provided.
   *
   * @param batchPersistenceManager the DAO object that contains the logic
   *                                for persisting the cache elements
   */
  public PersistenceSynchroListener(final BatchPersistenceManager batchPersistenceManager) {
    this.persistenceManager = batchPersistenceManager;
  }

  @Override
  public void apply(List<CACHEABLE> cacheables) {
    persistenceManager.persistList(cacheables.stream().map(Cacheable::getId).collect(Collectors.toList()));
  }
}
