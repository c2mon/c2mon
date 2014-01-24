package cern.c2mon.web.configviewer.security;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monSessionManager;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;

/** Our own custom authentication Provider. */
public class CustomAuthenticationProvider  implements AuthenticationProvider  {

  /** So that the SessionManager knows who we are : ) */
  private final String APP_NAME = "c2mon-web-configviewer";

  private RbacAuthorizationDetails adminAuthDetails;
  private RbacAuthorizationDetails processViewerAuthDetails;

  /**
   * CustomAuthenticationProvider logger
   * */
  private static Logger logger = Logger.getLogger(CustomAuthenticationProvider.class);

  /**
   * */
  @Autowired
  public CustomAuthenticationProvider() {
  }

  /**
   * Our own custom authentication method. 
   * @param authentication contains information about the current user (and his password).
   * @return the result of our authentication attempt. 
   */
  @Override
  public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

    String username = (String) authentication.getPrincipal();
    String password = (String) authentication.getCredentials();

    // try to login
    if (!C2monServiceGateway.getSessionManager().login(APP_NAME, username, password))
      throw new BadCredentialsException("Invalid username/password"); // failed to login

// login successful => check if Authorized
//    String role = getRole(username);
    String role = "ROLE_ADMIN";

    Authentication customAuthentication =
      new CustomUserAuthentication(role, authentication);
    
    customAuthentication.setAuthenticated(true);

    return customAuthentication;
  }

  /**
   * Private helper method. Returns a role for the given username.
   * @param username the username
   * @return a role for the given username
   */
  private String getRole(final String username) {

    String role = "ROLE_GUEST";

    C2monSessionManager sessionManager = C2monServiceGateway.getSessionManager();

    if (sessionManager.isAuthorized(username, adminAuthDetails))
      role = "ROLE_ADMIN";
        else {
          if (sessionManager.isAuthorized(username, processViewerAuthDetails)) {
            role = "ROLE_PROCESS_VIEWER";
          }
        }
    
    role = "ROLE_ADMIN";
    return role;
  }

  @Override
  public boolean supports(final Class< ? extends Object > authentication) {

    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}