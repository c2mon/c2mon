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

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.client.ext.history.ClientDataTagRequestCallback;
import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.SavedHistoryEvent;
import cern.c2mon.client.ext.history.common.SavedHistoryEventsProvider;
import cern.c2mon.client.ext.history.dbaccess.exceptions.HistoryException;

/**
 * Factory to retrieve a {@link SqlSessionFactory}, {@link SqlSession}
 * or a {@link HistoryProvider}.<br/>
 * 
 * @author vdeila
 */
public final class HistorySessionFactory {

  @Autowired
  private HistoryMapper historyMapper;
  
  @Autowired
  private SavedHistoryMapper savedHistoryMapper;
  
  @Autowired
  private SavedHistoryEventsMapper savedHistoryEventsMapper;
  

  /**
   * Is Singleton and therefore private
   */
  private HistorySessionFactory() {
  }

  /**
   * 
   * @param clientDataTagRequestCallback
   *          callback for the history provider to get access to attributes in
   *          the {@link Tag}. Like the
   *          {@link Tag#getType()}.
   * 
   * @return A {@link HistoryProvider} which can be used to easily get history
   *         data
   * @throws HistoryException
   *           If the configuration file could not be read. Or if the system
   *           properties for the data source is not set.
   */
  public HistoryProvider createHistoryProvider(final ClientDataTagRequestCallback clientDataTagRequestCallback) throws HistoryException {
    return new SqlHistoryProviderDAO(historyMapper, clientDataTagRequestCallback);
  }
  
  /**
   * 
   * @param event
   *          the event which will be requested. Can be <code>null</code>, but
   *          may decrease performance significantly
   * @param clientDataTagRequestCallback
   *          callback for the history provider to get access to attributes in
   *          the {@link Tag}. Like the
   *          {@link Tag#getType()}.
   * 
   * @return A {@link HistoryProvider} which can be used to easily get event
   *         history data
   * @throws HistoryException
   *           If the configuration file could not be read. Or if the system
   *           properties for the data source is not set.
   */
  public HistoryProvider createSavedHistoryProvider(final SavedHistoryEvent event, final ClientDataTagRequestCallback clientDataTagRequestCallback) throws HistoryException {
    return new SqlHistoryEventsProviderDAO(event, historyMapper, savedHistoryMapper, clientDataTagRequestCallback);
  }
  
  /**
   * 
   * @return A {@link SavedHistoryEventProvider} which can be used to easily get
   *         the list of saved history events
   * 
   * @throws HistoryException
   *           If the configuration file could not be read. Or if the system
   *           properties for the data source is not set.
   */
  public SavedHistoryEventsProvider createSavedHistoryEventsProvider()
      throws HistoryException {
    return new SqlSavedHistoryEventsProviderDAO(savedHistoryEventsMapper);
  }
  
  public HistoryMapper getHistoryMapper() {
    return historyMapper;
  }
  
  public SavedHistoryMapper getSavedHistoryMapper() {
    return savedHistoryMapper;
  }
  
  public SavedHistoryEventsMapper getSavedHistoryEventsMapper() {
    return savedHistoryEventsMapper;
  }
  
  public void setHistoryMapper(final HistoryMapper historyMapper) {
    this.historyMapper = historyMapper;
  }
  
  public void setSavedHistoryMapper(final SavedHistoryMapper savedHistoryMapper) {
    this.savedHistoryMapper = savedHistoryMapper;
  }
  
  public void setSavedHistoryEventsMapper(final SavedHistoryEventsMapper savedHistoryEventsMapper) {
    this.savedHistoryEventsMapper = savedHistoryEventsMapper;
  }
}
