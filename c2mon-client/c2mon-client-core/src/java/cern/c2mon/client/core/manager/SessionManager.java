/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.core.manager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.auth.AuthenticationListener;
import cern.c2mon.client.auth.AuthenticationManager;
import cern.c2mon.client.auth.AuthorizationManager;
import cern.c2mon.client.common.listener.SessionListener;
import cern.c2mon.client.core.C2monSessionManager;
import cern.tim.shared.common.command.AuthorizationDetails;

/**
 * The session manager handles the user authentication and allows registering SessionListener.
 * 
 * @author Matthias Braeger
 */
@Service
public class SessionManager implements C2monSessionManager, AuthenticationListener {

    /** Log4j instance */
    private static final Logger LOG = Logger.getLogger(SessionManager.class);

    /**
     * Collection of listeners that will be notified whenever a login/logout action completes successfully.
     */
    private Set<SessionListener> sessionListeners = Collections.synchronizedSet(new HashSet<SessionListener>());

    /** The authentication manager */
    private final AuthenticationManager authenticationManager;
    
    /** The authorization manager */
    private final AuthorizationManager authorizationManager;

    /**
     * Default Constructor
     * 
     * @param pAuthenticationManager Reference to the authentication manager
     * @param pAuthorizationManager The authorization manager to use
     */
    @Autowired
    public SessionManager(final AuthenticationManager pAuthenticationManager, 
                          final AuthorizationManager pAuthorizationManager) {
        this.authenticationManager = pAuthenticationManager;
        this.authorizationManager = pAuthorizationManager;
    }
    
    /**
     * Registeres this class as {@link AuthenticationListener}
     */
    @PostConstruct
    protected void init() {
      authenticationManager.addAuthenticationListener(this);
    }
    
    /**
     * Removes the sessionListener from the list.
     */
    @PreDestroy
    protected void cleanup() {
      sessionListeners.clear();
    }

    @Override
    public void addSessionListener(final SessionListener pListener) {
      if (pListener != null && !sessionListeners.contains(pListener)) {
          sessionListeners.add(pListener);
      }
    }

    /**
     * Use this message to authenticate with
     * a given user name and password. The {@link SessionManager} will then 
     * use your (valid) session for all authorization checks. <p>
     * Please notice that when using RBAC the mandatory 
     * application name parameter is set to <code>c2mon-client-api</code>.
     * 
     * @param pUserName The user name
     * @param pPassword The password of the user
     * @return <code>true</code>, if the authentication was successful.
     */
    @Override
    public boolean login(final String pUserName, final String pPassword) {
      return authenticationManager.login(pUserName, pPassword);
    }
    
    @Override
    public boolean login(final String appName, final String pUserName, final String pPassword) {
      return authenticationManager.login(appName, pUserName, pPassword);
    }

    @Override
    public boolean logout(final String userName) {
      return authenticationManager.logout(userName);
    }

    @Override
    public void removeSessionListener(final SessionListener pListener) {
      if (pListener != null) {
          sessionListeners.remove(pListener);
      }
    }

    @Override
    public boolean isUserLogged(final String userName) {
      return authenticationManager.isUserLogged(userName);
    }

    @Override
    public Set<String> getLoggedUserNames() {
      return authenticationManager.getLoggedUserNames();
    }
    
    @Override
    public boolean isAuthorized(final String userName, final AuthorizationDetails authorizationDetails) {
      if (authenticationManager.isUserLogged(userName)) {
        return authorizationManager.isAuthorized(userName, authorizationDetails);
      }
      else {
        return false;
      }
    }

    @Override
    public void onLogin(final String userName) {
      for (SessionListener listener : sessionListeners) {
        listener.onLogin(userName);
      }
    }

    @Override
    public void onLogout(final String userName) {
      for (SessionListener listener : sessionListeners) {
        listener.onLogout(userName);
      }
    }

    @Override
    public boolean isAnyUserLogged() {
      for (String user : authenticationManager.getLoggedUserNames()) {
        if (isUserLogged(user)) {
          return true;
        }
      }
      
      return false;
    }
}
