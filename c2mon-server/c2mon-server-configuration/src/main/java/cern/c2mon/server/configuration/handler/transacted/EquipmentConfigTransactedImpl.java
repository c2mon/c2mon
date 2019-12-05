/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.EquipmentConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.DataTagAdd;
import cern.c2mon.shared.daq.config.EquipmentUnitAdd;
import cern.c2mon.shared.daq.config.IChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Equipment configuration transacted methods.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class EquipmentConfigTransactedImpl extends AbstractEquipmentConfigTransacted<Equipment> implements EquipmentConfigHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(EquipmentConfigTransactedImpl.class); 
  
  private final EquipmentFacade equipmentFacade;
  
  private final EquipmentDAO equipmentDAO;
  
  private final ProcessXMLProvider processXMLProvider;
  
  private final ControlTagCache controlCache;
  
  private final ControlTagFacade controlTagFacade;
  
  @Autowired
  public EquipmentConfigTransactedImpl(ControlTagConfigHandler controlTagConfigHandler, 
                                       EquipmentFacade equipmentFacade, 
                                       EquipmentCache abstractEquipmentCache,
                                       EquipmentDAO equipmentDAO, 
                                       AliveTimerCache aliveTimerCache,
                                       CommFaultTagCache commFaultTagCache, 
                                       ProcessXMLProvider processXMLProvider, 
                                       ControlTagCache controlCache, 
                                       ControlTagFacade controlTagFacade) {

    super(controlTagConfigHandler, equipmentFacade, abstractEquipmentCache, equipmentDAO, aliveTimerCache, commFaultTagCache);
    
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
  public List<ProcessChange> create(ConfigurationElement element) throws IllegalAccessException {
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
  public List<ProcessChange> update(Long id, Properties properties) throws IllegalAccessException {
    return super.updateAbstractEquipment(abstractEquipmentCache.get(id), properties);
  }
   
  @Override
  @Transactional(value = "cacheTransactionManager", propagation=Propagation.REQUIRES_NEW)
  public ProcessChange remove(final Long id, final ConfigurationElementReport equipmentReport) {
    LOGGER.debug("Removing Equipment " + id + " from DB");
    try {
      equipmentDAO.deleteItem(id);
    } catch (UnexpectedRollbackException ex) {
      equipmentReport.setFailure("Aborting removal of equipment " + id + " as unable to remove it from DB.");
        throw new UnexpectedRollbackException("Interrupting removal of Equipment as failed to remove it from DB - " 
            + "control tags will not be removed.", ex);      
    }
    return null;
  }
  
  /**
   * Ensures that the Alive-, Status- and CommFault Tags have appropriately the equipment id set.
   * @param equipment The equipment to which the control tags are assigned
   */
  private List<ProcessChange> updateControlTagInformation(final ConfigurationElement element, final Equipment equipment) {
      
      List<ProcessChange> changes = new ArrayList<ProcessChange>(3);
      Long processId = equipment.getProcessId();
      Long equipmentId = equipment.getId();
      
      if (equipment.getAliveTagId() != null) {
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
