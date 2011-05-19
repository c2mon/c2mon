package cern.c2mon.server.configuration.handler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.tim.server.cache.AliveTimerCache;
import cern.tim.server.cache.AliveTimerFacade;
import cern.tim.server.cache.CommFaultTagCache;
import cern.tim.server.cache.CommFaultTagFacade;
import cern.tim.server.cache.EquipmentCache;
import cern.tim.server.cache.EquipmentFacade;
import cern.tim.server.cache.loading.EquipmentDAO;
import cern.tim.server.common.equipment.Equipment;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigConstants.Action;
import cern.tim.shared.client.configuration.ConfigConstants.Entity;
import cern.tim.shared.common.ConfigurationException;

/**
 * Provides reconfiguration methods for Equipment.
 * 
 * <p>Should currently not be used outside the module
 * (interface should be provided before this is done).
 * 
 * <p>The methods here should be used to reconfigure a
 * running server. They take care of updating the cache
 * and database.
 * 
 * <p>There is no "rollback", meaning that the system could
 * be left in an inconsistent state if the database update 
 * fails (in general, the DB update is performed at the end).
 * In this case, removing an object and recreating it may
 * be necessary. 
 * 
 * <p>An equipment can only be completely removed once all alarms
 * and rules associated to its tags have been manually removed
 * (no recursive action here). In this case the equipment will
 * remain with control tags and subequipments, together with
 * the datatags with associated alarms or rules.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class EquipmentConfigHandler extends AbstractEquipmentConfigHandler<Equipment> {

  private static final Logger LOGGER = Logger.getLogger(EquipmentConfigHandler.class); 
  
  private EquipmentFacade equipmentFacade;
  
  private EquipmentDAO equipmentDAO;
  
  private EquipmentCache equipmentCache;
  
  private DataTagConfigHandler dataTagConfigHandler; 
  
  private CommandTagConfigHandler commandTagConfigHandler;
  
  private SubEquipmentConfigHandler subEquipmentConfigHandler;
  
  @Autowired
  public EquipmentConfigHandler(ControlTagConfigHandler controlTagConfigHandler, AliveTimerCache aliveTimerCache,
                                CommFaultTagCache commFaultTagCache, EquipmentCache abstractEquipmentCache, 
                                EquipmentFacade equipmentFacade, EquipmentDAO equipmentDAO, EquipmentCache equipmentCache,
                                DataTagConfigHandler dataTagConfigHandler, CommFaultTagFacade commFaultTagFacade, 
                                AliveTimerFacade aliveTimerFacade, CommandTagConfigHandler commandTagConfigHandler, 
                                SubEquipmentConfigHandler subEquipmentConfigHandler) {
    super(controlTagConfigHandler, equipmentFacade, abstractEquipmentCache, equipmentDAO,
        aliveTimerFacade, commFaultTagFacade);
    this.equipmentFacade = equipmentFacade;
    this.equipmentDAO = equipmentDAO;
    this.equipmentCache = equipmentCache;
    this.dataTagConfigHandler = dataTagConfigHandler;       
    this.commandTagConfigHandler = commandTagConfigHandler;
    this.subEquipmentConfigHandler = subEquipmentConfigHandler;
  }

  /**
   * Inserts the equipment into the cache and updates the DB.
   * The Process in the cache is updated to refer to the new
   * Equipment.
   * 
   * <p>Also updates the associated cache object in the AliveTimer
   * and CommFaultTag caches. 
   * 
   * @param element the configuration element
   * @throws IllegalAccessException 
   */
  public void createEquipment(ConfigurationElement element) throws IllegalAccessException {
    Equipment equipment = super.createAbstractEquipment(element);
    equipmentFacade.addEquipmentToProcess(equipment.getId(), equipment.getProcessId());
  }
  
  /**
   * Rules need to be removed before this operation can complete successfully
   * (DataTags on which rules depend will be left in place, consistently in cache
   * and DB; DAQ will continue to function as expected, but with the RECONFIGURED
   * process state).
   * 
   * <p>In general, if a DataTag cannot be removed, the operation will keep Equipment
   * SubEquipment and Processes in place (including ControlTags).
   *
   * <p>Any failure when removing the SubEquipments will interrupt the Equipment removal
   * and leave it in the current state (no rollback of SubEquipment removals).
   * 
   * <p>Remove operations should always succeed as a last resort to restoring a consistent
   * configuration. Will only fail to complete if alarms or rules are associated to tags
   * managed by this equipment. In this case these should be removed first.
   * 
   * 
   * @param equipmentid the id of the equipment to be removed
   * @param equipmentReport the equipment-level configuration report
   */
  public void removeEquipment(final Long equipmentid, final ConfigurationElementReport equipmentReport) {
    Equipment equipment = equipmentCache.get(equipmentid);    
    try {
      equipment.getWriteLock().lock();
      boolean abortAfterTags = removeEquipmentTags(equipment, equipmentReport);
      boolean abort = abortAfterTags || removeEquipmentCommands(equipment, equipmentReport);
      if (abort) {       
        equipmentReport.setFailure("Aborting removal of equipment "
            + equipmentid + " as unable to remove all associated datatags."); 
        throw new ConfigurationException(ConfigurationException.UNDEFINED, "Aborting removal of Equipment as failed to remove all" 
            + "associated datatags and commandtags.");
      } else {        
        removeSubEquipments(equipment, equipmentReport);        
        equipmentDAO.deleteItem(equipmentid);
        equipmentFacade.removeCacheObject(equipmentCache.get(equipmentid));
        removeEquipmentControlTags(equipment, equipmentReport); //must be removed last as equipment references them
      }          
    } finally {
      equipment.getWriteLock().unlock();
    }         
  }

  

  /**
   * Removes the subequipments attached to this equipment.
   * Exceptions are caught, added to the report and thrown
   * up to interrupt the equipment removal (all exceptions
   * are wrapped in {@link ConfigurationException}).
   * 
   *<p>Call within Equipment lock.
   * 
   * @param equipment the equipment for which the subequipments should be removed
   * @param equipmentReport the report at the equipment level
   */
  private void removeSubEquipments(Equipment equipment, ConfigurationElementReport equipmentReport) {
    for (Long subEquipmentId : equipment.getSubEquipmentIds()) {
      ConfigurationElementReport subEquipmentReport = new ConfigurationElementReport(Action.REMOVE, Entity.SUBEQUIPMENT, subEquipmentId);
      equipmentReport.addSubReport(subEquipmentReport);
      try {
        subEquipmentConfigHandler.removeSubEquipment(subEquipmentId, subEquipmentReport);
      } catch (Exception ex) {
        subEquipmentReport.setFailure("Exception caught - aborting removal of subequipment "
            + subEquipmentId , ex); 
        throw new ConfigurationException(ConfigurationException.UNDEFINED, ex);
      }
      
    }
  }

  /**
   * Removes all command tags associated with this equipment.
   * @param equipment
   * @param equipmentReport
   * @return
   */
  private boolean removeEquipmentCommands(Equipment equipment, ConfigurationElementReport equipmentReport) {
    boolean abortAfterCommands = false;
    for (Long commandTagId : equipment.getCommandTagIds()) {
      ConfigurationElementReport commandReport = new ConfigurationElementReport(Action.REMOVE, Entity.COMMANDTAG, commandTagId);
      equipmentReport.addSubReport(commandReport);
      try {
        commandTagConfigHandler.removeCommandTag(commandTagId, commandReport);
      } catch (Exception ex) {
        LOGGER.error("Exception caught while removing Equipment tags.", ex);
        abortAfterCommands = true;
      }      
    }
    return abortAfterCommands;
  }

  /**
   * Removes the tags for this equipment. The DAQ is not informed as
   * this method is only called when the whole Equipment is removed.
   * 
   * <p>Call within equipment lock.
   * @param equipment for which the tags should be removed
   * @return true if configuration should be aborted
   */
  private boolean removeEquipmentTags(Equipment equipment, ConfigurationElementReport equipmentReport) {
    boolean abortAfterDataTags = false;
    for (Long dataTagId : equipment.getDataTagIds()) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.DATATAG, dataTagId);
      equipmentReport.addSubReport(tagReport);
      try {
        dataTagConfigHandler.removeDataTag(dataTagId, tagReport);
      } catch (Exception ex) {
        LOGGER.error("Exception caught while removing Equipment tags.", ex);
        abortAfterDataTags = true;
      }                 
    }
    return abortAfterDataTags;
  }
  
  /**
   * Adds a tag to the equipment.
   * @param equipmentId
   * @param dataTagId
   */
//  public void addDataTagToEquipment(Long equipmentId, Long dataTagId) {
//    equipmentFacade.addTagToEquipment(equipmentId, dataTagId);        
//  }
  
  /**
   * Removes the specified tag from the list of tags for this equipment.
   * @param equipmentId
   * @param dataTagId
   */
//  public void removeDataTagFromEquipment(Long equipmentId, Long dataTagId) {    
//    equipmentFacade.removeTagFromEquipment(equipmentId, dataTagId);              
//  }

  
}
