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
package cern.c2mon.client.ext.rbac.impl;

import cern.accsoft.security.rba.RBAToken;

/**
 * This manager is responsible of finding a valid {@link RBAToken}
 * for a given user name. Please notice that this Interface is only
 * used internally by the {@link RbacAuthorizationManager}.
 *
 * @author Matthias Braeger
 */
interface RbaTokenManager {
  /**
   * Tries to find the {@link RBAToken} for a given user
   * @param userName The name of the user for which we want to 
   *                 have the {@link RBAToken}
   * @return The {@link RBAToken} of the user, or
   *         <code>null</code> if no token could be found.
   */
  RBAToken findRbaToken(String userName);
}
