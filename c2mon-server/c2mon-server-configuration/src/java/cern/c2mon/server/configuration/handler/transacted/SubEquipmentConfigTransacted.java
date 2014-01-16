package cern.c2mon.server.configuration.handler.transacted;

import java.util.List;
import java.util.Properties;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 *  For internal use only. Allows use of Spring AOP for transaction management.
 * 
 * @author Mark Brightwell
 *
 */
public interface SubEquipmentConfigTransacted extends CommonEquipmentConfigTransacted<SubEquipment> {

  /**
   * Transacted method for removing subequipment.
   * 
   * @param subEquipment ref to sub-equipment
   * @param subEquipmentReport report
   * @return change for DAQ
   */
  ProcessChange doRemoveSubEquipment(SubEquipment subEquipment, ConfigurationElementReport subEquipmentReport);

  /**
   * Transacted method for creating subequipment.
   * @param element config details
   * @return change for DAQ
   * @throws IllegalAccessException
   */
  ProcessChange doCreateSubEquipment(ConfigurationElement element) throws IllegalAccessException;

  
}
