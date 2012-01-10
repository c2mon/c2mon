package cern.c2mon.web.configviewer.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monSessionManager;
import cern.tim.shared.client.command.RbacAuthorizationDetails;

public class RbacDecisionManager implements AccessDecisionManager {

  /** So that the SessionManager knows who we are : ) */
  private final String APP_NAME = "c2mon-web-configviewer";

  /** A map of (PageUrls, AuthorizationDetails) required to access each page. */
  private Map<String, String> authorizationDetails;

  @Autowired
  /**
   * 
   */
  public RbacDecisionManager(Map<String, String> authorizationDetails) {

    this.authorizationDetails = authorizationDetails;
  }


  /**
   * RbacDecisionManager logger
   * */
  private static Logger logger = Logger.getLogger(RbacDecisionManager.class);

  @Override
  public void decide(Authentication authentication, Object secureObject,
      Collection attributes) throws AccessDeniedException,
      InsufficientAuthenticationException {

    // The supports method ensures we are dealing with FilterInvocations
    //  so we can safely cast the secure object
    FilterInvocation invocation = (FilterInvocation) secureObject;
    WebAuthenticationDetails requestDetails = (WebAuthenticationDetails) 
    authentication.getDetails();
    
    // The url that the user tries to access
    String username = (String) authentication.getPrincipal();
    String pageUrl = invocation.getRequestUrl();
    logger.info(username + " tries to access url:" + pageUrl);

    C2monSessionManager sessionManager = C2monServiceGateway.getSessionManager();

    RbacAuthorizationDetails details = getRequiredPermissions(pageUrl);
    if (details == null) { // no special permissions required!
      logger.info("no special permissions required to access:" + pageUrl);
      return; // bye - bye!
    }
    
    if (!sessionManager.isAuthorized(username, details))  {
      logger.info(username +
          " tried to access:" + pageUrl + " but does not have permission to do so!");
      throw new AccessDeniedException("go away"); // user does not have permission!
    }
    
    logger.info(username + 
        " succesfully authorised to access:" + pageUrl);

    //    boolean authorized = true;
    //    if (!authorized) {
    //      throw new AccessDeniedException("go away");
    //    }
  }

  /**
   * Given a page Url return the RbacAuthorizationDetails required to access the Page.
   * @param pageUrl the page the user is trying to access.
   * @return the RbacAuthorizationDetails required to access the Page. Can be null
   * in case the page is open to everyone.
   */
  private RbacAuthorizationDetails getRequiredPermissions(final String pageUrl) {

    Iterable<String> i = authorizationDetails.keySet();

    for (String key : i) {

      if (pageUrl.contains(key)) {
        String stringEncodedAuthDetails = authorizationDetails.get(key);
        return splitDetails(stringEncodedAuthDetails);
      }
    }
    return null;
  }

  /**
   * RbacAuthorizationDetails are provided as a string for Convenience.
   * Given that string this method returns an RbacAuthorizationDetails Object.
   * @param stringEncodedAuthDetails a String that contains the RbacAuthorizationDetails
   * @return an RbacAuthorizationDetails Object
   */
  private RbacAuthorizationDetails splitDetails (final String stringEncodedAuthDetails) {

    String[] splitedDetails = stringEncodedAuthDetails.replace(" ", "").split( ",\\s*" ); // split on commas

    RbacAuthorizationDetails authDetails = null;

    if (splitedDetails.length < 3)
      logger.error(new Error("CustomAuthenticationProvider: error splitting Admin Details!:"
          + stringEncodedAuthDetails + ". Splitted in:" + splitedDetails
      ));

    if (splitedDetails.length == 3) {
      authDetails = new RbacAuthorizationDetails();
      authDetails.setRbacClass(splitedDetails[0]);
      authDetails.setRbacDevice(splitedDetails[1]);
      authDetails.setRbacProperty(splitedDetails[2]);
    }

    return authDetails;
  }

  public boolean supports(Class clazz) {
    // This manager should be used for FilterInvocations (authorizing web
    // requests)
    return FilterInvocation.class.isAssignableFrom(clazz);
  }

  public boolean supports(ConfigAttribute config) {
    return true;
  }
}
