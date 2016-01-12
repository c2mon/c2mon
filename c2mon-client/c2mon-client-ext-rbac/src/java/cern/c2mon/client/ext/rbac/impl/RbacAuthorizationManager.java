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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.accsoft.security.rba.RBAToken;
import cern.accsoft.security.rba.authorization.AccessChecker;
import cern.accsoft.security.rba.authorization.AccessException;
import cern.c2mon.client.ext.rbac.AuthorizationManager;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.command.AuthorizationDetails;

/**
 * This class implements the {@link AuthorizationManager} interface for RBAC authentication/authorization.
 * 
 * @author Matthias Braeger, Wojtek Buczak
 */
@Service
public class RbacAuthorizationManager implements AuthorizationManager {

    /** The token manager */
    private final RbaTokenManager tokenManager;
  
    /**
     * Default Constructor
     * @param pTokenManager The token manager which is needed to get the
     *                      token for a given user.
     */
    @Autowired
    public RbacAuthorizationManager(final RbaTokenManager pTokenManager) {
        tokenManager = pTokenManager;
    }

    @Override
    public boolean isAuthorized(final String userName, final AuthorizationDetails authorizationDetails) {
        RbacAuthorizationDetails rbacDetails;
        boolean result = false;

        if (authorizationDetails instanceof RbacAuthorizationDetails) {
            rbacDetails = (RbacAuthorizationDetails) authorizationDetails;
        } else {
            throw new RuntimeException("Incorrect authorization Manager loaded. Class "
                    + authorizationDetails.getClass() + " is not supported. Please get the latest JARs");
        }

        if (userName != null) {
          RBAToken token = tokenManager.findRbaToken(userName);
          if (token == null) {
              result = false;
          } else if (token.isValid()) {
              try {
                  result = new AccessChecker().isAuthorized(token, rbacDetails.getRbacClass(), rbacDetails
                          .getRbacDevice(), rbacDetails.getRbacProperty(), "set");
              } catch (AccessException e) {
                  result = false;
              }
          }
        }

        return result;
    }
}
