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
package cern.c2mon.client.auth.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cern.accsoft.security.rba.RBAToken;
import cern.accsoft.security.rba.TokenExpiredException;
import cern.accsoft.security.rba.TokenFormatException;
import cern.accsoft.security.rba.login.DefaultCallbackHandler;
import cern.accsoft.security.rba.login.LoginPolicy;
import cern.accsoft.security.rba.login.RBALoginContext;
import cern.c2mon.client.auth.AuthenticationListener;
import cern.c2mon.client.auth.AuthenticationManager;
import cern.rba.util.holder.ClientTierRbaTokenChangeListener;
import cern.rba.util.holder.ClientTierSubjectHolder;
import cern.rba.util.lookup.RbaTokenLookup;

/**
 * The RBAC implementation of the {@link AuthenticationManager}.
 *
 * @author Matthias Braeger
 */
@Service
public class RbacAuthenticationManager implements AuthenticationManager, ClientTierRbaTokenChangeListener {
  
  /** Log4j logger instance */
  private static final Logger LOG = Logger.getLogger(RbacAuthenticationManager.class);
  
  /** Used to synchonize login and logout requests */
  private static final Object SYNC_LOCK = new Object(); 
  
  /** The RBAC login context */
  private static volatile RBALoginContext ctx = null;
  
  /** The list of registered {@link AuthenticationListener} instances */
  private Set<AuthenticationListener> authenticationListeners =
    Collections.synchronizedSet(new HashSet<AuthenticationListener>());

  /**
   * Registering this class as {@link ClientTierRbaTokenChangeListener}
   */
  @PostConstruct
  protected void init() {
    ClientTierSubjectHolder.addRbaTokenChangeListener(this);
  }
  
  /**
   * This method is called by SPRING before this Service gets destroyed 
   */
  @PreDestroy
  protected void cleanup() {
    logout();
    authenticationListeners.clear();
  }
  
  @Override
  public boolean isUserLogged() {
    final RBAToken token = RbaTokenLookup.findClientTierRbaToken();
    return token != null && token.isValid();
  }

  @Override
  public String getUserName() {
    final RBAToken token = RbaTokenLookup.findClientTierRbaToken();
    if (token != null) {
      return token.getUser().getName();
    }
    
    return null;
  }

  /**
   * Executes a RBAC user login with the default application name
   * "c2mon-client-api"
   * @param userName The user name
   * @param userPassw The password of the user
   * @return <code>true</code>, if the authentication
   *         was successfull
   */
  @Override
  public boolean login(final String userName, final String userPassw) {
    return login("c2mon-client-api", userName, userPassw);
  }
  
  @Override
  public boolean login(final String appName, final String userName, final String userPassw) {
    synchronized (SYNC_LOCK) {
      boolean validSession = false;
      if (ctx == null) {
        try {
          ctx = new RBALoginContext(LoginPolicy.EXPLICIT,
              new DefaultCallbackHandler(appName, userName, userPassw.toCharArray()));
          ctx.login();
          validSession = ctx.getRBASubject().getAppToken().isValid();
        }
        catch (LoginException e) {
          LOG.info("login() - Login attempt for user " + userName + " failed. Reason: " + e.getMessage());
        }
      }
      
      return validSession;
    }
  }

  @Override
  public synchronized void logout() {
    synchronized (SYNC_LOCK) {
      if (ctx != null) {
        try {
          ctx.logout();
        }
        catch (LoginException e) {
          LOG.warn("User logout was unsuccessul. Reason: " + e.getMessage());
        }
        ctx = null;
      }
    }
  }

  @Override
  public void addAuthenticationListener(final AuthenticationListener listener) {
    if (listener != null && !authenticationListeners.contains(listener)) {
      authenticationListeners.add(listener);
    }
  }

  @Override
  public void removeAuthenticationListener(final AuthenticationListener listener) {
    if (listener != null) {
      authenticationListeners.remove(listener);
    }
  }

  /**
   * This listener method is called every time, when somebody is logging
   * in or out. The SessionManager will accordingly inform all registered
   * {@link SessionListener}.
   * 
   * @param rbaToken The RBAC token
   * @throws TokenFormatException In case of problems with the token.
   * @throws TokenExpiredException In case the token is already expired
   */
  @Override
  public void rbaTokenChanged(final RBAToken rbaToken) throws TokenFormatException, TokenExpiredException {
    if (rbaToken == null || rbaToken.isEmpty() || !rbaToken.isValid()) {
      // the user has logged out
      for (AuthenticationListener listener : authenticationListeners) {
        listener.onLogout();
      }
    }
    else {
      String userName = rbaToken.getUser().getName();
      for (AuthenticationListener listener : authenticationListeners) {
        listener.onLogin(userName);
      }
    }
  }
}
