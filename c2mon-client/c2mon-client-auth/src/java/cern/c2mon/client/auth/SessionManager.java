package cern.c2mon.client.auth;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import cern.tim.shared.client.auth.SessionInfo;

/**
 * Generic session manager.
 * 
 * This class implements a generic session manager. It offers a framework for 
 * the following functionality:
 * (1) authentication: login and logout
 * (2) authorisation management: checking whether an authenticated user has
 *     a certain privilege.
 *
 */
public abstract class SessionManager  {

  private static final Logger LOG = Logger.getLogger(SessionManager.class);

  /**
   * Singleton instance of a SessionManager implementation.
   */
  protected static SessionManager instance = null;

  /**
   * Information about the current session.
   */
  private SessionInfo currentSession = null;

  /**
   * Collection of listeners that will be notified whenever a login/logout
   * action completes successfully.
   */
  private ArrayList sessionListeners = new ArrayList();


  /**
   * The initialise method must be called before the SessionManager can be used.
   */
  public static void initialise(SessionManager pInstance) {
    /*
    if (SessionManager.instance == null) {
      SessionManager.instance = pInstance;
      Runtime.getRuntime().addShutdownHook(
        new Thread() {
          public void run() {
            instance.logoutNoNotify();
          }      
        }
      );
    }
    */
  }



  /**
   * Check wether the SessionManager has been initialised.
   */
  public static boolean isInitialised() {
    return (SessionManager.instance != null);
  }

  public static SessionManager getInstance(boolean async) {
    if (instance == null) {
//      TODO: Write TimSessionManager
//      instance = TimSessionManager.getInstance(async);
      Runtime.getRuntime().addShutdownHook(
        new Thread() {
          public void run() {
            instance.logoutNoNotify();
          }      
        }
      );
    }
    return SessionManager.instance;
  }
  /**
   * Get the singleton instance of the SessionManager
   */
  public static SessionManager getInstance() {
    return getInstance(true);
  }


  public final SessionInfo login(final String pUserName, final String pPassword) {
    SessionInfo result = executeLogin(pUserName, pPassword);
    if (result != null) {
      if (isLoggedIn()) {
        logout();
      }
      this.currentSession = result;

      // notify listeners
      int len = this.sessionListeners.size();
      for (int i= 0; i != len; i++) {
        ((SessionListener)this.sessionListeners.get(i)).onLogin(result);
      }
    }
    return result;
  }
    
  protected abstract SessionInfo executeLogin(final String pUserName, final String pPassword);

  private final void logoutNoNotify() {
    if (this.currentSession != null) {
      if (executeLogout(this.currentSession)) {
        // unset current session
        this.currentSession = null;
      }
    }
  }

  public final boolean logout() {
    if (this.currentSession != null) {
      if (executeLogout(this.currentSession)) {
        // notify listeners
        int len = this.sessionListeners.size();
        for (int i= 0; i != len; i++) {
          ((SessionListener)this.sessionListeners.get(i)).onLogout(this.currentSession);
        }
        // unset current session
        this.currentSession = null;
        return true;
      }
      return false;
    }
    else {
      return true;
    }
  }

  protected abstract boolean executeLogout(SessionInfo pSessionInfo);

  public final SessionInfo getSessionInfo() {
    return this.currentSession;
  }

  public final boolean isLoggedIn() {
    return (this.currentSession != null && this.currentSession.isValidSession());
  }

  public final boolean checkPrivilege(final String pPrivilegeName) {
    if (isLoggedIn()) {
      return executeCheckPrivilege(this.currentSession, pPrivilegeName);
    }
    else {
      return false;
    }
  }

  public void addSessionListener(SessionListener pListener) {
    if (pListener != null) {
      this.sessionListeners.add(pListener);
    }
  }


  public void removeSessionListener(SessionListener pListener) {
    if (pListener != null) {
      this.sessionListeners.remove(pListener);
    }
  }

  protected abstract boolean executeCheckPrivilege(final SessionInfo pCurrentSession, final String pPrivilegeName);

  public void suspendSession(boolean isSuspended) {  
    LOG.debug("entering suspendSession()..");
    int len = this.sessionListeners.size();
    LOG.debug("notifying session listeners (number of listeners: "+len);
    for (int i= 0; i != len; i++) {  
      ((SessionListener)this.sessionListeners.get(i)).onSuspend(this.currentSession, isSuspended);
    }
    LOG.debug("leaving suspendSession()");
  }

}