/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.configuration.handler.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.configuration.handler.DataTagConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.DataTagConfigTransacted;
import cern.c2mon.server.configuration.impl.ConfigurationUpdateImpl;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;

/**
 * See interface documentation also.
 * 
 * <p>Currently all alarms and rules must be manually removed from any tag before it can be removed.
 * This also applied when removing an Equipment or Process: this will only succeed if all alarms
 * and rules have first been removed.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class DataTagConfigHandlerImpl implements DataTagConfigHandler {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(DataTagConfigHandlerImpl.class);  
  
  /**
   * Bean with DB transactions on methods.
   */
  @Autowired
  private DataTagConfigTransacted dataTagConfigTransacted;
  
  /**
   * Helper class for accessing the List of registered listeners
   * for configuration updates.
   */
  private ConfigurationUpdateImpl configurationUpdateImpl;
  
  /**
   * Cache for final removal.
   */
  private DataTagCache dataTagCache;
  
  private EquipmentFacade equipmentFacade;

  /**
   * Constructor.
   * @param dataTagCache cache
   * @param equipmentFacade
   * @param configurationUpdateImpl
   */
  @Autowired
  public DataTagConfigHandlerImpl(DataTagCache dataTagCache, EquipmentFacade equipmentFacade, ConfigurationUpdateImpl configurationUpdateImpl) {        
    this.dataTagCache = dataTagCache;
    this.equipmentFacade = equipmentFacade;
    this.configurationUpdateImpl = configurationUpdateImpl;
  }

  @Override
  public ProcessChange createDataTag(ConfigurationElement element) throws IllegalAccessException {
    ProcessChange change = dataTagConfigTransacted.doCreateDataTag(element);
    dataTagCache.lockAndNotifyListeners(element.getEntityId());    
    if (LOGGER.isTraceEnabled()) {
    	LOGGER.trace("createDataTag - Notifying Configuration update listeners");
    }
    this.configurationUpdateImpl.notifyListeners(element.getEntityId());
    return change;
  }

  @Override
  public List<ProcessChange> removeDataTag(Long id, ConfigurationElementReport tagReport) {
    LOGGER.trace("Removing DataTag " + id);
    try {
      List<ProcessChange> changes = dataTagConfigTransacted.doRemoveDataTag(id, tagReport);
      DataTag dataTag = dataTagCache.get(id);
      dataTagCache.remove(id); //only removed from cache if no exception is thrown
      //remove from Equipment list only once definitively removed from DB & cache (o.w. remove/recreate Process/Equipment cannot reach it)
      equipmentFacade.removeTagFromEquipment(dataTag.getEquipmentId(), dataTag.getId());
      return changes;
    } catch (CacheElementNotFoundException e) {     
      tagReport.setWarning(e.getMessage());
      return new ArrayList<ProcessChange>(); //no changes for DAQ layer
    }    
  }

  @Override
  public ProcessChange updateDataTag(Long id, Properties elementProperties) {
	  try {
		  ProcessChange processChange = dataTagConfigTransacted.doUpdateDataTag(id, elementProperties);
		  if (LOGGER.isTraceEnabled()) {
		    	LOGGER.trace("createDataTag - Notifying Configuration update listeners");
		    }
		  this.configurationUpdateImpl.notifyListeners(id);
		  return processChange;
	  } catch (UnexpectedRollbackException e) {
		  LOGGER.error("Rolling back update in cache");
		  dataTagCache.remove(id); //DB transaction is rolled back here: reload the tag
		  dataTagCache.loadFromDb(id);
		  throw e;
	  }
  }

  @Override
  public void addAlarmToTag(Long tagId, Long alarmId) {
    dataTagConfigTransacted.addAlarmToTag(tagId, alarmId);
  }

  @Override
  public void addRuleToTag(Long tagId, Long ruleId) {
    dataTagConfigTransacted.addRuleToTag(tagId, ruleId);
  }

  @Override
  public void removeAlarmFromTag(Long tagId, Long alarmId) {
    dataTagConfigTransacted.removeAlarmFromTag(tagId, alarmId);
  }

  @Override
  public void removeRuleFromTag(Long tagId, Long ruleId) {
    dataTagConfigTransacted.removeRuleFromTag(tagId, ruleId);
  }  
}
