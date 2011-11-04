package cern.c2mon.client.common.listener;

/**
 * Listener interface that is called whenever a user logs in or out
 *
 * @author Matthias Braeger
 */
public interface SessionListener  {

  public void onLogin(final String userName);

  public void onLogout();
}