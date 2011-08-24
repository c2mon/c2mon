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

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryProviderType;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.history.dbaccess.HistorySessionFactory;
import cern.c2mon.client.history.dbaccess.exceptions.HistoryException;

/**
 * Factory to get all types of {@link HistoryProvider}s
 * 
 * @author vdeila
 * 
 */
public final class HistoryProviderFactory {

  /** Singleton instance */
  private static HistoryProviderFactory instance = null;

  /**
   * @return An instance
   */
  public static HistoryProviderFactory getInstance() {
    if (instance == null) {
      instance = new HistoryProviderFactory();
    }
    return instance;
  }

  /**
   * 
   * @param type
   *          The type of HistoryProvider that should be created
   * @param clientDataTagRequestCallback
   *          callback for the history provider to get access to attributes in
   *          the {@link ClientDataTagValue}. Like the
   *          {@link ClientDataTagValue#getType()}.
   * @return A provider
   * @throws HistoryException
   *           If the provider could not be retrieved
   */
  public HistoryProvider createHistoryProvider(final HistoryProviderType type, final ClientDataTagRequestCallback clientDataTagRequestCallback) throws HistoryException {
    switch (type) {
    case HISTORY_EVENTS:
      return HistorySessionFactory.getInstance().createHistoryEventsProvider(clientDataTagRequestCallback);
    case HISTORY_SHORT_TERM_LOG:
      return HistorySessionFactory.getInstance().createHistoryProvider(clientDataTagRequestCallback);
    default:
      throw new HistoryException("Invalid HistoryProviderType");
    }
  }
}
