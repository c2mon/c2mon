/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
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
 ******************************************************************************/
package cern.c2mon.client.core.manager;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.C2monHistoryManager;
import cern.c2mon.client.core.cache.BasicCacheHandler;
import cern.c2mon.client.core.listener.TagSubscriptionListener;

@Service
public class HistoryManager implements C2monHistoryManager, TagSubscriptionListener {

  /** Reference to the <code>TagManager</code> singleton */
  private final CoreTagManager tagManager;
  
  /** Reference to the <code>ClientDataTagCache</code> */
  private final BasicCacheHandler cache;
  
  @Autowired
  protected HistoryManager(final CoreTagManager pTagManager, final BasicCacheHandler pCache) {
    this.tagManager = pTagManager;
    this.cache = pCache;
  }
  
  /**
   * Inner method to initialize the STL database connection.
   */
  private void init() {
    
  }

  @Override
  public void startHistoryPlayerMode() {
    tagManager.addTagSubscriptionListener(this);
    
  }

  @Override
  public void stopHistoryPlayerMode() {
    tagManager.removeTagSubscriptionListener(this);
    
  }

  @Override
  public void onNewTagSubscriptions(final Set<Long> tagIds) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onUnsubscribe(final Set<Long> tagIds) {
    // TODO Auto-generated method stub
    
  }
}
