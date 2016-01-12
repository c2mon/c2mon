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
package cern.c2mon.client.ext.history.dbaccess;

import java.util.List;

import cern.c2mon.client.ext.history.dbaccess.beans.SavedHistoryEventBean;

/**
 * Used to get the list of saved events from the sql server. Instantiated by
 * iBatis or by {@link HistorySessionFactory}
 * 
 * @see HistorySessionFactory
 * @see SavedHistoryEventBean
 * 
 * @author vdeila
 * 
 */
public interface SavedHistoryEventsMapper {

  /**
   * 
   * @return a list of all the saved events stored in the database
   */
  List<SavedHistoryEventBean> getSavedEvents();

}
