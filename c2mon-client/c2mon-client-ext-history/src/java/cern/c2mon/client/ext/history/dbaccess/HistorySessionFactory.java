/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.ext.history.dbaccess;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientDataTagValue;
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
   *          the {@link ClientDataTagValue}. Like the
   *          {@link ClientDataTagValue#getType()}.
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
   *          the {@link ClientDataTagValue}. Like the
   *          {@link ClientDataTagValue#getType()}.
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
