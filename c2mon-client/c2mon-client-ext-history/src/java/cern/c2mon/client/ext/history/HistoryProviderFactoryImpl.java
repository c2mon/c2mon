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
package cern.c2mon.client.ext.history;

import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.HistoryProviderFactory;
import cern.c2mon.client.ext.history.common.SavedHistoryEvent;
import cern.c2mon.client.ext.history.common.SavedHistoryEventsProvider;
import cern.c2mon.client.ext.history.common.exception.HistoryProviderException;
import cern.c2mon.client.ext.history.dbaccess.HistorySessionFactory;
import cern.c2mon.client.ext.history.dbaccess.exceptions.HistoryException;

/**
 * Factory to get all types of {@link HistoryProvider}s
 * 
 * @author vdeila
 */
public class HistoryProviderFactoryImpl implements HistoryProviderFactory {

  /**
   * callback for the history provider to get access to attributes in
   *          the {@link Tag}. Like the
   *          {@link Tag#getType()}.
   */
  private final ClientDataTagRequestCallback clientDataTagRequestCallback;
  
  @Autowired
  private HistorySessionFactory historyFactory;
  
  /**
   * Constructor
   */
  public HistoryProviderFactoryImpl() {
    this(null);
  }
  
  /**
   * 
   * @param clientDataTagRequestCallback
   *          callback for the history provider to get access to attributes in
   *          the {@link Tag}. Like the
   *          {@link Tag#getType()}.
   */
  public HistoryProviderFactoryImpl(final ClientDataTagRequestCallback clientDataTagRequestCallback) {
    this.clientDataTagRequestCallback = clientDataTagRequestCallback;
    
    this.historyFactory = (HistorySessionFactory)
        C2monHistoryGateway.context.getBean("historyFactory");
  }

  @Override
  public HistoryProvider createHistoryProvider() throws HistoryProviderException {
    try {
      return historyFactory.createHistoryProvider(this.clientDataTagRequestCallback);
    }
    catch (HistoryException e) {
      throw new HistoryProviderException("Could not get a history provider.", e);
    }
  }

  @Override
  public HistoryProvider createSavedHistoryProvider(final SavedHistoryEvent event) throws HistoryProviderException {
    try {
      return historyFactory.createSavedHistoryProvider(event, clientDataTagRequestCallback);
    }
    catch (HistoryException e) {
      throw new HistoryProviderException("Could not get a saved history provider.", e);
    }
  }

  @Override
  public SavedHistoryEventsProvider createSavedHistoryEventsProvider() throws HistoryProviderException {
    try {
      return historyFactory.createSavedHistoryEventsProvider();
    }
    catch (HistoryException e) {
      throw new HistoryProviderException("Could not get a saved history events provider.", e);
    }
  }
}
