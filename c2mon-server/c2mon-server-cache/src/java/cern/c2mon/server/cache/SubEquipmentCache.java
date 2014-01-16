package cern.c2mon.server.cache;

import cern.c2mon.server.common.subequipment.SubEquipment;

/**
 * The module public interface that should be used to access the SubEquipment
 * server cache. 
 * 
 * <p>It provides methods for retrieving references to the objects in the
 * cache, which may be accessed by other threads concurrently. To guarantee 
 * exclusive access the thread must synchronize on the SubEquipment object in
 * the cache.
 * 
 * @author Mark Brightwell
 *
 */
public interface SubEquipmentCache extends C2monCacheWithListeners<Long, SubEquipment> {
  
  String cacheInitializedKey = "c2mon.cache.subequipment.initialized";
  
}
