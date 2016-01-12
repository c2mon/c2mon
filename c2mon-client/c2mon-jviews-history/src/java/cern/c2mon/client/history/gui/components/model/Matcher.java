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
package cern.c2mon.client.history.gui.components.model;

/**
 * Used by {@link SearchListModel} to filter out and order a search in a list.
 * 
 * @see SearchListModel
 * 
 * @author vdeila
 * 
 * @param <T>
 *          the type of the search
 */
public interface Matcher<T> {

  /**
   * 
   * @param listObj
   *          the object from the list model
   * @param search
   *          the search object
   * @return a positive number if the search matches the listObj, the closer to
   *         <code>0</code> the better match. A negative number means it doesn't
   *         match at all.
   */
  public int matches(Object listObj, T search);

}
