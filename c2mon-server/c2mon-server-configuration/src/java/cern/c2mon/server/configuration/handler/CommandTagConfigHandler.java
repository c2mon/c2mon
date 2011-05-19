package cern.c2mon.server.configuration.handler;

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
