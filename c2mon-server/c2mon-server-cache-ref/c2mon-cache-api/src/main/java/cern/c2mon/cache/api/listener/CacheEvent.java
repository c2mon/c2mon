package cern.c2mon.cache.api.listener;

/**
 * References to the various events that {@link CacheListener}s should be notified about
 * <p>
 * If you add more events, make sure to trigger them in C2monCache and test for them in the listener tests!
 */
public enum CacheEvent {
  /**
   * An object was updated in the cache. The new value may be equal to the previous one
   */
  UPDATE_ACCEPTED,
  // Previously existed, but was never used anywhere?
  @Deprecated
  UPDATE_REJECTED,
  /**
   * Called when the status of the DAQ/Equipment changes from
   * RUNNING to DOWN/STOPPED and vice-versa.
   *
   * @param tag a copy of the Tag with new status applied
   */
  SUPERVISION_CHANGE,
  SUPERVISION_UPDATE,
  CONFIRM_STATUS
}
