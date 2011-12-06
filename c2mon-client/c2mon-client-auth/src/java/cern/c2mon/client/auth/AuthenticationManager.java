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
package cern.c2mon.client.auth;

import java.util.Set;

/**
 * The authentication manager interface provides
 * methods to handle the user's authentication.
 *
 * @author Matthias Braeger
 */
public interface AuthenticationManager {
  
  /**
   * Executes a user login
   * @param userName The user name
   * @param userPassw The password of the user
   * @return <code>true</code>, if the authentication
   *         was successfull
   */
  boolean login(String userName, String userPassw);
  
  /**
   * Executes a user login for a given application name
   * @param appName the name of the application from which the
   *                user wants to login.
   * @param userName The user name
   * @param userPassw The password of the user
   * @return <code>true</code>, if the authentication
   *         was successfull
   */
  boolean login(String appName, String userName, String userPassw);
  
  /**
   * Performs a user logout
   * @param userName The user to be logged out
   * @return <code>true</code>, if log out was unsuccessful
   */
  boolean logout(String userName);
  
  /**
   * Checks whether a given user is logged in.
   * @param userName The name of the user for which we want to check the
   *                 valid authentication.
   * @return <code>true</code>, if a user is logged.
   */
  boolean isUserLogged(String userName);
  
  /**
   * @return The name of the users which are currently
   *         logged in.
   */
  Set<String> getLoggedUserNames();
  
  /**
   * Registers an {@link AuthenticationListener} for getting a
   * notification whenever a user loggs in or out. 
   * @param listener The listener instance which shall be registered
   */
  void addAuthenticationListener(AuthenticationListener listener);
  
  /**
   * Removes an {@link AuthenticationListener} from the notification
   * list of the AuthenticationManager. 
   * @param listener The listener instance which shall be removed.
   */
  void removeAuthenticationListener(AuthenticationListener listener);
}
