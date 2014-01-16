package cern.c2mon.server.cache.datatag;

import java.sql.Timestamp;

import cern.c2mon.server.cache.tag.CommonTagObjectFacade;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagQuality;

  /**
   * Interface specifying the public methods of the bean managing updates to DataTagCacheObject's.
   * The methods should only be concerned with the actual update procedure to the cache object. More
   * complicated logic should be contained in the DataTagFacade object, such as managing the listener
   * notification.
   * 
   * @author Mark Brightwlel
   *
   */
  public interface DataTagCacheObjectFacade extends CommonTagObjectFacade<DataTag> {
  
  
  /**
   * Update the DataTag object held in the cache with the provided values.
   *  
   * @param dataTag reference to the tag in the cache
   * @param value the new value (Boolean, Float, etc.)
   * @param valueDesc free-text description of the tag's value
   * @param sourceTimestamp source timestamp *of the current value*
   * @param daqTimestamp timestamp when *the current value* is processed by the DAQ
   * @param cacheTimestamp the time when the value is updated in the cache
   */
  void update(DataTag dataTag, Object value, String valueDesc, Timestamp sourceTimestamp, Timestamp daqTimestamp, Timestamp cacheTimestamp);
  
  
  /**
   * Creates a DataTagCacheObject with the given id. This method should only be
   * used to create a DataTag that was not located in the cache, nor is configured
   * in the database (TIMPRO account).
   * @param id
   * @return
   */
  DataTagCacheObject createEmptyDataTag(Long id);

  /**
   * Updates and invalidates the tag with the provided invalid quality and sets all the timestamps.
   * The quality field of the tag is set to the passed quality object (all other quality flags are overridden).
   * @param dataTag
   * @param value
   * @param valueDescription
   * @param sourceTimestamp source timestamp
   * @param daqTimestamp DAQ timestamp
   * @param cacheTimestamp cache timestamp  
   */
  void updateAndInvalidate(DataTag dataTag, Object value, String valueDescription, Timestamp sourceTimestamp, Timestamp daqTimestamp, 
                                            Timestamp cacheTimestamp, DataTagQuality dataTagQuality);

  /**
   * Sets the individual timestamps fo this DataTag 
   * 
   * @param dataTag the Tag
   * @param sourceTimestamp time obtained from source (Equipment)
   * @param daqTimestamp when value is processed by DAQ core
   * @param cacheTimestamp when value is put in cache
   */
  void setTimestamps(DataTag dataTag, Timestamp sourceTimestamp, Timestamp daqTimestamp, Timestamp cacheTimestamp);


  /**
   * Sets a new quality for this DataTag, overwriting all current quality flags. The value is not changed, but the cache
   * timestamp is updated.
   * 
   * @param dataTag tag to update
   * @param dataTagQuality the new quality, that will override all current quality settings
   * @param cacheTimestamp the new server timestamp
   */
  void setQuality(final DataTag dataTag, final DataTagQuality dataTagQuality, final Timestamp cacheTimestamp);


}
