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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cern.c2mon.client.auth.SessionListener;
import cern.c2mon.client.core.C2monSessionManager;
import cern.tim.shared.client.auth.SessionInfo;

@Service
public class SessionManager implements C2monSessionManager {

  /** Log4j instance */
  private static final Logger LOG = Logger.getLogger(SessionManager.class);

  /**
   * Information about the current session.
   */
  private SessionInfo currentSession = null;

  /**
   * Collection of listeners that will be notified whenever a login/logout
   * action completes successfully.
   */
  private Collection<SessionListener> sessionListeners = new ArrayList<SessionListener>();
  
  @Override
  public void addSessionListener(SessionListener pListener) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public SessionInfo login(String pUserName, String pPassword) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean logout() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void removeSessionListener(SessionListener pListener) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public SessionInfo getSessionInfo() {
    return currentSession;
  }
  
  
}
