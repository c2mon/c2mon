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
package cern.c2mon.server.history.mapper;

import cern.c2mon.pmanager.IFallback;

/**
 * Mapper interface that must be implemented for objects
 * wishing to use the default logging implementation.
 *
 * @author Mark Brightwell
 *
 * @param <T> the class that is to be logged
 */
public interface LoggerMapper<T extends IFallback> {

  /**
   * Inserts the Loggable object into the table.
   *
   * @param loggable the object to log in the DB
   */
  void insertLog(T loggable);

}
