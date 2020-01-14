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
package cern.c2mon.server.history.logger;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.history.structure.Loggable;
import cern.c2mon.server.history.structure.LoggerConverter;
import cern.c2mon.shared.common.Cacheable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Default implementation of a BatchLogger for C2MON that
 * uses the C2MON persistence manager as a disk fallback
 * mechanism.
 *
 * <p>To use this implementation, a LoggerConverter
 * needs to be provided for the cache object, converting
 * it into the object used for logging (which must implement
 * the IFallback interface for the fallback mechanism).
 *
 * @author Mark Brightwell
 *
 * @param <T> the cache object that needs logging
 */
public class DefaultLogger<T extends Cacheable> implements BatchLogger<T> {

  /**
   * Converter bean for converting cache objects into a log (history) object.
   */
  private LoggerConverter<T> converter;

  /**
   * The fallback persistence manager.
   */
  private IPersistenceManager persistenceManager;

  /**
   * Unique constructor.
   *
   * @param converter a converter bean
   * @param persistenceManager the persistence manager
   */
  public DefaultLogger(final LoggerConverter<T> converter, final IPersistenceManager persistenceManager) {
    super();
    this.converter = converter;
    this.persistenceManager = persistenceManager;
  }

  @Override
  public void log(final Collection<T> tags) {
    ArrayList<Loggable> loggables = new ArrayList<Loggable>();

    // Convert the list of DataTagCacheObjects to TagRecord objects (IFallback objects)
    for (T tag : tags) {
        if (tag != null) {
          loggables.add(converter.convertToLogged(tag));
        }
    }
    persistenceManager.storeData(loggables);
  }

}
