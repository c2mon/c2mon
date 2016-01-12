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

/**
 * This listener interface is used by the C2MON SessionManager
 * to get informed about a user login or logout. 
 *
 * @author Matthias Braeger
 */
public interface AuthenticationListener {
  
  /**
   * This method is called whenever a user successfully 
   * managed to login.
   * @param userName The name of the user that logged in.
   */
  void onLogin(String userName);
  
  /**
   * This method is called when a previously logged user
   * successfully logged out.
   * 
   * @param userName The name of the user that has been logged out.
   */
  void onLogout(String userName);
}
