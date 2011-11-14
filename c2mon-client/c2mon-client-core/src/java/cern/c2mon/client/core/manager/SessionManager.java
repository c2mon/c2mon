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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cern.accsoft.security.rba.RBAToken;
import cern.accsoft.security.rba.TokenExpiredException;
import cern.accsoft.security.rba.TokenFormatException;
import cern.c2mon.client.auth.AuthorizationManager;
import cern.c2mon.client.common.listener.SessionListener;
import cern.c2mon.client.core.C2monSessionManager;
import cern.rba.util.holder.ClientTierRbaTokenChangeListener;
import cern.rba.util.holder.ClientTierSubjectHolder;

/**
 * The session manager handles the user authentication and allows registering SessionListener.
 * 
 * @author Matthias Braeger
 */
@Service
public class SessionManager implements C2monSessionManager, ClientTierRbaTokenChangeListener {

    /** Log4j instance */
    private static final Logger LOG = Logger.getLogger(SessionManager.class);

    /**
     * Collection of listeners that will be notified whenever a login/logout action completes successfully.
     */
    private Collection<SessionListener> sessionListeners = new ArrayList<SessionListener>();

    /** The authorization manager */
    private final AuthorizationManager authorizationManager;

    /**
     * Default Constructor
     * 
     * @param pAuthorizationManager The authorization manager to use
     */
    @Autowired
    public SessionManager(final AuthorizationManager pAuthorizationManager) {
        this.authorizationManager = pAuthorizationManager;
        ClientTierSubjectHolder.addRbaTokenChangeListener(this);
    }

    @Override
    public void addSessionListener(final SessionListener pListener) {
        if (pListener != null && !sessionListeners.contains(pListener)) {
            sessionListeners.add(pListener);
        }
    }

    @Override
    public boolean login(@SuppressWarnings("unused") final String pUserName,
            @SuppressWarnings("unused") final String pPassword) {
        throw new UnsupportedOperationException("This method is not supported, yet");
    }

    @Override
    public boolean logout() {
        throw new UnsupportedOperationException("This method is not supported, yet");
    }

    @Override
    public void removeSessionListener(final SessionListener pListener) {
        if (pListener != null) {
            sessionListeners.remove(pListener);
        }
    }

    @Override
    public boolean isUserLogged() {
        return authorizationManager.isUserLogged();
    }

    @Override
    public String getUserName() {
        return authorizationManager.getUserName();
    }

    /**
     * This listener method is called every time, when somebody is logging
     * in or out. The SessionManager will accordingly inform all registered
     * {@link SessionListener}.
     * 
     * @param rbaToken The RBAC token
     * @throws TokenFormatException In case of problems with the token.
     */
    @Override
    public void rbaTokenChanged(final RBAToken rbaToken) throws TokenFormatException, TokenExpiredException {
        if (rbaToken == null || rbaToken.isEmpty() || !rbaToken.isValid()) {
            // the user has logged out
            for (SessionListener listener : sessionListeners) {
                listener.onLogout();
            }
        } else {
            String userName = rbaToken.getUser().getName();
            for (SessionListener listener : sessionListeners) {
                listener.onLogin(userName);
            }
        }
    }
}
