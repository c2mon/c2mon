package cern.c2mon.server.cache;

import java.util.Collection;

/**
 * C2monCacheListener version that expects collections rather than
 * single cache objects. Can specify the type expected.
 * 
 * @author Mark Brightwell
 * @param <S> the type of collection the listener is expecting
 *
 */
public interface BufferedTimCacheListener<S> {

  /**
   * Callback when a cache object is modified. Only the key is passed (the listener
   * may access the cache to get the latest values for instance). 
   * 
   * @param collection keys of the objects that have been updated
   * 
   */
  void notifyElementUpdated(Collection<S> collection);
  
  /**
   * Callback used for confirming the value of the caches object. This is
   * used in particular during a system recovery after a crash. Guaranteed
   * actions should be performed, however this call will often duplicate
   * a previous notifyElementUpdated call.
   * 
   * @param eventCollection keys of the cache objects
   */
  void confirmStatus(Collection<S> eventCollection);
}
