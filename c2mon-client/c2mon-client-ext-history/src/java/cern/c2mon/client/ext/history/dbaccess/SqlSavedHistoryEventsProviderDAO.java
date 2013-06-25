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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.client.ext.history.common.SavedHistoryEvent;
import cern.c2mon.client.ext.history.common.SavedHistoryEventsProvider;
import cern.c2mon.client.ext.history.dbaccess.beans.SavedHistoryEventBean;

/**
 * 
 * Implementation of the {@link SavedHistoryEventsProvider}<br/>
 * <br/>
 * Gets the list of saved history events
 * 
 * @author vdeila
 * 
 */
public class SqlSavedHistoryEventsProviderDAO implements SavedHistoryEventsProvider {
  
  /** iBatis mapper for history DB access. */
  private SavedHistoryEventsMapper savedHistoryEventsMapper;

  /**
   * @param savedHistoryEventsMapper iBatis mapper for history DB access
   */
  public SqlSavedHistoryEventsProviderDAO(final SavedHistoryEventsMapper savedHistoryEventsMapper) {
  
    this.savedHistoryEventsMapper = savedHistoryEventsMapper;
  }
  
  /**
   * @return a saved history events mapper
   */
  private SavedHistoryEventsMapper getSavedHistoryEventsMapper() {
    
    return savedHistoryEventsMapper;
  }

  @Override
  public Collection<SavedHistoryEvent> getSavedHistoryEvents() {
    final ArrayList<SavedHistoryEventBean> events;
   
    events = new ArrayList<SavedHistoryEventBean>(getSavedHistoryEventsMapper().getSavedEvents());
    
    // Converting dates into local time zone
    for (SavedHistoryEventBean bean : events) {
      bean.convertIntoLocalTimeZone();
    }
    // Converting the list into a list of SavedHistoryEvent
    return new ArrayList<SavedHistoryEvent>(events);
  }

}
