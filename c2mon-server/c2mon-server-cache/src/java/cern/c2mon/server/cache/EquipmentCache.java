package cern.c2mon.server.cache;

import cern.c2mon.server.common.equipment.Equipment;

/**
 * The module public interface that should be used to access the Equipment
 * in the server cache. 
 * 
 * <p>It provides methods for retrieving references to the objects in the
 * cache, which may be accessed by other threads concurrently. To guarantee 
 * exclusive access the thread must synchronize on the Equipment object in
 * the cache.
 * 
 * <p>Notice that Equipments cannot share the same CommFault or AliveTag ids,
 * although this is not enforced in the cache DB. The loading of the CommFault
 * and Alive cache will then fail as multiple entries will be returned!
 * 
 * @author Mark Brightwell
 *
 */
public interface EquipmentCache extends C2monCacheWithListeners<Long, Equipment> {
  
  String cacheInitializedKey = "c2mon.cache.equipment.initialized";
  
}
