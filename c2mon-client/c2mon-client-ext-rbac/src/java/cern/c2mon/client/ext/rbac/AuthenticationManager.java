/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.ext.rbac;

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
