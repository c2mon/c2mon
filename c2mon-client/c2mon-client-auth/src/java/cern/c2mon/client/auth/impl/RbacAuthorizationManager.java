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
package cern.c2mon.client.auth.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.accsoft.security.rba.RBAToken;
import cern.accsoft.security.rba.authorization.AccessChecker;
import cern.accsoft.security.rba.authorization.AccessException;
import cern.c2mon.client.auth.AuthorizationManager;
import cern.rba.util.lookup.RbaTokenLookup;
import cern.tim.shared.client.command.RbacAuthorizationDetails;
import cern.tim.shared.common.command.AuthorizationDetails;

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
