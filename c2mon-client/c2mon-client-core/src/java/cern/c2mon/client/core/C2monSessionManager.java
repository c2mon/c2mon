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
package cern.c2mon.client.core;

import cern.c2mon.client.auth.SessionListener;
import cern.tim.shared.client.auth.SessionInfo;

/**
 * This interface describes the methods which are provided by
 * the C2MON session manager singleton. The session manager
 * handles the user authentication and allows registering
 * <code>SessionListener</code>.
 *
 * @author Matthias Braeger
 */
public interface C2monSessionManager {
  /**
   * Registers a listener for receiving event updates from the
   * <code>SessionManager</code>.
   * 
   * @param pListener The listener instance to register
   */
  void addSessionListener(final SessionListener pListener);

  /**
   * Removes the given listener from the <code>SessionManager</code>.
   * 
   * @param pListener The listener instance to remove
   */
  void removeSessionListener(final SessionListener pListener);

  /**
   * Use this message to authenticate the TIM client instance at the server with
   * a given user name and password
   * 
   * @param pUserName The user name
   * @param pPassword The password of the user
   * @return A <code>SessionInfo</code> that informs you whether the
   *         authentication was successful or not.
   */
  SessionInfo login(final String pUserName, final String pPassword);

  /**
   * Closes the current session.
   * 
   * @return true, if the logout was successful
   */
  boolean logout();
  
  /**
   * @return The information about the current session, or
   *         <code>null</code>, if user is not logged in.
   */
  SessionInfo getSessionInfo();
}
