package cern.c2mon.server.configuration.handler.transacted;

import java.util.List;
import java.util.Properties;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.common.equipment.AbstractEquipment;

/**
 * Common parts of transacted config bean for Equipment/Sub-equipment. 
 * 
 * @author Mark Brightwell
 *
 * @param <T> type of abstract equipment
 */
public interface CommonEquipmentConfigTransacted<T extends AbstractEquipment> {

  /**
   * Transacted method for creating sub-equipment.
   * @param subEquipment ref to sub-equipment
   * @param properties details of update
   * @return change event for DAQ
   * @throws IllegalAccessException
   */
  List<ProcessChange> doUpdateAbstractEquipment(T abstractEquipment, Properties properties) throws IllegalAccessException;
  
}
