package cern.c2mon.client.auth.impl;

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
