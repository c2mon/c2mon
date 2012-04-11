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
package cern.c2mon.client.history;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryProviderFactory;
import cern.c2mon.client.common.history.SavedHistoryEvent;
import cern.c2mon.client.common.history.SavedHistoryEventsProvider;
import cern.c2mon.client.common.history.exception.HistoryProviderException;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.history.dbaccess.HistorySessionFactory;
import cern.c2mon.client.history.dbaccess.exceptions.HistoryException;

/**
 * Factory to get all types of {@link HistoryProvider}s
 * 
 * @author vdeila
 * 
 */
public class HistoryProviderFactoryImpl implements HistoryProviderFactory {

  /**
   * callback for the history provider to get access to attributes in
   *          the {@link ClientDataTagValue}. Like the
   *          {@link ClientDataTagValue#getType()}.
   */
  private final ClientDataTagRequestCallback clientDataTagRequestCallback;
  
  /**
   * Spring's application context 
   */
  private static ClassPathXmlApplicationContext appContext;
  
  /**
   * Spring's application context path
   */
  private static final String APPLICATION_CONTEXT_PATH = 
    "classpath:cern/c2mon/client/history/springConfig/spring-history.xml";
  
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
   *          the {@link ClientDataTagValue}. Like the
   *          {@link ClientDataTagValue#getType()}.
   */
  public HistoryProviderFactoryImpl(final ClientDataTagRequestCallback clientDataTagRequestCallback) {
    this.clientDataTagRequestCallback = clientDataTagRequestCallback;
    
    // TODO: This can be removed once this class is also maintained by SPRING
    this.historyFactory = (HistorySessionFactory) getApplicationContext().getBean("historyFactory");
  }
  
  /**
   * Spring's application context 
   * @return appContext
   */
  public static ClassPathXmlApplicationContext getApplicationContext() {

    appContext = 
      new ClassPathXmlApplicationContext(new String[] {
          APPLICATION_CONTEXT_PATH
      });
    
    return appContext;
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
