package cern.c2mon.client.common.listener;

/**
 * Listener interface that is called whenever a user logs in or out
 *
 * @author Matthias Braeger
 */
public interface SessionListener  {

  /**
   * This method gets called whenever a user has logged in.
   * @param userName The name of the user that has logged in.
   */
  void onLogin(final String userName);

  /**
   * This method gets called whenever a user has logged out.
   * @param userName The name of the user that has logged out.
   */
  void onLogout(final String userName);
}
