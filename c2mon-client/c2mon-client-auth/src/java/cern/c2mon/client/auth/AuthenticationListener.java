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
