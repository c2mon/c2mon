package cern.c2mon.server.daqcommunication.out;

/**
 * Service for requesting refreshed data from the 
 * Data aquisition layer (synchronisation should 
 * usually be maintained, but provided as backup
 * refresh).
 * 
 * <p>In general, notice values only change in the cache
 * if the refresh detects fresh data.
 * 
 * @author Mark Brightwell
 *
 */
public interface DataRefreshManager {

  /**
   * Refreshes the values in the cache for a given DAQ Process (from DAQ cache).
   * @param id the id of the Process
   */
  void refreshValuesForProcess(Long id);

  /**
   * Refreshes the values of all DataTags in the system from 
   * the DAQ caches.
   */
  void refreshTagsForAllProcess();
  
}
