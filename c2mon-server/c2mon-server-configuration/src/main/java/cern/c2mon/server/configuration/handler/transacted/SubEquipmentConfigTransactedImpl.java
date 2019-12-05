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
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.SubEquipmentConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.DataTagAdd;
import cern.c2mon.shared.daq.config.IChange;
import cern.c2mon.shared.daq.config.SubEquipmentUnitAdd;
import cern.c2mon.shared.daq.config.SubEquipmentUnitRemove;
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
 * See interface docs.
 *
 * @author Mark Brightwell
 *
 */
@Service
public class SubEquipmentConfigTransactedImpl extends AbstractEquipmentConfigTransacted<SubEquipment> implements SubEquipmentConfigHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SubEquipmentConfigTransactedImpl.class);

  /**
   * Facade.
   */
  private final SubEquipmentFacade subEquipmentFacade;

  /**
   * DAO.
   */
  private final SubEquipmentDAO subEquipmentDAO;

  private final ControlTagCache controlCache;
  
  private final ControlTagFacade controlTagFacade;

  private final ProcessXMLProvider processXMLProvider;

  /**
   * Autowired constructor.
   */
  @Autowired
  public SubEquipmentConfigTransactedImpl(ControlTagConfigHandler controlTagConfigHandler,
                                          SubEquipmentFacade subEquipmentFacade,
                                          SubEquipmentCache subEquipmentCache,
                                          SubEquipmentDAO subEquipmentDAO,
                                          AliveTimerCache aliveTimerCache,
                                          CommFaultTagCache commFaultTagCache,
                                          ProcessCache processCache,
                                          ProcessXMLProvider processXMLProvider,
                                          ControlTagCache controlCache, 
                                          ControlTagFacade controlTagFacade) {
    super(controlTagConfigHandler, subEquipmentFacade, subEquipmentCache, subEquipmentDAO, aliveTimerCache, commFaultTagCache);
    this.subEquipmentFacade = subEquipmentFacade;
    this.subEquipmentDAO = subEquipmentDAO;
    this.processXMLProvider = processXMLProvider;
    this.controlCache = controlCache;
    this.controlTagFacade = controlTagFacade;
  }

  /**
   * Creates the SubEquipment cache object and puts it into the cache and DB.
   * The alive and commfault tag caches are updated also. The Equipment cache
   * object is updated to include the new SubEquipment.
   *
   * @param element details of configuration
   * @throws IllegalAccessException should not be thrown here (in common
   *           interface for Tags)
   */
  @Override
  @Transactional(value = "cacheTransactionManager")
  public List<ProcessChange> create(final ConfigurationElement element) throws IllegalAccessException {
    SubEquipment subEquipment = super.createAbstractEquipment(element);
    subEquipmentFacade.addSubEquipmentToEquipment(subEquipment.getId(), subEquipment.getParentId());
    SubEquipmentUnitAdd subEquipmentUnitAdd = new SubEquipmentUnitAdd(element.getSequenceId(), subEquipment.getId(), subEquipment.getParentId(),
    processXMLProvider.getSubEquipmentConfigXML((SubEquipmentCacheObject) subEquipment));
    
    List<ProcessChange> changes = new ArrayList<ProcessChange>();
    changes.add(new ProcessChange(subEquipmentFacade.getProcessIdForAbstractEquipment(subEquipment.getId()), subEquipmentUnitAdd));
    changes.addAll(updateControlTagInformation(element, subEquipment));
    
    return changes;
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public List<ProcessChange> update(final SubEquipment subEquipment, Properties properties) throws IllegalAccessException {
    return super.updateAbstractEquipment(subEquipment, properties);
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public List<ProcessChange> remove(final SubEquipment subEquipment, final ConfigurationElementReport subEquipmentReport) {
    List<ProcessChange> processChanges = new ArrayList<ProcessChange>();

    try {
      subEquipmentDAO.deleteItem(subEquipment.getId());
      Long processId = this.subEquipmentFacade.getProcessIdForAbstractEquipment(subEquipment.getId());

      SubEquipmentUnitRemove subEquipmentUnitRemove = new SubEquipmentUnitRemove(0L, subEquipment.getId(), subEquipment.getParentId());
      processChanges.add(new ProcessChange(processId, subEquipmentUnitRemove));

    } catch (RuntimeException e) {
      subEquipmentReport.setFailure("Rolling back removal of sub-equipment " + subEquipment.getId());
      throw new UnexpectedRollbackException("Exception caught while removing Sub-equipment from DB: rolling back", e);
    }

    return processChanges;
  }
  
  
  /**
   * Ensures that the Alive-, Status- and CommFault Tags have appropriately the sub-equipment id set.
   * @param subEquipment The sub-equipment to which the control tags are assigned
   */
  private List<ProcessChange> updateControlTagInformation(final ConfigurationElement element, final SubEquipment subEquipment) {
      
      List<ProcessChange> changes = new ArrayList<ProcessChange>(3);
      final Long processId = subEquipmentFacade.getProcessIdForAbstractEquipment(subEquipment.getId());
      
      ControlTag aliveTagCopy = controlCache.getCopy(subEquipment.getAliveTagId());
      if (aliveTagCopy != null) {
        setSubEquipmentId((ControlTagCacheObject) aliveTagCopy, subEquipment.getId(), processId);
        
        if (aliveTagCopy.getAddress() != null) {
          // Inform Process about newly added alive tag
          IChange toAdd = new DataTagAdd(element.getSequenceId(), subEquipment.getId(), controlTagFacade.generateSourceDataTag(aliveTagCopy));
          ConfigurationElementReport report = new ConfigurationElementReport(Action.CREATE, Entity.CONTROLTAG, aliveTagCopy.getId());
          ProcessChange change = new ProcessChange(processId, toAdd);
          change.setNestedSubReport(report);
          changes.add(change);
        }
        else {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
              String.format("Alive tag #%d (%s) for sub-equipment #%d (%s) must by definition have a hardware address defined.", aliveTagCopy.getId(), aliveTagCopy.getName(), subEquipment.getId(), subEquipment.getName()));
        }
      
      } else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
            String.format("No Alive tag (%s) found for sub-equipment #%d (%s).", subEquipment.getAliveTagId(), subEquipment.getId(), subEquipment.getName()));
      }
      
      
      ControlTag commFaultTagCopy = controlCache.getCopy(subEquipment.getCommFaultTagId());
      if (commFaultTagCopy != null) {
        setSubEquipmentId((ControlTagCacheObject) commFaultTagCopy, subEquipment.getId(), processId);
      } else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
            String.format("No CommFault tag (%s) found for sub-equipment #%d (%s).", subEquipment.getCommFaultTagId(), subEquipment.getId(), subEquipment.getName()));
      }
      
      
      ControlTag statusTagCopy = controlCache.getCopy(subEquipment.getStateTagId());
      if (statusTagCopy != null) {
        setSubEquipmentId((ControlTagCacheObject) statusTagCopy, subEquipment.getId(), processId);
      } else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
            String.format("No Status tag (%s) found for sub-equipment #%d (%s).", subEquipment.getStateTagId(), subEquipment.getId(), subEquipment.getName()));
      }
      
      return changes;
  }
  
  private void setSubEquipmentId(ControlTagCacheObject copy, Long subEquipmentId, Long processId) {
    String logMsg = String.format("Adding sub-equipment id #%s to control tag #%s", subEquipmentId, copy.getId()); 
    LOGGER.trace(logMsg);
    copy.setSubEquipmentId(subEquipmentId);
    copy.setProcessId(processId);
    controlCache.putQuiet(copy);
  }
  

}
