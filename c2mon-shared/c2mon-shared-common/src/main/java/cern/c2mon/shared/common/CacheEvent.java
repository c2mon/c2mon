package cern.c2mon.shared.common;

/**
 * References to the various events that {@code CacheListener}s should be notified about
 * <p>
 * If you add more events, make sure to trigger them using the C2monCache and test for them in the listener tests!
 */
public enum CacheEvent {
  /**
   * A {@link Cacheable} was just put into the Cache for the first time. No item with this key
   * existed previously
   */
  INSERTED,
  /**
   * A {@link Cacheable} was updated in the cache. The new value may be equal to the previous one
   */
  UPDATE_ACCEPTED,
  /**
   * A {@link Cacheable} failed its {@link C2monCacheFlow#preInsertValidate(Cacheable,Cacheable)},
   * causing the Cache to reject it
   */
  UPDATE_REJECTED,
  /**
   * A {@link Cacheable} terminated unexpectedly during its update process
   */
  UPDATE_FAILED,
  /**
   * The status of the Supervised value changed from RUNNING to DOWN/STOPPED and vice-versa.
   */
  SUPERVISION_CHANGE,
  /**
   * A Supervised value was updated, though its status may not have changed
   *
   * @deprecated This is obsolete now that we can use {@link CacheEvent#UPDATE_ACCEPTED} instead
   */
  SUPERVISION_UPDATE,
  /**
   * A status check was requested. Used for instance to fix inconsistent state after crash
   */
  CONFIRM_STATUS
}
