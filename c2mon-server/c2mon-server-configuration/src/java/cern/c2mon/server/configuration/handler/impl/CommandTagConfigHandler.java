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

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.CommandTagCache;
import cern.tim.server.cache.CommandTagFacade;
import cern.tim.server.cache.EquipmentFacade;
import cern.tim.server.cache.loading.CommandTagDAO;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.daq.command.CommandTag;
import cern.tim.shared.daq.config.Change;
import cern.tim.shared.daq.config.CommandTagAdd;
import cern.tim.shared.daq.config.CommandTagRemove;

/**
 * See interface documentation.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class CommandTagConfigHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(CommandTagConfigHandler.class);
  
  @Autowired
  private CommandTagFacade commandTagFacade;
  
  @Autowired
  private CommandTagDAO commandTagDAO;
  
  @Autowired
  private CommandTagCache commandTagCache;
  
  @Autowired
  private EquipmentFacade equipmentFacade;
  
  public List<ProcessChange> createCommandTag(ConfigurationElement element) throws IllegalAccessException {
    LOGGER.trace("Creating CommandTag " + element.getEntityId());
    CommandTag commandTag = commandTagFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    commandTagDAO.insertCommandTag(commandTag);
    commandTagCache.putQuiet(commandTag);
    equipmentFacade.addCommandToEquipment(commandTag.getEquipmentId(), commandTag.getId());
    CommandTagAdd commandTagAdd = new CommandTagAdd(element.getSequenceId(), 
                                                    commandTag.getEquipmentId(), 
                                                    commandTagFacade.generateSourceCommandTag(commandTag));    
    ArrayList<ProcessChange> processChanges = new ArrayList<ProcessChange>();
    processChanges.add(new ProcessChange(equipmentFacade.getProcessForAbstractEquipment(commandTag.getEquipmentId()).getId(), commandTagAdd));
    return processChanges;      
  }
  
  public List<ProcessChange> updateCommandTag(Long id, Properties properties) throws IllegalAccessException {
    LOGGER.trace("Updating CommandTag " + id);
    //reject if trying to change equipment it is attached to - not currently allowed
    if (properties.containsKey("equipmentId")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the equipment to which a command is attached - this is not currently supported!");
    }
    CommandTag commandTag = commandTagCache.get(id);
    Change commandTagUpdate = null;
    try {
      commandTag.getWriteLock().lock();
      commandTagUpdate = commandTagFacade.updateConfig(commandTag, properties);
      commandTagDAO.updateCommandTag(commandTag);
      commandTagCache.get(commandTag.getId());
    } catch (RuntimeException ex) {
      
      throw ex; 
    } finally {
      commandTag.getWriteLock().unlock();
    }    
    ArrayList<ProcessChange> processChanges = new ArrayList<ProcessChange>();
    processChanges.add(new ProcessChange(equipmentFacade.getProcessForAbstractEquipment(commandTag.getEquipmentId()).getId(), commandTagUpdate));
    return processChanges;    
  }
  
  /**
   * 
   * @param id
   * @param elementReport
   * @return a ProcessChange event to send to the DAQ if no error occurred
   */
  public List<ProcessChange> removeCommandTag(final Long id, final ConfigurationElementReport elementReport) {
    LOGGER.trace("Removing CommandTag " + id);
    CommandTag commandTag = commandTagCache.get(id);
    try {
      commandTag.getWriteLock().lock();            
      commandTagDAO.deleteCommandTag(commandTag.getId());
      commandTagCache.remove(commandTag.getId());
      commandTag.getWriteLock().unlock();
      //unlock before accessing equipment
      equipmentFacade.removeCommandFromEquipment(commandTag.getEquipmentId(), commandTag.getId());
    } catch (Exception ex) {      
      elementReport.setFailure("Exception caught while removing a commandtag.", ex);
      LOGGER.error("Exception caught while removing a commandtag (id: " + id + ")", ex);
      throw new RuntimeException(ex);
    } finally {
      if (commandTag.getWriteLock().isHeldByCurrentThread()) {
        commandTag.getWriteLock().unlock();
      }      
    }      
    CommandTagRemove removeEvent = new CommandTagRemove();  
    removeEvent.setCommandTagId(id);
    removeEvent.setEquipmentId(commandTag.getEquipmentId());
    ArrayList<ProcessChange> processChanges = new ArrayList<ProcessChange>();
    processChanges.add(new ProcessChange(equipmentFacade.getProcessForAbstractEquipment(commandTag.getEquipmentId()).getId(), removeEvent));
    return processChanges;    
  }
  
}
