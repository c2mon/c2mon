package cern.c2mon.server.configuration.handler.transacted;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessXMLProvider;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.daq.config.EquipmentUnitAdd;

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
  
  private ProcessXMLProvider processXMLProvider;
  
  @Autowired
  public EquipmentConfigTransactedImpl(ControlTagConfigHandler controlTagConfigHandler, AliveTimerCache aliveTimerCache,
                                CommFaultTagCache commFaultTagCache, EquipmentCache abstractEquipmentCache, 
                                EquipmentFacade equipmentFacade, EquipmentDAO equipmentDAO, EquipmentCache equipmentCache,
                                ProcessCache processCache, ProcessXMLProvider processXMLProvider) {
    super(controlTagConfigHandler, equipmentFacade, abstractEquipmentCache, equipmentDAO,
        aliveTimerCache, commFaultTagCache);
    this.equipmentFacade = equipmentFacade;
    this.equipmentDAO = equipmentDAO;
    this.equipmentCache = equipmentCache;
    this.processCache = processCache;
    this.processXMLProvider = processXMLProvider;
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
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange doCreateEquipment(ConfigurationElement element) throws IllegalAccessException {
    Equipment equipment = super.createAbstractEquipment(element);
    equipmentFacade.addEquipmentToProcess(equipment.getId(), equipment.getProcessId());
    EquipmentUnitAdd equipmentUnitAdd = new EquipmentUnitAdd(element.getSequenceId(), equipment.getId(), processXMLProvider.getEquipmentConfigXML(equipment.getId()));
    return new ProcessChange(equipment.getProcessId(), equipmentUnitAdd);
  }
  
  @Override
  @Transactional(value = "cacheTransactionManager")
  public List<ProcessChange> doUpdateAbstractEquipment(Equipment equipment, Properties properties) throws IllegalAccessException {   
    return super.updateAbstractEquipment(equipment, properties);        
  }
   
  @Override
  @Transactional(value = "cacheTransactionManager", propagation=Propagation.REQUIRES_NEW)
  public void doRemoveEquipment(final Equipment equipment, final ConfigurationElementReport equipmentReport) {
    LOGGER.debug("Removing Equipment " + equipment.getId() + " from DB");
    try {
      equipmentDAO.deleteItem(equipment.getId());                                     
    } catch (UnexpectedRollbackException ex) {
      equipmentReport.setFailure("Aborting removal of equipment " + equipment.getId() + " as unable to remove it from DB."); 
        throw new UnexpectedRollbackException("Interrupting removal of Equipment as failed to remove it from DB - " 
            + "control tags will not be removed.", ex);      
    }              
  }
  
}
