///******************************************************************************
// * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
// *
// * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
// * C2MON is free software: you can redistribute it and/or modify it under the
// * terms of the GNU Lesser General Public License as published by the Free
// * Software Foundation, either version 3 of the license.
// *
// * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// * more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
// *****************************************************************************/
//package cern.c2mon.web.configviewer.security;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//
//import cern.c2mon.client.common.service.SessionService;
//import cern.c2mon.client.ext.rbac.C2monSessionGateway;
//import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
//
///** Our own custom authentication Provider. */
//public class CustomAuthenticationProvider  implements AuthenticationProvider  {
//
//  /** So that the SessionManager knows who we are : ) */
//  private final static String APP_NAME = "c2mon-web";
//
//  private RbacAuthorizationDetails adminAuthDetails;
//  private RbacAuthorizationDetails processViewerAuthDetails;
//
//  /**
//   * CustomAuthenticationProvider logger
//   */
//  private static Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);
//
//  /**
//   * Our own custom authentication method.
//   * @param authentication contains information about the current user (and his password).
//   * @return the result of our authentication attempt.
//   */
//  @Override
//  public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
//
//    String username = (String) authentication.getPrincipal();
//    String password = (String) authentication.getCredentials();
//
//    // Don't attempt to login the user if they are already logged in.
//    if (!C2monSessionGateway.getSessionService().isUserLogged(username)) {
//      if (!C2monSessionGateway.getSessionService().login(APP_NAME, username, password)) {
//        throw new BadCredentialsException("Invalid username/password");
//      }
//    } else {
//      logger.debug("Repeated login for user " + username);
//    }
//
//    String role = "ROLE_ADMIN";
//
//    Authentication customAuthentication =
//      new CustomUserAuthentication(role, authentication);
//
//    customAuthentication.setAuthenticated(true);
//
//    return customAuthentication;
//  }
//
//  /**
//   * Private helper method. Returns a role for the given username.
//   * @param username the username
//   * @return a role for the given username
//   */
//  private String getRole(final String username) {
//
//    String role = "ROLE_GUEST";
//
//    SessionService sessionManager = C2monSessionGateway.getSessionService();
//
//    if (sessionManager.isAuthorized(username, adminAuthDetails))
//      role = "ROLE_ADMIN";
//        else {
//          if (sessionManager.isAuthorized(username, processViewerAuthDetails)) {
//            role = "ROLE_PROCESS_VIEWER";
//          }
//        }
//
//    role = "ROLE_ADMIN";
//    return role;
//  }
//
//  @Override
//  public boolean supports(final Class< ? extends Object > authentication) {
//
//    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
//  }
//}
