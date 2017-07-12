package cern.c2mon.server.jcacheref.prototype.equipment;

import javax.cache.processor.EntryProcessorException;

/**
 * @author Szymon Halastra
 */
public interface EquipmentCommandCRUD {

  void addCommandToEquipment(Long equipmentId, Long commandId) throws EntryProcessorException ;

  void removeCommandFromEquipment(Long equipmentId, Long commandId);
}
