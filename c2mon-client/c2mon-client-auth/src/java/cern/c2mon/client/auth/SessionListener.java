package cern.c2mon.client.auth;

import cern.tim.shared.client.auth.SessionInfo;

public interface SessionListener  {

  public void onLogin(SessionInfo pSessionInfo);

  public void onLogout(SessionInfo pSessionInfo);

  public void onSuspend(SessionInfo pSessionInfo, boolean isSuspended);

}