package cern.c2mon.client.auth.impl;

import org.springframework.stereotype.Service;

import cern.accsoft.security.rba.RBAToken;
import cern.rba.util.lookup.RbaTokenLookup;
import cern.accsoft.security.rba.authorization.AccessChecker;
import cern.accsoft.security.rba.authorization.AccessException;

import cern.c2mon.client.auth.AuthorizationManager;
import cern.tim.shared.client.command.RbacAuthorizationDetails;
import cern.tim.shared.common.command.AuthorizationDetails;

/**
 * This class implements the {@link AuthorizationManager} interface for RBAC authentication/authorization.
 * 
 * @author Matthias Braeger, Wojtek Buczak
 */
@Service
public class RbacAuthorizationManager implements AuthorizationManager {

    /**
     * Default Constructor
     */
    public RbacAuthorizationManager() {
        // do nothing
    }

    @Override
    public boolean isAuthorized(final AuthorizationDetails authorizationDetails) {
        RbacAuthorizationDetails rbacDetails;
        boolean result = false;

        if (authorizationDetails instanceof RbacAuthorizationDetails) {
            rbacDetails = (RbacAuthorizationDetails) authorizationDetails;
        } else {
            throw new RuntimeException("Incorrect authorization Manager loaded. Class "
                    + authorizationDetails.getClass() + " is not supported. Please get the latest JARs");
        }

        RBAToken token = RbaTokenLookup.findClientTierRbaToken();
        if (token == null) {
            result = false;
        } else if (token.isValid()) {
            try {
                result = new AccessChecker().isAuthorized(token, rbacDetails.getRbacClass(), rbacDetails
                        .getRbacDevice(), rbacDetails.getRbacProperty());
            } catch (AccessException e) {
                result = false;
            }
        }

        return result;
    }

    @Override
    public boolean isUserLogged() {
        final RBAToken token = RbaTokenLookup.findClientTierRbaToken();
        return token != null && token.isValid();
    }

    @Override
    public String getUserName() {
      final RBAToken token = RbaTokenLookup.findClientTierRbaToken();
      if (token != null) {
        return token.getUser().getName();
      }
      
      return null;
    }
}
