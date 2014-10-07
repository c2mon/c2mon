package cern.c2mon.server.common.subequipment;

import java.util.Collection;

import cern.c2mon.server.common.equipment.AbstractEquipment;

/**
 * Interface of the cache object representing a Subequipment. External
 * modules should use this interface to interact with the cache
 * object, rather than the specific implementation.
 *
 * @author Mark Brightwell
 *
 */
public interface SubEquipment extends AbstractEquipment {

  /**
   * Returns the id of the parent Equipment of this SubEquipment.
   * Should never be null.
   * @return the Id of the parent Equipment
   */
  Long getParentId();

  /**
   * Returns the live list of DataTag ids attached to this
   * SubEquipment; locking on SubEquipment level required if accessing
   * this.
   * @return list of DataTag ids
   */
  Collection<Long> getDataTagIds();

}
