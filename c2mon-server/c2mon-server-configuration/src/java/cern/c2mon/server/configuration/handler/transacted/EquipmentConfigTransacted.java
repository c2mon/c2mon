package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.common.equipment.Equipment;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;

/**
 *  For internal use only. Allows use of Spring AOP for transaction management.
 * 
 * @author Mark Brightwell
 *
 */
public interface EquipmentConfigTransacted extends CommonEquipmentConfigTransacted<Equipment> {

  /**
   * Create equipment in transaction.
   * @param element
   * @return 
   * @throws IllegalAccessException
   */
  ProcessChange doCreateEquipment(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Remove equipment in transaction.
   * @param equipment
   * @param equipmentReport
   */
  void doRemoveEquipment(Equipment equipment, ConfigurationElementReport equipmentReport);

}
