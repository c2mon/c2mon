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

import cern.c2mon.shared.common.command.AuthorizationDetails;

/**
 * The Authorization Manager allows you to check whether the an
 * authenticated user is authorized for a given task.
 *
 * @author Matthias Braeger
 */
public interface AuthorizationManager {

  /**
   * Checks whether the logged in user is authorized against the given
   * authorization details.
   * 
   * @param userName The name of the user for which we have to check privileges
   * @param authorizationDetails The authorization details that shall be checked
   * @return <code>true</code>, if the logged in user is authorized for the
   *         given authorization details.
   * @throws RuntimeException If <code>authorizationDetails</code> cannot
   *         be cast into the supported authentication class, e.g.
   *         <code>RbacAuthorizationDetails</code>
   * @throws NullPointerException In case of a <code>null</code> parameter 
   */
  boolean isAuthorized(String userName, AuthorizationDetails authorizationDetails);
}
