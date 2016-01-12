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
