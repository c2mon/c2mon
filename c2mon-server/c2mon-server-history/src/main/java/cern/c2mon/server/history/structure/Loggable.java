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
package cern.c2mon.server.history.structure;

/**
 * Should be implemented by any object that needs logging to the history
 * database and wishing to use the default logging implementation. The
 * methods are only used for debug log messages.
 *
 * @author Mark Brightwell
 *
 */
public interface Loggable {

  /**
   * Returns the id of the object to be logged.
   * @return the id as String
   */
  String getId();

  /**
   * Returns the value of the object to be logged
   * @return the value as String
   */
  String getValue();

}
