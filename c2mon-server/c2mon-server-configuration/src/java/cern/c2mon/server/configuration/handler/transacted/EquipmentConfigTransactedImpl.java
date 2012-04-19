package cern.c2mon.server.configuration.handler.transacted;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.DataTagConfigHandler;
import cern.c2mon.server.configuration.handler.ProcessConfigHandler;
import cern.c2mon.server.configuration.handler.SubEquipmentConfigHandler;
import cern.c2mon.server.configuration.handler.impl.CommandTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.AliveTimerCache;
import cern.tim.server.cache.CommFaultTagCache;
import cern.tim.server.cache.EquipmentCache;
import cern.tim.server.cache.EquipmentFacade;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.loading.EquipmentDAO;
import cern.tim.server.common.equipment.Equipment;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.common.ConfigurationException;

/**
 * Equipment configuration transacted methods.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class EquipmentConfigTransactedImpl extends AbstractEquipmentConfigTransacted<Equipment> implements EquipmentConfigTransacted {

  private static final Logger LOGGER = Logger.getLogger(EquipmentConfigTransactedImpl.class); 
  
  private EquipmentFacade equipmentFacade;
  
  private EquipmentDAO equipmentDAO;
  
  private EquipmentCache equipmentCache; 
  
  private ProcessCache processCache;
  
  @Autowired
  public EquipmentConfigTransactedImpl(ControlTagConfigHandler controlTagConfigHandler, AliveTimerCache aliveTimerCache,
                                CommFaultTagCache commFaultTagCache, EquipmentCache abstractEquipmentCache, 
                                EquipmentFacade equipmentFacade, EquipmentDAO equipmentDAO, EquipmentCache equipmentCache,
                                ProcessCache processCache) {
    super(controlTagConfigHandler, equipmentFacade, abstractEquipmentCache, equipmentDAO,
        aliveTimerCache, commFaultTagCache);
    this.equipmentFacade = equipmentFacade;
    this.equipmentDAO = equipmentDAO;
    this.equipmentCache = equipmentCache;
    this.processCache = processCache;
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
  @Override
  @Transactional("cacheTransactionManager")
  public ProcessChange doCreateEquipment(ConfigurationElement element) throws IllegalAccessException {
    Equipment equipment = super.createAbstractEquipment(element);
    equipmentFacade.addEquipmentToProcess(equipment.getId(), equipment.getProcessId());
    return new ProcessChange(equipment.getProcessId());
  }
  
  @Override
  @Transactional("cacheTransactionManager")
  public List<ProcessChange> doUpdateAbstractEquipment(Equipment equipment, Properties properties) throws IllegalAccessException {   
    return super.updateAbstractEquipment(equipment, properties);        
  }
   
  @Override
  @Transactional(value = "cacheTransactionManager", propagation=Propagation.REQUIRES_NEW)
  public ProcessChange doRemoveEquipment(final Equipment equipment, final ConfigurationElementReport equipmentReport) {
    LOGGER.debug("Removing Equipment " + equipment.getId() + " from DB");
    try {
      equipmentDAO.deleteItem(equipment.getId());                
      return new ProcessChange(equipment.getProcessId());                        
    } catch (UnexpectedRollbackException ex) {
      equipmentReport.setFailure("Aborting removal of equipment " + equipment.getId() + " as unable to remove it from DB."); 
        throw new UnexpectedRollbackException("Interrupting removal of Equipment as failed to remove it from DB - " 
            + "control tags will not be removed.", ex);      
    }              
  }
  
}
