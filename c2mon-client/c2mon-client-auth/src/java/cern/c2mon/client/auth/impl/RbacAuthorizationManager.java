package cern.c2mon.client.auth.impl;

import org.springframework.stereotype.Service;

import cern.accsoft.security.rba.util.gui.RBAIntegrator;
import cern.c2mon.client.auth.AuthorizationManager;
import cern.tim.shared.client.command.RbacAuthorizationDetails;
import cern.tim.shared.common.command.AuthorizationDetails;

/**
 * This class implements the {@link AuthorizationManager} interface
 * for RBAC authentication/authorization.
 *
 * @author Matthias Braeger
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
    if (authorizationDetails instanceof RbacAuthorizationDetails) {
      rbacDetails = (RbacAuthorizationDetails) authorizationDetails;
    }
    else {
      throw new RuntimeException("Incorrect authorization Manager loaded. Class "
          + authorizationDetails.getClass()
          + " is not supported. Please get the latest JARs");
    }              

    final RBAIntegrator rba = RBAIntegrator.getInstance();
    if (rba.isUserLogged()) {
      return rba.isAuthorized(rbacDetails.getRbacClass(), rbacDetails.getRbacDevice(), rbacDetails.getRbacProperty());
    }
    else { 
      return false;
    }
  }

  @Override
  public boolean isUserLogged() {
    final RBAIntegrator rba = RBAIntegrator.getInstance();
    return rba.isUserLogged();
  }
}
