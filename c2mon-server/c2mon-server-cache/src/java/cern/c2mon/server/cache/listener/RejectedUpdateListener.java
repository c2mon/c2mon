package cern.c2mon.server.cache.listener;

public interface RejectedUpdateListener {

  /**
   * 
   * @param object
   */
  void notifyUpdateRejected(Object object);
  
}
