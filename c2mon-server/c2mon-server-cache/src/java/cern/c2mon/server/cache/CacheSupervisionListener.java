package cern.c2mon.server.cache;

import cern.c2mon.server.common.tag.Tag;

/**
 * Implemented by classes wishing to receive
 * supervision invalidation/validation callbacks
 * on <b>all</b> tags on supervision status changes
 * of DAQs or Equipment.
 * 
 * <p>Prefer the use of a SupervisionListener when
 * a single notification for the DAQ/Equipment is
 * feasible.
 * 
 * <p>Callbacks are only made when a Process/Equipment
 * moves from running to down or vice-versa.
 * 
 * @author Mark Brightwell
 * @param <T> the type in the cache
 *
 */
public interface CacheSupervisionListener<T extends Tag> {

  /**
   * Called when the status of the DAQ/Equipment changes from
   * RUNNING to DOWN/STOPPED and vice-versa.
   * 
   * <p>Is called within a lock on the Tag (in the cache, not
   * the passed parameter).
   * 
   * @param tag a copy of the Tag with new status applied
   */
  void onSupervisionChange(T tag);
  
}
