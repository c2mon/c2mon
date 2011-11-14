package cern.c2mon.client.auth;

import cern.tim.shared.common.command.AuthorizationDetails;

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
   * @param authorizationDetails The authorization details that shall be checked
   * @return <code>true</code>, if the logged in user is authorized for the
   *         given authorization details.
   * @throws RuntimeException If <code>authorizationDetails</code> cannot
   *         be cast into the supported authentication class, e.g.
   *         <code>RbacAuthorizationDetails</code>
   * @throws NullPointerException In case of a <code>null</code> parameter 
   */
  boolean isAuthorized(AuthorizationDetails authorizationDetails);
  
  /**
   * @return <code>true</code>, if a user is logged in, otherwise <code>false</code>
   */
  boolean isUserLogged();
  
  /**
   * @return The user name, or <code>null</code>, if no user is logged in.
   */
  String getUserName();
}
