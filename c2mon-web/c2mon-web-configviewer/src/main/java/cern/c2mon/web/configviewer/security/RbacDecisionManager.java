package cern.c2mon.web.configviewer.security;

import cern.c2mon.client.common.service.SessionService;
import cern.c2mon.client.common.util.RbacAuthorizationDetailsParser;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Decides whether the current user has enough permissions
 * to access a page or not.
 *
 * @author ekoufaki
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
public class RbacDecisionManager implements AccessDecisionManager {

  /**
   * A map of (PageUrls, AuthorizationDetails) required to access each page.
   */
  private Map<String, String> authorizationDetails;

  @Autowired
  private SessionService sessionService;

  /**
   * Decides whether the current user has enough permissions
   * to access a page or not.
   *
   * @param authorizationDetails A map of (PageUrls, AuthorizationDetails) required to access each page.
   *                             The AuthorizationDetails should be provided as 3 comma seperated strings
   *                             in the following order: "Class,Device,Property"
   *                             Example: "TIM_APPLICATIONS,TIM_WEBCONFIG,RUN"
   */
  @Autowired
  public RbacDecisionManager(final Map<String, String> authorizationDetails) {
    this.authorizationDetails = authorizationDetails;
  }

  @Override
  public void decide(final Authentication authentication, final Object secureObject, final Collection attributes)
      throws AccessDeniedException, InsufficientAuthenticationException {

    // The supports method ensures we are dealing with FilterInvocations
    //  so we can safely cast the secure object
    FilterInvocation invocation = (FilterInvocation) secureObject;

    // The url that the user tries to access
    String username = (String) authentication.getPrincipal();
    String pageUrl = invocation.getRequestUrl();
    log.debug(username + " tries to access url:" + pageUrl);

    RbacAuthorizationDetails details = getRequiredPermissions(pageUrl);
    if (details == null) { // no special permissions required!
      log.debug("no special permissions required to access: " + pageUrl);
      return;
    }

    if (!sessionService.isUserLogged(username)) {
      log.debug(username + " tried to access: " + pageUrl + " but is not logged in.");
      authentication.setAuthenticated(false);
      throw new AccessDeniedException("User not logged in");

    } else {
      if (!sessionService.isAuthorized(username, details)) {
        log.debug(username + " tried to access: " + pageUrl + " but does not have permission.");
        throw new AccessDeniedException("Permission denied");
      }
    }

    log.info(username + " successfully authorised to access: " + pageUrl);
  }

  /**
   * Given a page Url return the RbacAuthorizationDetails required to access the Page.
   *
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
   *
   * @param stringEncodedAuthDetails a String that contains the RbacAuthorizationDetails
   * @return an RbacAuthorizationDetails Object
   */
  private RbacAuthorizationDetails splitDetails(final String stringEncodedAuthDetails) {

    RbacAuthorizationDetails details;
    try {
      details = RbacAuthorizationDetailsParser.parseRbacDetails(stringEncodedAuthDetails);
    } catch (IOException e) {
      throw new AccessDeniedException("Not able to fetch RbacAuthorizationDetails. Access has been denied.");
    }

    return details;
  }

  @Override
  public boolean supports(Class clazz) {
    // This manager should be used for FilterInvocations (authorizing web
    // requests)
    return FilterInvocation.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean supports(ConfigAttribute config) {
    return true;
  }
}
