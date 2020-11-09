package cern.c2mon.cache.api.service;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;

import java.util.Map;

/**
 * Common Service methods for Equipment and SubEquipment.
 *
 * @author Szymon Halastra
 */
public interface CommonEquipmentOperations {

  /**
   * Returns the Process id for a given Equipment or SubEquipment
   * <p>
   * <p>Throws a {@link CacheElementNotFoundException} if the SubEquipment, Equipment
   * or Process cannot be located in the cache. Throws a {@link NullPointerException}
   * if the Equipment/SubEquipment has no process/equipment Id set.
   *
   * @param abstractEquipmentId the Id of the (Sub)Equipment
   *
   * @return The id to the Process object in the cache
   */
  Long getProcessId(Long abstractEquipmentId);

  /**
   * Dynamically creates a Map ControlTag id -> Equipment id.
   * ControlTags with no associated AbstractEquipment (or associated
   * to a DAQ) are not in the Map.
   * <p>
   * <p>Is not designed for intensive use (only used during
   * live reconfiguration of ControlTags). If needed for intensive
   * use, change the code to store this information in the cache.
   *
   * @return the map {ControlTag id -> Equipment id}
   */
  Map<Long, Long> getControlTags();

  /**
   * Removes the commfault tag for this equipment from the
   * equipment and the commfault cache.
   *
   * @param abstractEquipmentId id of abstract equipment
   *
   * @throws CacheElementNotFoundException if the abstractEquipment cannot be located in the corresponding cache
   */
  void removeCommFaultTag(Long abstractEquipmentId);

}