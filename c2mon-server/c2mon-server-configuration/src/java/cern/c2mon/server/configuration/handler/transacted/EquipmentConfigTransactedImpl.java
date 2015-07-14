package cern.c2mon.server.configuration.handler.transacted;

import java.util.ArrayList;
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
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.ControlTagFacade;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessXMLProvider;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.DataTagAdd;
import cern.c2mon.shared.daq.config.EquipmentUnitAdd;
import cern.c2mon.shared.daq.config.IChange;

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
  
  private ProcessXMLProvider processXMLProvider;
  
  private ControlTagCache controlCache;
  
  private ControlTagFacade controlTagFacade;
  
  @Autowired
  public EquipmentConfigTransactedImpl(ControlTagConfigHandler controlTagConfigHandler, AliveTimerCache aliveTimerCache,
                                CommFaultTagCache commFaultTagCache, EquipmentCache abstractEquipmentCache, 
                                EquipmentFacade equipmentFacade, EquipmentDAO equipmentDAO, ProcessXMLProvider processXMLProvider, 
                                ControlTagCache controlCache, ControlTagFacade controlTagFacade) {
    super(controlTagConfigHandler, equipmentFacade, abstractEquipmentCache, equipmentDAO,
        aliveTimerCache, commFaultTagCache);
    this.equipmentFacade = equipmentFacade;
    this.equipmentDAO = equipmentDAO;
    this.processXMLProvider = processXMLProvider;
    this.controlCache = controlCache;
    this.controlTagFacade = controlTagFacade;
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
  public List<ProcessChange> doCreateEquipment(ConfigurationElement element) throws IllegalAccessException {
    Equipment equipment = super.createAbstractEquipment(element);
    equipmentFacade.addEquipmentToProcess(equipment.getId(), equipment.getProcessId());
    
    // Please note, that the Equipment XML configuration is also containing the Alive tag configuration.
    // It's therefore not required to send an additional ProcessChange object for creating it.
    EquipmentUnitAdd equipmentUnitAdd = new EquipmentUnitAdd(element.getSequenceId(), equipment.getId(), processXMLProvider.getEquipmentConfigXML(equipment.getId()));
    
    List<ProcessChange> result = new ArrayList<ProcessChange>();
    result.add(new ProcessChange(equipment.getProcessId(), equipmentUnitAdd));

    // ProcessChange events are ignored (see explanation above)
    updateControlTagInformation(element, equipment);
    
    return result;
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
  
  /**
   * Ensures that the Alive-, Status- and CommFault Tags have appropriately the equipment id set.
   * @param equipment The equipment to which the control tags are assigned
   */
  private List<ProcessChange> updateControlTagInformation(final ConfigurationElement element, final Equipment equipment) {
      
      List<ProcessChange> changes = new ArrayList<ProcessChange>(3);
      Long processId = equipment.getProcessId();
      Long equipmentId = equipment.getId();
      
      ControlTag aliveTagCopy = controlCache.getCopy(equipment.getAliveTagId());
      if (aliveTagCopy != null) {
        setEquipmentId((ControlTagCacheObject) aliveTagCopy, equipmentId, processId);
        
        if (aliveTagCopy.getAddress() != null) {
          IChange toAdd = new DataTagAdd(element.getSequenceId(), equipmentId, controlTagFacade.generateSourceDataTag(aliveTagCopy));
          ConfigurationElementReport report = new ConfigurationElementReport(Action.CREATE, Entity.CONTROLTAG, aliveTagCopy.getId());
          ProcessChange change = new ProcessChange(processId, toAdd);
          change.setNestedSubReport(report);
          changes.add(change);
        }
      } else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
            String.format("No Alive tag (%s) found for equipment #%d (%s).", equipment.getAliveTagId(), equipment.getId(), equipment.getName()));
      }
      
      ControlTag commFaultTagCopy = controlCache.getCopy(equipment.getCommFaultTagId());
      if (commFaultTagCopy != null) {
        setEquipmentId((ControlTagCacheObject) commFaultTagCopy, equipmentId, processId);
      } else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
            String.format("No CommFault tag (%s) found for equipment #%d (%s).", equipment.getCommFaultTagId(), equipment.getId(), equipment.getName()));
      }
      
      ControlTag statusTagCopy = controlCache.getCopy(equipment.getStateTagId());
      if (statusTagCopy != null) {
        setEquipmentId((ControlTagCacheObject) statusTagCopy, equipmentId, processId);
      } else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
            String.format("No Status tag (%s) found for equipment #%d (%s).", equipment.getStateTagId(), equipment.getId(), equipment.getName()));
      }
      
      return changes;
  }
  
  private void setEquipmentId(ControlTagCacheObject copy, Long equipmentId, Long processId) {
    String logMsg = String.format("Adding equipment id #%s to control tag #%s", equipmentId, copy.getId()); 
    LOGGER.trace(logMsg);
    copy.setEquipmentId(equipmentId);
    copy.setProcessId(processId);
    controlCache.putQuiet(copy);
  }
  
}
