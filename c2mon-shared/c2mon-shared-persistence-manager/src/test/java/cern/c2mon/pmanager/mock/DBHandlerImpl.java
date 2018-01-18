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
package cern.c2mon.pmanager.mock;

import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the IDBPersistenceHandler interface for testing purposes.
 * Instead of writing to a DB it just stores the data in an ArrayList
 *
 * @author mruizgar
 */
public class DBHandlerImpl implements IDBPersistenceHandler<IFallback> {

  /**
   * Container for the objects that have to be stored
   */
  private List<IFallback> storage = new ArrayList<>();

  /**
   * Gets a string identifying the DB user account
   *
   * @return String The string identifying the user account
   */
  @Override
  public final String getDBInfo() {
    return "timdb@test";
  }

  /**
   * Stores an IFallback object into a DB table
   *
   * @param object IFallback object containing the data to be committed to the DB
   * @throws IDBPersistenceException An exception is thrown in case the object cannot be committed to the DB
   */

  @Override
  public final void storeData(final IFallback object) throws IDBPersistenceException {
    if (object instanceof FallbackImpl) {
      if (!object.toString().equals(FallbackImpl.ERROR)) {
        storage.add(object);
      } else {
        throw new IDBPersistenceException("Connection to the DB has been lost");
      }
    }
  }

  /**
   * Stores a list of incoming objects into a db table
   *
   * @param data List of IFallback objects containing the data to be committed to the DB
   * @throws IDBPersistenceException An exception is thrown in case the objects cannot be committed to the DB
   */
  @Override
  public final void storeData(final List<IFallback> data) throws IDBPersistenceException {
    for (IFallback aData : data) {
      if (aData instanceof FallbackImpl) {
        if (!aData.toString().equals(FallbackImpl.ERROR)) {
          storage.add(aData);
        } else {
          throw new IDBPersistenceException("Connection to the DB has been lost", 0);
        }
      }
    }
  }
}
